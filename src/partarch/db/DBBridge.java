/*
 * Copyright (C) 2014 <gotozero@yandex.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package partarch.db;

import java.io.IOException;
import java.net.URL;
import partarch.db.util.OracleJDBCRowSetKeepConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.rowset.OracleJDBCRowSet;
import partarch.PartArchController;
import partarch.db.DBBridge.DBTask;
import partarch.db.util.PrepareSQL;
import partarch.dialogs.ConfirmationDialogController;
import partarch.model.Table;
import partarch.dialogs.ExceptionDialogController;
import partarch.model.Index;
import partarch.model.IndexColumn;
import partarch.model.IndexInfo;
import partarch.model.Partition;
import partarch.model.PartitionedColumns;
import partarch.model.Subpartition;
import partarch.model.SubpartitionedColumns;
import partarch.model.TableInfo;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class DBBridge extends Stage implements Initializable{

    private PartArchController partArchController;
    private Stage stage=null;
    // Fetch size for object such a ResultSet and so on
    private final int FETCH_SIZE_4K = 4000;

    private OracleConnection conn;
    // executes database operations concurrent to JavaFX operations.
    private ThreadPoolExecutor databaseExecutor;
    
    // Task Exeution Journal
    private ObservableList<DBTask> taskList = FXCollections.observableArrayList();
    // True if proccess can continue work
    // False If proccess must comply traffic rules and cannot move on
    private volatile boolean greenlight = true;
    

    /**
     * SQLs
     */
    private static final String SQLtableList = "SELECT T.OWNER, T.TABLE_NAME, \n"
            + "       CASE WHEN T.PARTITIONED = 'YES' THEN 'P' WHEN T.TEMPORARY = 'Y' THEN 'T' WHEN T.IOT_TYPE IS NOT NULL THEN 'I'  ELSE 'H' END AS \"TYPE\" \n"
            + "       FROM SYS.DBA_TABLES T\n"
            + "WHERE T.OWNER NOT IN ('SYS', 'DBSNMP', 'APEX', 'DMSYS', 'MDSYS', 'MDDATA', 'SYSMAN', 'SYSTEM', 'WMSYS', 'XDB', 'OUTLN', 'EXFSYS', 'CTXSYS', 'ORDSYS', 'OLAPSYS', 'APEX_040200') \n"
            + "order by T.OWNER, T.TABLE_NAME";
    private static final String SQLTablePartitioningInfo = "select DS.SIZE_MiB, DPT.OWNER, DPT.TABLE_NAME, DPT.PARTITIONING_TYPE, DPT.SUBPARTITIONING_TYPE, DPT.DEF_TABLESPACE_NAME,\n"
            + "DPT.PARTITION_COUNT, DPT.DEF_SUBPARTITION_COUNT, DPT.PARTITIONING_KEY_COUNT, DPT.SUBPARTITIONING_KEY_COUNT\n"
            + "from (select owner, segment_name, SUM(bytes)/1024/1024 SIZE_MiB from dba_segments where owner = ? and segment_name= ?\n"
            + "group by owner, segment_name) DS, DBA_PART_TABLES DPT \n"
            + "where DS.OWNER = DPT.OWNER(+) and DS.SEGMENT_NAME = DPT.TABLE_NAME(+)";
    private static final String SQLPartitionedColumns = " select DPKC.COLUMN_POSITION, DPKC.COLUMN_NAME, DTC.DATA_TYPE ||' ('|| DTC.DATA_LENGTH || ')' AS DATA_TYPE, DTC.COLUMN_ID\n"
            + "  from DBA_PART_KEY_COLUMNS DPKC, DBA_TAB_COLUMNS DTC \n"
            + " where DPKC.OWNER = DTC.OWNER and DPKC.NAME = DTC.TABLE_NAME and DPKC.COLUMN_NAME = DTC.COLUMN_NAME  AND DPKC.OBJECT_TYPE = 'TABLE' and DPKC.OWNER = ? and DPKC.NAME = ?";
    private static final String SQLSubpartitionedColumns = " select DPKC.COLUMN_POSITION, DPKC.COLUMN_NAME, DTC.DATA_TYPE ||' ('|| DTC.DATA_LENGTH || ')' AS DATA_TYPE, DTC.COLUMN_ID\n"
            + "  from DBA_SUBPART_KEY_COLUMNS DPKC, DBA_TAB_COLUMNS DTC \n"
            + " where DPKC.OWNER = DTC.OWNER and DPKC.NAME = DTC.TABLE_NAME and DPKC.COLUMN_NAME = DTC.COLUMN_NAME  AND DPKC.OBJECT_TYPE = 'TABLE' and DPKC.OWNER = ? and DPKC.NAME = ?";
    private static final String SQLPartitions = "SELECT PARTITION_NAME,HIGH_VALUE,HIGH_VALUE_LENGTH,TABLESPACE_NAME,NUM_ROWS,BLOCKS,\n"
            + "EMPTY_BLOCKS,LAST_ANALYZED,AVG_SPACE,SUBPARTITION_COUNT,COMPRESSION FROM sys.DBA_TAB_PARTITIONS\n"
            + "WHERE TABLE_OWNER = ? AND table_name = ? ORDER BY PARTITION_POSITION";
    private static final String SQLSubPartitions = "SELECT PARTITION_NAME,SUBPARTITION_NAME,HIGH_VALUE,HIGH_VALUE_LENGTH,TABLESPACE_NAME,NUM_ROWS,BLOCKS,\n"
            + "EMPTY_BLOCKS,LAST_ANALYZED,AVG_SPACE,COMPRESSION FROM sys.DBA_TAB_SUBPARTITIONS\n"
            + "WHERE TABLE_OWNER = ? AND table_name = ? ORDER BY PARTITION_NAME, SUBPARTITION_POSITION";
    private static final String SQLIndexInfo = "SELECT * FROM SYS.DBA_INDEXES DI WHERE DI.OWNER = ? and DI.INDEX_NAME = ?";
    private static final String SQLIndexes = "select DI.OWNER, DI.INDEX_NAME, DI.TABLE_OWNER, DI.TABLE_NAME, DI.UNIQUENESS, DI.LOGGING, DI.PARTITIONED, DI.NUM_ROWS, DI.DEGREE,\n"
            + "(select SUM(bytes)/1024/1024 SIZE_MiB from dba_segments where owner = DI.OWNER and segment_name= DI.INDEX_NAME group by owner, segment_name) SIZE_MiB\n"
            + "FROM SYS.DBA_INDEXES DI WHERE DI.TABLE_OWNER = ? and DI.TABLE_NAME = ?";
    private static final String SQLIndexColumns = " select DIC.INDEX_NAME,DIC.COLUMN_NAME, DIC.DESCEND, DTC.DATA_TYPE ||' ('|| DTC.DATA_LENGTH || ')' AS DATA_TYPE, DIC.COLUMN_POSITION \n"
            + "from DBA_IND_COLUMNS DIC, DBA_TAB_COLUMNS DTC \n"
            + "where DIC.TABLE_NAME = DTC.TABLE_NAME AND DIC.TABLE_OWNER = DTC.OWNER AND DIC.COLUMN_NAME = DTC.COLUMN_NAME\n"
            + "AND DIC.INDEX_OWNER = ? and DIC.INDEX_NAME = ? ";
    private static final String SQLTableRowCount="select count(*) from  ";


    @FXML
    private TableView<DBTask> taskTableView;
    
    @FXML
    private TextArea messageTextArea;
    
    
    @FXML
    protected void closeButtonHandle(ActionEvent event){
        close();
    }
    
    
    
    
    
    @Override
    public void close() {
        if (databaseExecutor.getActiveCount() > 0) {
            ConfirmationDialogController dialog = new ConfirmationDialogController(stage, "There is a task in running state.\nAre you sure to cancel it and all of the others?");
            if (dialog.confirm()) {
                clearQueueAndRestartExecutor();
                hide();
                stage.requestFocus();
            }
        } else {
            hide();
            stage.requestFocus();
        }
    }
    
    public void cleanUp(){
        close();
        databaseExecutor.shutdownNow();
        try {
            if (conn != null) {conn.close();}
        } catch (SQLException ex) {
            Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clearQueueAndRestartExecutor() {
        databaseExecutor.shutdownNow();
        if (conn != null) {
            try {
                conn.cancel();
            } catch (SQLException ex) {
                Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        databaseExecutor.shutdownNow();
        databaseExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, new DatabaseThreadFactory());
        //notifyTraffic();
    }

    /**
     * Only for tasks that comply traffic lights.
     */
    public synchronized void checkTrafficLight(DBTask task) {
        while (!greenlight) {
            try {
                wait();
            } catch (InterruptedException ex) {
                task.cancel();
                Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
        }
        greenlight = false;
    }

    public synchronized void notifyTraffic(){
        greenlight = true;
        notifyAll();
    }
    
    public void addDBTaskToQueue(Task task) {
        if (conn != null) {
            if (conn.isUsable()) {
                databaseExecutor.submit(task);
            }

        }
    }

    public void addConnectionTaskToQueue(Task task) {
        databaseExecutor.submit(task);
    }
    
    
    
    
    
    
    
    
    /**
     * Tasks for executing by ThreadPoolExecutor. Some of them have to obey
     * rules by calling synchronized methods checkTrafficLight() when starting
     * and notifyTraffic() on successful completion before and after erroneous
     * code respectively inside Task.call() implementation. Introduced for
     * ability to cancel many repetitive DDL or DML operations that could
     * possibly getting the same exception or other needs.
     *
     */
    
    
    
    
    
    
    public Task<Void> getDDLTask(String ddlBlock) {
        Task<Void> ddlTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                setTaskName("DDL Execute");
                setTaskMessage(ddlBlock);
                checkTrafficLight(this);
                executeDDL(ddlBlock);
                notifyTraffic();
                return null;
            }

            private void executeDDL(String ddlBlock) throws SQLException {
                String[] parts = ddlBlock.split("\\;");

                for (String part : parts) {
                    if (!isCancelled() && part.length() > 3) {
                        setCurrentStatement(part);
                        Statement stmt = conn.createStatement();
                        stmt.executeUpdate(part);
                    }
                }
            }

        };
        

        ddlTask.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {

                Throwable throwable = event.getSource().getException();
                        
                if (throwable != null) {
                    if (throwable instanceof SQLException) {
                        SQLException ex = (SQLException) throwable;
                        Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, ex);
                        ExceptionDialogController exceptionDialog = new ExceptionDialogController(ex.getMessage() + "\n", ex, stage);
                        exceptionDialog.show();
                    } else if (throwable instanceof NullPointerException) {
                        Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, throwable);
                        ExceptionDialogController exceptionDialog = new ExceptionDialogController("java.lang.NullPointerException", null, stage);
                        exceptionDialog.show();

                    } else {
                        Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, throwable);
                        ExceptionDialogController exceptionDialog = new ExceptionDialogController(throwable.getMessage(), null, stage);
                        exceptionDialog.show();
                    }
                } else {
                    System.err.println("DBTask< ? > have been failed, but throwable is null");
                }

                ConfirmationDialogController confirm = new ConfirmationDialogController(stage, "An error has occured.\n"
                        + "Are you sure to continue other statements?");
                if (confirm.confirm()) {
                    notifyTraffic();
                } else {
                    clearQueueAndRestartExecutor();
                }
            }
        });

        return ddlTask;
    }

    /**
     * Table row count
     * @return Return count of rows by supplying table name
     */
    
    public Task<Integer> getRowCountTask(String tableName) {
        Task<Integer> rowCountTask = new DBTask<Integer>() {

            @Override
            protected Integer call() throws Exception {
                taskList.add(this);
                this.setTaskName("Get Table Row Count");
                this.setTaskMessage(tableName);
                
                return fetchRowCount(tableName);
            }
                        
            private int fetchRowCount(String tableName) throws SQLException{
                int count = -1;
                Statement stmt = conn.createStatement();
                
                try (ResultSet rs = stmt.executeQuery(SQLTableRowCount + tableName);) {
                    while(rs.next()){
                        count = rs.getInt(1);
                    }
                }
                return count;
            }
        };
        return rowCountTask;
    }

    
    
    /**
     *
     * @param index
     */
    public Task<Void> getIndexInfoTask(Index index) {
        Task<Void> indexInfotask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Index Info");
                this.setTaskMessage(SQLIndexInfo);
                index.setIndexInfo(fetchIndexInfo(index.getOwner(), index.getName()));
                return null;
            }

            private ObservableList<IndexInfo> fetchIndexInfo(String schemaName, String indexName) throws SQLException {
                ObservableList<IndexInfo> indexInfos = FXCollections.observableArrayList();
                PreparedStatement pstmt = conn.prepareStatement(SQLIndexInfo);

                pstmt.setFetchSize(FETCH_SIZE_4K);
                pstmt.setString(1, schemaName);
                pstmt.setString(2, indexName);
                try (ResultSet rs = pstmt.executeQuery();) {
                    rs.setFetchSize(FETCH_SIZE_4K);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int count = rsmd.getColumnCount();

                    if (!rs.next()) {
                        return null;
                    }

                    for (int i = 1; i <= count; i++) {
                        if (rs.getString(i) != null) {
                            IndexInfo info = new IndexInfo(rsmd.getColumnName(i), rs.getString(i));
                            indexInfos.add(info);
                        }
                    }
                }
                return indexInfos;
            }
        };
        return indexInfotask;
    }
    
    /**
     *
     * @param index
     */
    public Task<Void> getIndexColumnsTask(Index index) {
        Task<Void> indexColumnsTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Index Columns");
                this.setTaskMessage(SQLIndexColumns);
                index.setIndexColumns(fetchIndexColumns(index.getOwner(), index.getName()));
                return null;
            }

            private ObservableList<IndexColumn> fetchIndexColumns(String schemaName, String indexName) throws SQLException {
                ObservableList<IndexColumn> indexColumns = FXCollections.observableArrayList();
                PreparedStatement pstmt = conn.prepareStatement(SQLIndexColumns);

                pstmt.setFetchSize(FETCH_SIZE_4K);
                pstmt.setString(1, schemaName);
                pstmt.setString(2, indexName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.setFetchSize(FETCH_SIZE_4K);
                    while (rs.next()) {
                        IndexColumn indexColumn = new IndexColumn(rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5));
                        indexColumns.add(indexColumn);
                    }
                    return indexColumns;
                }
            }
        };
        return indexColumnsTask;
    }

    /**
     * Table Indexes
     * @param table
     */
    public Task<Void> getIndexesTask(Table table) {
        Task<Void> indexesTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch List of Indexes");
                this.setTaskMessage(SQLIndexes);
                table.setIndexes(fetchIndexes(table.getTableSchema(), table.getTableName()));
                return null;
            }

            private ObservableList<Index> fetchIndexes(String schemaName, String tableName) throws SQLException {
                ObservableList<Index> indexes = FXCollections.observableArrayList();
                PreparedStatement pstmt = conn.prepareStatement(SQLIndexes);

                pstmt.setFetchSize(FETCH_SIZE_4K);
                pstmt.setString(1, schemaName);
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.setFetchSize(FETCH_SIZE_4K);
                    while (rs.next()) {
                        Index index = new Index(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                                rs.getString(7).equalsIgnoreCase("yes"), rs.getInt(8), rs.getInt(9), rs.getInt(10));
                        index.setDB(DBBridge.this);
                        indexes.add(index);
                    }
                    return indexes;
                }
            }
        };
        return indexesTask;
    }
    
    /**
     * Additional information about table size, (sub)partitioning type and so on
     * @param table 
     */

    public Task<Void> getTableInfoTask(Table table) {
        Task<Void> tableInfotask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Table Info");
                table.setTableInfo(fetchTableInfo(table.getTableSchema(), table.getTableName()));
                return null;
            }

            private ObservableList<TableInfo> fetchTableInfo(String schemaName, String tableName) throws SQLException {
                ObservableList<TableInfo> tableInfos = FXCollections.observableArrayList();
                PreparedStatement pstmt = conn.prepareStatement(SQLTablePartitioningInfo);

                pstmt.setFetchSize(FETCH_SIZE_4K);
                pstmt.setString(1, schemaName);
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery();) {
                    rs.setFetchSize(FETCH_SIZE_4K);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int count = rsmd.getColumnCount();

                    if (!rs.next()) {
                        return null;
                    }

                    for (int i = 1; i <= count; i++) {
                        if (rs.getString(i) != null) {
                            TableInfo info = new TableInfo(rsmd.getColumnName(i), rs.getString(i));
                            tableInfos.add(info);
                        }
                    }
                }
                return tableInfos;
            }
        };
        return tableInfotask;
    }

    

    
    /**
     * Get columns that table partitioned by
     * @param table 
     */   
    public Task<Void> getPartitionedColumnsTask(Table table) {
        Task<Void> partitionedColumnsTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Partitioned Columns");
                this.setTaskMessage(SQLPartitionedColumns);
                table.setPartitionedColumns(fetchPartitionedColumns(table.getTableSchema(), table.getTableName()));
                return null;
            }

            private ObservableList<PartitionedColumns> fetchPartitionedColumns(String schemaName, String tableName) throws SQLException {
                ObservableList<PartitionedColumns> partitionedColumns = FXCollections.observableArrayList();
                PreparedStatement pstmt = conn.prepareStatement(SQLPartitionedColumns);

                pstmt.setFetchSize(FETCH_SIZE_4K);
                pstmt.setString(1, schemaName);
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery();) {
                    rs.setFetchSize(FETCH_SIZE_4K);
                    while (rs.next()) {
                        PartitionedColumns pcolumn = new PartitionedColumns(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                        partitionedColumns.add(pcolumn);
                    }
                }
                return partitionedColumns;
            }
        };
        return partitionedColumnsTask;
    }

     /**
     * Get columns that table subpartitioned by
     * @param table 
     */    
    public Task<Void> getSubpartitionedColumnsTask(Table table) {
        Task<Void> subpartitionedColumnsTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Subpartitioned Columns");
                table.setSubpartitionedColumns(fetchSubpartitionedColumns(table.getTableSchema(), table.getTableName()));
                return null;
            }

            private ObservableList<SubpartitionedColumns> fetchSubpartitionedColumns(String schemaName, String tableName) throws SQLException {
                ObservableList<SubpartitionedColumns> subpartitionedColumns = FXCollections.observableArrayList();
                PreparedStatement pstmt = conn.prepareStatement(SQLSubpartitionedColumns);

                pstmt.setFetchSize(FETCH_SIZE_4K);
                pstmt.setString(1, schemaName);
                pstmt.setString(2, tableName);
                try (ResultSet rs = pstmt.executeQuery();) {
                    rs.setFetchSize(FETCH_SIZE_4K);
                    while (rs.next()) {
                        SubpartitionedColumns spcolumn = new SubpartitionedColumns(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
                        subpartitionedColumns.add(spcolumn);
                    }
                }
                return subpartitionedColumns;
            }
        };
        return subpartitionedColumnsTask;
    }
    
    
    /**
     * Get partition list
     * @param table 
     * @return  Void
     * 
     */

    public Task<Void> getPartitionsTask(Table table) {
        Task<Void> partitionsTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Partitions");
                table.setPartitions(fetchPartitions(table.getTableSchema(), table.getTableName()));
                return null;
            }

            private ObservableList<Partition> fetchPartitions(String schemaName, String tableName) throws SQLException {
                ObservableList<Partition> partitions = FXCollections.observableArrayList();

                OracleJDBCRowSet rs = new OracleJDBCRowSetKeepConnection(conn);
                rs.setFetchSize(FETCH_SIZE_4K);
                rs.setReadOnly(true);
                rs.setCommand(new PrepareSQL(SQLPartitions, schemaName, tableName).toString());
                rs.execute();
                while (rs.next()) {
                    Partition partition = new Partition(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getInt(6),
                            rs.getInt(7), rs.getString(8), rs.getInt(9), rs.getInt(10), rs.getString(11));
                    partitions.add(partition);
                }
                return partitions;
            }
        };
        return partitionsTask;
    }

    /**
     * Get subpartition list
     *
     * @param table
     */

    public Task<Void> getSubpartitionsTask(Table table) {
        Task<Void> subpartitionsTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Subpartitions");
                table.setSubpartitions(fetchPartitions(table.getTableSchema(), table.getTableName()));
                return null;
            }

            private ObservableList<Subpartition> fetchPartitions(String schemaName, String tableName) throws SQLException {
                ObservableList<Subpartition> subpartitions = FXCollections.observableArrayList();

                OracleJDBCRowSet rs = new OracleJDBCRowSetKeepConnection(conn);
                rs.setFetchSize(FETCH_SIZE_4K);
                rs.setReadOnly(true);
                rs.setCommand(new PrepareSQL(SQLSubPartitions, schemaName, tableName).toString());
                rs.execute();
                while (rs.next()) {
                    Subpartition subpartition = new Subpartition(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5), rs.getInt(6),
                            rs.getInt(7), rs.getInt(8), rs.getString(9), rs.getInt(10), rs.getString(11));
                    subpartitions.add(subpartition);
                }
                return subpartitions;
            }
        };
        return subpartitionsTask;
    }
    

    /**
     * Table and Schema initialization part
     * @return Void
     */
      
    public Task<Void> getTableListTask() {
        Task<Void> taskTableList = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                taskList.add(this);
                this.setTaskName("Fetch Table List");
                this.setTaskMessage(SQLtableList);
                partArchController.fillTableList(fetchTableList());
                return null;
            }

            private TreeMap<String, Table> fetchTableList() throws SQLException {
                TreeMap<String, Table> tableList = new TreeMap<>();
                Statement stmt = conn.createStatement();
                stmt.setFetchSize(FETCH_SIZE_4K);
                ResultSet rs = stmt.executeQuery(SQLtableList);
                rs.setFetchSize(FETCH_SIZE_4K);
                String previousSchema = null;
                while (rs.next()) {
                    if (previousSchema == null || !previousSchema.equals(rs.getString(1))) {
                        previousSchema = rs.getString(1);
                        Table table = new Table(null, rs.getString(1), null);
                        tableList.put(rs.getString(1), table);
                    }
                    Table table = new Table(rs.getString(1), rs.getString(2), rs.getString(3));
                    table.setDB(DBBridge.this);
                    tableList.put(rs.getString(1) + "." + rs.getString(2), table);
                }
                return tableList;
            }
        };
        return taskTableList;
    }
    

    
    /**
     * Oracle Connection Task
     * @param ods
     * @return 
     */  
    public Task<Void> getConnectionTask(OracleDataSource ods) {
        Task<Void> connectionTask = new DBTask<Void>() {

            @Override
            protected Void call() throws Exception {
                this.setTaskName("Get connection");
                this.setTaskMessage(ods != null ? ods.getURL() : "");
                taskList.add(this);
                conn = getConnection(ods);
                return null;
            }

            private OracleConnection getConnection(OracleDataSource ods) throws SQLException {
                OracleConnection oraConn = (OracleConnection) ods.getConnection();
                if (oraConn.isValid(60)) {
                    return oraConn;
                }
                return null;
            }
        };
        
        connectionTask.setOnSucceeded((WorkerStateEvent event) -> {
            addDBTaskToQueue(getTableListTask());
        });
        return connectionTask;
    }
    
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (taskList != null) {
            taskTableView.setItems(taskList);
        }

        taskList.addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change c) {
                if (taskList.size() > 0) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            taskTableView.scrollTo(taskList.size());
                            taskTableView.getSelectionModel().selectLast();
                        }
                    });
                }

            }
        });

        taskTableView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends DBTask> observable, DBTask oldValue, DBTask newValue) -> {
            messageTextArea.setText(newValue != null ? "" + newValue.getTaskMessage() : "");
        });

    }

    /**
     * Constructor
     * 
     * @param parent
     * @param partArchController
     */
    public DBBridge( Stage parent, PartArchController partArchController) {
        this.stage = parent;
        this.partArchController = partArchController;
        
        setTitle("Execution Status");
        initModality(Modality.APPLICATION_MODAL);

        if (parent != null) {
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);
        } else {
            System.out.println("centeronscreen");
            centerOnScreen();
        }

        this.setOnShowing(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                setX(stage.getX() + stage.getWidth() / 4);
                setY(stage.getY() + stage.getHeight() / 4);
            }
        });

        setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                event.consume();
                close();
            }
        });
        
        
        databaseExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, new DatabaseThreadFactory());

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/partarch/dialogs/ExecutionDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    
    /**
     * All Tasks inherited from this class
     *
     * @param <T>
     */
    public abstract class DBTask<T> extends Task<T> {

        DBTask() {
            setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    exceptionHandler();
                }
            });

            setOnScheduled((WorkerStateEvent event) -> {
                DBBridge.this.show();
            });

            stateProperty().addListener((ObservableValue<? extends State> observable, State oldValue, State newValue) -> {
                DBTask.this.setTaskStatus(newValue.toString());
                if (newValue == State.SCHEDULED) {
                    setStartTime(System.currentTimeMillis());
                } else if (newValue == State.SUCCEEDED || newValue == State.CANCELLED || newValue == State.FAILED) {
                    setTimeTaken(System.currentTimeMillis() - getStartTime());
                }
            });

            runningProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean wasRunning, Boolean isRunning) -> {
                if (!isRunning) {
                    if (databaseExecutor.getActiveCount() == 0) {
                        DBBridge.this.hide();
                    }
                }
            });
        }
 
        public void exceptionHandler() {
            if (getException() != null) {
                if (getException() instanceof SQLException) {
                    SQLException ex = (SQLException) getException();
                    Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, ex);
                    ExceptionDialogController exceptionDialog = new ExceptionDialogController(ex.getMessage(), ex, stage);
                    exceptionDialog.showAndWait();
                } else if (getException() instanceof NullPointerException) {
                    Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, getException());
                    ExceptionDialogController exceptionDialog = new ExceptionDialogController("java.lang.NullPointerException", null, stage);
                    exceptionDialog.showAndWait();

                } else {
                    Logger.getLogger(DBBridge.class.getName()).log(Level.SEVERE, null, getException());
                    ExceptionDialogController exceptionDialog = new ExceptionDialogController(getException().getMessage(), null, stage);
                    exceptionDialog.showAndWait();
                }
            } else {
                System.err.println("DBTask< ? > have been failed, but throwable is null");
            }

        }

        private SimpleLongProperty startTime = new SimpleLongProperty(0);
        private SimpleLongProperty timeTaken = new SimpleLongProperty(0);
        private SimpleObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<LocalDateTime>(LocalDateTime.now());
        private SimpleStringProperty taskName = new SimpleStringProperty("unnamed_task");
        private SimpleStringProperty taskMessage = new SimpleStringProperty("no_message");
        private SimpleStringProperty taskStatus = new SimpleStringProperty("unnamed_status");
        private SimpleStringProperty currentStatement = new SimpleStringProperty("unnamed_statement");

        public SimpleLongProperty startTimeProperty() {
            return this.startTime;
        }

        public long getStartTime() {
            return this.startTimeProperty().get();
        }

        public void setStartTime(final long startTime) {
            this.startTimeProperty().set(startTime);
        }

        public SimpleLongProperty timeTakenProperty() {
            return this.timeTaken;
        }

        public long getTimeTaken() {
            return this.timeTakenProperty().get();
        }

        public void setTimeTaken(final long timeTaken) {
            this.timeTakenProperty().set(timeTaken);
        }

        public SimpleObjectProperty dateTimeProperty() {
            return this.dateTime;
        }

        public Object getDateTime() {
            return this.dateTimeProperty().get();
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTimeProperty().set(dateTime);
        }

        public SimpleStringProperty taskNameProperty() {
            return this.taskName;
        }

        public java.lang.String getTaskName() {
            return this.taskNameProperty().get();
        }

        public void setTaskName(final java.lang.String taskName) {
            this.taskNameProperty().set(taskName);
        }

        public SimpleStringProperty taskMessageProperty() {
            return this.taskMessage;
        }

        public java.lang.String getTaskMessage() {
            return this.taskMessageProperty().get();
        }

        public void setTaskMessage(final java.lang.String taskMessage) {
            this.taskMessageProperty().set(taskMessage);
        }

        public SimpleStringProperty taskStatusProperty() {
            return this.taskStatus;
        }

        public java.lang.String getTaskStatus() {
            return this.taskStatusProperty().get();
        }

        public void setTaskStatus(final java.lang.String taskStatus) {
            this.taskStatusProperty().set(taskStatus);
        }

        public SimpleStringProperty currentStatementProperty() {
            return this.currentStatement;
        }

        public java.lang.String getCurrentStatement() {
            return this.currentStatementProperty().get();
        }

        public void setCurrentStatement(final java.lang.String currentStatement) {
            this.currentStatementProperty().set(currentStatement);
        }
    }


    
    

    /**
     * ThreadFactory class
     */
    private static class DatabaseThreadFactory implements ThreadFactory {

        static final AtomicInteger poolNumber = new AtomicInteger(1);
        static final ThreadGroup group = new ThreadGroup("database group");
        

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(group, runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
            thread.setDaemon(true);

            return thread;
        }
    }

}
