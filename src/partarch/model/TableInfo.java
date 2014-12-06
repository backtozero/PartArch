/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partarch.model;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author jesus
 */
public class TableInfo {

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

    public TableInfo(String parameter, String value) {
        this.parameter = new SimpleStringProperty(parameter);
        this.value = new SimpleStringProperty(value);
    }
    
    

}
