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
package partarch.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import partarch.db.GetDataFromDB;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class Table {
    
    private GetDataFromDB db;
    
    private boolean partitionedColumnsExecuted = false;
    private ObservableList<PartitionedColumns> partitionedColumns = FXCollections.observableArrayList();
    private boolean subpartitionedColumnsExecuted = false;
    private ObservableList<SubpartitionedColumns> subpartitionedColumns = FXCollections.observableArrayList();
    private boolean partitionsExecuted = false;
    private ObservableList<Partition> partitions = FXCollections.observableArrayList();
    private boolean subpartitionsExecuted = false;
    private ObservableList<Subpartition> subpartitions = FXCollections.observableArrayList();
    private boolean tableInfosExecuted = false;
    private ObservableList<TableInfo> tableInfo = FXCollections.observableArrayList();
    private boolean indexesExecuted = false;
    private ObservableList<Index> indexes = FXCollections.observableArrayList();
    

    /**
     * Main variables about table
     */
    private SimpleStringProperty tableName;
    private SimpleStringProperty tableType;
    private SimpleStringProperty tableSchema;

    public SimpleStringProperty tableNameProperty() {
        if (tableName == null) {
            tableName = new SimpleStringProperty(this, null);
        }
        return tableName;
    }

    public SimpleStringProperty tableTypeProperty() {
        if (tableType == null) {
            tableType = new SimpleStringProperty(this, null);
        }
        return tableType;
    }

    public SimpleStringProperty tableSchemaProperty() {
        if (tableSchema == null) {
            tableSchema = new SimpleStringProperty(this, null);
        }
        return tableSchema;
    }

    public String getTableName() {
        return tableName.get();
    }

    public void setTableName(String value) {
        tableName.set(value);
    }


    public String getTableType() {
        return tableType.get();
    }

    public void setTableType(String value) {
        tableType.set(value);
    }

    public String getTableSchema() {
        return tableSchema.get();
    }

    public void setTableSchema(String value) {
        tableSchema.set(value);
    }

    /**
     * Constructor and setDB
     * 
     * @param tableSchema
     * @param tableName
     * @param tableType
     */
    public Table(String tableSchema, String tableName, String tableType) {
        this.tableName = new SimpleStringProperty(tableName);
        this.tableSchema = new SimpleStringProperty(tableSchema);
        this.tableType = new SimpleStringProperty(tableType);
    }

    public void setDB(GetDataFromDB db) {
        this.db = db;
    }


    
    
    
    
    
    
    /**
     * Other Table properties lists
     * 
     * 
     * 
     * @return indexes
     */
    public ObservableList<Index> getIndexes(boolean refresh) {
        if (this.indexes.size() == 0 && this.indexesExecuted == false) {
            db.addDBTaskToQueue(db.getIndexesTask(this));
            indexesExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getIndexesTask(this));
            indexesExecuted = true;
        }
        return indexes;
    }
    
    /**
     * @return partitionedColumns
     */
    public ObservableList<PartitionedColumns> getPartitionedColumns(boolean refresh) {
        if (this.partitionedColumns.size() == 0 && this.partitionedColumnsExecuted == false) {
            db.addDBTaskToQueue(db.getPartitionedColumnsTask(this));
            partitionedColumnsExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getPartitionedColumnsTask(this));
            partitionedColumnsExecuted = true;
        }
        return partitionedColumns;
    }
    
    /**
     * @return subpartitionedColumns
     */
    public ObservableList<SubpartitionedColumns> getSubpartitionedColumns(boolean refresh) {
        if (this.subpartitionedColumns.size() == 0 && this.subpartitionedColumnsExecuted == false) {
            db.addDBTaskToQueue(db.getSubpartitionedColumnsTask(this));
            subpartitionedColumnsExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getSubpartitionedColumnsTask(this));
            subpartitionedColumnsExecuted = true;
        }
        return subpartitionedColumns;
    }

    /**
     * @param refresh If true fetch new data
     * @return Table partitions
     */
    public ObservableList<Partition> getPartitions(boolean refresh) {
        if (this.partitions.size() == 0 && this.partitionsExecuted == false) {
            //db.executeGetPartitionsTask(this);
            db.addDBTaskToQueue(db.getPartitionsTask(this));
            partitionsExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getPartitionsTask(this));
            partitionsExecuted = true;
        }
        return partitions;
    }

    /**
     * @return subpartitions
     */
    public ObservableList<Subpartition> getSubpartitions(boolean refresh) {
        if (this.subpartitions.size() == 0 && this.subpartitionsExecuted == false) {
            db.addDBTaskToQueue(db.getSubpartitionsTask(this));
            subpartitionsExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getSubpartitionsTask(this));
            subpartitionsExecuted = true;
        }
        return subpartitions;
    }

    /**
     * @return tableInfo
     */
    public ObservableList<TableInfo> getTableInfo(boolean refresh) {
        if (this.tableInfo.size() == 0 && this.tableInfosExecuted == false) {
            db.addDBTaskToQueue(db.getTableInfoTask(this));
            tableInfosExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getTableInfoTask(this));
            tableInfosExecuted = true;
        }
        return tableInfo;
    }

    /**
     * @param partitionedColumns
     */
    public void setPartitionedColumns(ObservableList<PartitionedColumns> partitionedColumns) {
        if (partitionedColumns != null) {
            this.partitionedColumns.clear();
            this.partitionedColumns.addAll(partitionedColumns);
        }
    }

    /**
     * @param partitionedColumns
     */
    public void setSubpartitionedColumns(ObservableList<SubpartitionedColumns> subpartitionedColumns) {
        if (subpartitionedColumns != null) {
            this.subpartitionedColumns.clear();
            this.subpartitionedColumns.addAll(subpartitionedColumns);
        }
    }

    /**
     * @param partitions
     */
    public void setPartitions(ObservableList<Partition> partitions) {
        if (partitions != null) {
            this.partitions.clear();
            this.partitions.addAll(partitions);
        }
    }

    /**
     * @param subpartitions
     */
    public void setSubpartitions(ObservableList<Subpartition> subpartitions) {
        if (subpartitions != null) {
            this.subpartitions.clear();
            this.subpartitions.addAll(subpartitions);
        }
    }

    /**
     * @param tableInfo
     */
    public void setTableInfo(ObservableList<TableInfo> tableInfo) {
        if (tableInfo != null) {
            this.tableInfo.clear();
            this.tableInfo.addAll(tableInfo);
        }
    }
    
    /**
     * @param indexes
     */
    public void setIndexes(ObservableList<Index> indexes) {
        if (indexes != null) {
            this.indexes.clear();
            this.indexes.addAll(indexes);
        }
    }    
    
    
}
