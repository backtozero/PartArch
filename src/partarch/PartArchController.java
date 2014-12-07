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
package partarch;

import partarch.model.Table;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import oracle.jdbc.pool.OracleDataSource;
import partarch.db.DBBridge;
import partarch.dialogs.AboutDialogController;
import partarch.dialogs.ExchangePartitionController;
import partarch.dialogs.LoginDialogController;
import partarch.model.Index;
import partarch.model.IndexColumn;
import partarch.model.IndexInfo;
import partarch.model.Partition;
import partarch.model.PartitionedColumns;
import partarch.model.Subpartition;
import partarch.model.SubpartitionedColumns;
import partarch.model.TableInfo;
import partarch.util.ClipboardTool;

/**
 * Main 
 * @author <gotozero@yandex.com>
 */
public class PartArchController extends BorderPane implements Initializable {
    
    private final ClipboardTool ct = new ClipboardTool();
    private Stage stage;
    private static DBBridge db;
    final TreeItem<Table> rootTreeItem = new TreeItem<>();
    Table currentTable;
    
    public TreeMap<String,Table> tableList;
    
    
    private final Image tableIcon = new Image(getClass().getResourceAsStream("icons/table.png"), 16, 16, true, true);
    private final Image tablePartitionIcon = new Image(getClass().getResourceAsStream("icons/partition.png"), 16, 16, true, true);
    private final Image tableIotIcon = new Image(getClass().getResourceAsStream("icons/table_iot.png"), 16, 16, true, true);
    private final Image schemaIcon = new Image(getClass().getResourceAsStream("icons/schema.png"), 16, 16, true, true);
    
    
    /**
     * GUI elements and handlers.
     */
    @FXML
    private TreeTableView<Table> tableSchemaTreeTableView;
    @FXML
    private TreeTableColumn<Table,String> tableNameTreeTableColumn;
    @FXML
    private TreeTableColumn<Table, String> schemaNameTreeTableColumn;
    @FXML
    private TableView<PartitionedColumns> partitionedColumnsTableView;
    @FXML
    private TableView<Partition> partitionsTableView;
    @FXML
    private TableView<TableInfo> tableInfoTableView;
    @FXML
    private TableView<Subpartition> subpartitionsTableView;
    @FXML
    private TableView<SubpartitionedColumns> subpartitionedColumnsTableView;
    @FXML
    private TableView<Index> indexesTableView;
    @FXML
    private TableView<IndexColumn> indexColumnsTableView;
    @FXML
    private TableView<IndexInfo> indexInfoTableView;
    @FXML
    private Button openConnectionButton;
    @FXML
    private SplitPane mainSplitPane;

    
    @FXML
    private void handleAboutDialog(ActionEvent event) {
        AboutDialogController adc = new AboutDialogController(stage, ct);
        adc.show();
    }
    
