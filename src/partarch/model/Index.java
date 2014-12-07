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

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import partarch.db.DBBridge;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class Index {
    
    private DBBridge db;    
    
    private boolean indexColumnsExecuted = false;
    private ObservableList<IndexColumn> indexColumns = FXCollections.observableArrayList();
    private boolean indexInfosExecuted = false;
    private ObservableList<IndexInfo> indexInfo = FXCollections.observableArrayList();    

    private SimpleStringProperty owner;
    private SimpleStringProperty name;
    private SimpleStringProperty tableOwner;
    private SimpleStringProperty tableName;
    private SimpleStringProperty uniqueness;
    private SimpleStringProperty logging;
    private SimpleBooleanProperty partitioned;
    private SimpleIntegerProperty numRows;    
    private SimpleIntegerProperty degree;
    private SimpleIntegerProperty size;

    public SimpleStringProperty ownerProperty() {
        return this.owner;
    }

    public java.lang.String getOwner() {
        return this.ownerProperty().get();
    }

    public void setOwner(final java.lang.String owner) {
        this.ownerProperty().set(owner);
    }

    public SimpleStringProperty nameProperty() {
        return this.name;
    }

    public java.lang.String getName() {
        return this.nameProperty().get();
    }

    public void setName(final java.lang.String name) {
        this.nameProperty().set(name);
    }

    public SimpleStringProperty tableOwnerProperty() {
        return this.tableOwner;
    }

    public java.lang.String getTableOwner() {
        return this.tableOwnerProperty().get();
    }

    public void setTableOwner(final java.lang.String tableOwner) {
        this.tableOwnerProperty().set(tableOwner);
    }

    public SimpleStringProperty tableNameProperty() {
        return this.tableName;
    }

    public java.lang.String getTableName() {
        return this.tableNameProperty().get();
    }

    public void setTableName(final java.lang.String tableName) {
        this.tableNameProperty().set(tableName);
    }

    public SimpleStringProperty uniquenessProperty() {
        return this.uniqueness;
    }

    public java.lang.String getUniqueness() {
        return this.uniquenessProperty().get();
    }

    public void setUniqueness(final java.lang.String uniqueness) {
        this.uniquenessProperty().set(uniqueness);
    }

    public SimpleStringProperty loggingProperty() {
        return this.logging;
    }

    public java.lang.String getLogging() {
        return this.loggingProperty().get();
    }

    public void setLogging(final java.lang.String logging) {
        this.loggingProperty().set(logging);
    }

    public SimpleBooleanProperty partitionedProperty() {
        return this.partitioned;
    }

    public boolean isPartitioned() {
        return this.partitionedProperty().get();
    }

    public void setPartitioned(final boolean partitioned) {
        this.partitionedProperty().set(partitioned);
    }

    public SimpleIntegerProperty numRowsProperty() {
        return this.numRows;
    }

    public int getNumRows() {
        return this.numRowsProperty().get();
    }

    public void setNumRows(final int numRows) {
        this.numRowsProperty().set(numRows);
    }

    public SimpleIntegerProperty sizeProperty() {
        return this.size;
    }

    public int getSize() {
        return this.sizeProperty().get();
    }

    public void setSize(final int size) {
        this.sizeProperty().set(size);
    }

    public SimpleIntegerProperty degreeProperty() {
        return this.degree;
    }

    public int getDegree() {
        return this.degreeProperty().get();
    }

    public void setDegree(final int degree) {
        this.degreeProperty().set(degree);
    }

    public Index(String owner, String name, String tableOwner, String tableName, String uniqueness, String logging, boolean partitioned, int numRows, int degree, int size) {
        this.owner = new SimpleStringProperty(owner);
        this.name = new SimpleStringProperty(name);
        this.tableOwner = new SimpleStringProperty(tableOwner);
        this.tableName = new SimpleStringProperty(tableName);
        this.uniqueness = new SimpleStringProperty(uniqueness);
        this.logging = new SimpleStringProperty(logging);
        this.partitioned = new SimpleBooleanProperty(partitioned);
        this.numRows = new SimpleIntegerProperty(numRows);
        this.size = new SimpleIntegerProperty(size);
        this.degree = new SimpleIntegerProperty(degree);
    }

    /**
     *
     * @param db
     */
    public void setDB(DBBridge db) {
        this.db = db;
    }

    
    
    
    
    
    
    
    
    
    /**
     * @return indexColumns
     */
    public ObservableList<IndexColumn> getIndexColumns(boolean refresh) {
        if (this.indexColumns.size() == 0 && this.indexColumnsExecuted == false) {
            db.addDBTaskToQueue(db.getIndexColumnsTask(this));
            indexColumnsExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getIndexColumnsTask(this));
            indexColumnsExecuted = true;
        }
        return indexColumns;
    }

    /**
     * @return indexInfo
     */
    public ObservableList<IndexInfo> getIndexInfo(boolean refresh) {
        if (this.indexInfo.size() == 0 && this.indexInfosExecuted == false) {
            db.addDBTaskToQueue(db.getIndexInfoTask(this));
            indexInfosExecuted = true;
        } else if (refresh) {
            db.addDBTaskToQueue(db.getIndexInfoTask(this));
            indexInfosExecuted = true;
        }
        return indexInfo;
    }    
    
    
    /**
     * @param IndexColumn
     */
    public void setIndexColumns(ObservableList<IndexColumn> indexColumns) {
        if (indexColumns != null) {
            this.indexColumns.clear();
            this.indexColumns.addAll(indexColumns);
        }
    }

    /**
     * @param IndexInfo
     */
    public void setIndexInfo(ObservableList<IndexInfo> indexInfo) {
        if (indexInfo != null) {
            this.indexInfo.clear();
            this.indexInfo.addAll(indexInfo);
        }
    }

    

}
