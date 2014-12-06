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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class IndexColumn {

    private SimpleStringProperty name;
    private SimpleStringProperty descend;
    private SimpleStringProperty dataType;
    private SimpleIntegerProperty columnPosition;

    public SimpleStringProperty nameProperty() {
        return this.name;
    }

    public java.lang.String getName() {
        return this.nameProperty().get();
    }

    public void setName(final java.lang.String name) {
        this.nameProperty().set(name);
    }

    public SimpleStringProperty descendProperty() {
        return this.descend;
    }

    public java.lang.String getDescend() {
        return this.descendProperty().get();
    }

    public void setDescend(final java.lang.String descend) {
        this.descendProperty().set(descend);
    }

    public SimpleStringProperty dataTypeProperty() {
        return this.dataType;
    }

    public java.lang.String getDataType() {
        return this.dataTypeProperty().get();
    }

    public void setDataType(final java.lang.String dataType) {
        this.dataTypeProperty().set(dataType);
    }

    public SimpleIntegerProperty columnPositionProperty() {
        return this.columnPosition;
    }

    public int getColumnPosition() {
        return this.columnPositionProperty().get();
    }

    public void setColumnPosition(final int columnPosition) {
        this.columnPositionProperty().set(columnPosition);
    }

    public IndexColumn(String name, String descend, String dataType, int columnPosition) {
        this.name = new SimpleStringProperty(name);
        this.descend = new SimpleStringProperty(descend);
        this.dataType = new SimpleStringProperty(dataType);
        this.columnPosition = new SimpleIntegerProperty(columnPosition);
    }
    
    

}