    @FXML
    protected void handleMenuItemClose(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    protected void onRefreshTableListButton(ActionEvent event) {
        getTableList();
    }

    @FXML
    protected void handleOpenConnectionButton(ActionEvent event) {
        getConnection();
    }
    @FXML
    protected void onRefreshTableButton(ActionEvent event) {
        setTableData(true);
    }
    @FXML
    protected void onExchangeButton(ActionEvent event) {
        if (currentTable != null) {
            ExchangePartitionController exchange = new ExchangePartitionController(stage, currentTable, tableList, db, ct);
            exchange.show();
        }
    }
    @FXML
    protected void showTasksMenuItem(ActionEvent event){
        if (db != null) {
            db.showAndWait();
        }
    }

    
    
    
    


    /**
     * Open connection dialog that will return DataSource.
     */
    public void getConnection() {
        LoginDialogController loginDialog = new LoginDialogController(stage);
        OracleDataSource ods = loginDialog.create();
        if (ods != null) {
            db.addConnectionTaskToQueue(db.getConnectionTask(ods));
        }
    }
    
    public void getTableList(){
        db.addDBTaskToQueue(db.getTableListTask());
    }
    
    public void cleanUp() {
        db.cleanUp();
    }
        
    /**
     * Initial fill TreeTableView.
     * @param tableList
     */
    public void fillTableList(TreeMap<String, Table> tableList) {
        this.tableList = tableList;
        if (this.tableList != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    rootTreeItem.getChildren().clear();
                    TreeItem<Table> previousItem = null;
                    for (Map.Entry<String, Table> table : tableList.entrySet()) {
                        TreeItem<Table> item;
                        if (table.getValue().getTableType() == null) {
                            item = new TreeItem<>(table.getValue(), new ImageView(schemaIcon));
                        } else if (table.getValue().getTableType().equalsIgnoreCase("H")) {
                            item = new TreeItem<>(table.getValue(), new ImageView(tableIcon));
                        } else if (table.getValue().getTableType().equalsIgnoreCase("P")) {
                            item = new TreeItem<>(table.getValue(), new ImageView(tablePartitionIcon));
                        } else if (table.getValue().getTableType().equalsIgnoreCase("I")) {
                            item = new TreeItem<>(table.getValue(), new ImageView(tableIotIcon));
                        } else {
                            item = new TreeItem<>(table.getValue(), new ImageView(tableIcon));
                        }

                        if (item.getValue().getTableSchema() == null || previousItem == null) {
                            previousItem = item;
                            rootTreeItem.getChildren().add(item);
                        } else {
                            previousItem.getChildren().add(item);
                        }
                    }
                 tableSchemaTreeTableView.getSelectionModel().select(rootTreeItem.nextSibling());   
                }
            });
        }
    }

    public void setTableData(boolean refresh) {
        if (currentTable != null) {
            indexesTableView.getSelectionModel().clearSelection();
            
            partitionsTableView.setItems(currentTable.getPartitions(refresh));
            partitionedColumnsTableView.setItems(currentTable.getPartitionedColumns(refresh));
            tableInfoTableView.setItems(currentTable.getTableInfo(refresh));
            subpartitionsTableView.setItems(currentTable.getSubpartitions(refresh));
            subpartitionedColumnsTableView.setItems(currentTable.getSubpartitionedColumns(refresh));
            indexesTableView.setItems(currentTable.getIndexes(refresh));
            
        }
    }
    
    /**
     * Initializes the controller class.
     * @param stage
     */    
    public void setStage(Stage stage) {
        this.stage = stage;
        this.db = new DBBridge(stage, this);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        tableSchemaTreeTableView.setRoot(rootTreeItem);
        tableSchemaTreeTableView.showRootProperty().setValue(Boolean.FALSE);
        
        // Cannot be done in FXML because of some reason.
        tableNameTreeTableColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Table, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getTableName()));
        schemaNameTreeTableColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<Table, String> param)
                -> new ReadOnlyStringWrapper(param.getValue().getValue().getTableSchema()));
        
        // Set OnSelect Listener for TreeTableView.
        tableSchemaTreeTableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (tableSchemaTreeTableView.getSelectionModel().getSelectedItem() != null) {

                if (tableSchemaTreeTableView.getSelectionModel().getSelectedItem().getValue() != null
                        && tableSchemaTreeTableView.getSelectionModel().getSelectedItem().getValue().getTableType() != null) {
                    currentTable = tableSchemaTreeTableView.getSelectionModel().getSelectedItem().getValue();
                    setTableData(false);
                }

                indexesTableView.getSelectionModel().clearSelection();
                indexColumnsTableView.setItems(null);
                indexInfoTableView.setItems(null);
                indexesTableView.getSelectionModel().clearSelection();

            }
        });

        indexesTableView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Index> observable, Index oldValue, Index newValue) -> {
            if (newValue != null) {
                indexInfoTableView.setItems(newValue.getIndexInfo(false));
                indexColumnsTableView.setItems(newValue.getIndexColumns(false));
            } else {
                indexColumnsTableView.setItems(null);
                indexInfoTableView.setItems(null);
            }
        });

    }

}
