/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partarch.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author gotozero@yandex.com
 */
public class SubpartitionedColumns {

    private SimpleIntegerProperty position;
    private SimpleStringProperty columnName;
    private SimpleStringProperty columnType;
    private SimpleIntegerProperty tablePostition;

    public SimpleIntegerProperty positionProperty() {
        return this.position;
    }

    public int getPosition() {
        return this.positionProperty().get();
    }

    public void setPosition(final int position) {
        this.positionProperty().set(position);
    }

    public SimpleStringProperty columnNameProperty() {
        return this.columnName;
    }

    public java.lang.String getColumnName() {
        return this.columnNameProperty().get();
    }

    public void setColumnName(final java.lang.String columnName) {
        this.columnNameProperty().set(columnName);
    }

    public SimpleStringProperty columnTypeProperty() {
        return this.columnType;
    }

    public java.lang.String getColumnType() {
        return this.columnTypeProperty().get();
    }

    public void setColumnType(final java.lang.String columnType) {
        this.columnTypeProperty().set(columnType);
    }

    public SimpleIntegerProperty tablePostitionProperty() {
        return this.tablePostition;
    }

    public int getTablePostition() {
        return this.tablePostitionProperty().get();
    }

    public void setTablePostition(final int tablePostition) {
        this.tablePostitionProperty().set(tablePostition);
    }

    public SubpartitionedColumns(Integer position, String columnName, String columnType, Integer tablePostition) {
        this.position = new SimpleIntegerProperty(position);
        this.columnName = new SimpleStringProperty(columnName);
        this.columnType = new SimpleStringProperty(columnType);
        this.tablePostition = new SimpleIntegerProperty(tablePostition);
    }

}
