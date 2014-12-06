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

/**
 *
 * @author <gotozero@yandex.com>
 */
public class IndexInfo {

    private SimpleStringProperty parameter;
    private SimpleStringProperty value;

    public SimpleStringProperty parameterProperty() {
        return this.parameter;
    }

    public java.lang.String getParameter() {
        return this.parameterProperty().get();
    }

    public void setParameter(final java.lang.String parameter) {
        this.parameterProperty().set(parameter);
    }

    public SimpleStringProperty valueProperty() {
        return this.value;
    }

    public java.lang.String getValue() {
        return this.valueProperty().get();
    }

    public void setValue(final java.lang.String value) {
        this.valueProperty().set(value);
    }

    public IndexInfo(String parameter, String value) {
        this.parameter = new SimpleStringProperty(parameter);
        this.value = new SimpleStringProperty(value);
    }

}
