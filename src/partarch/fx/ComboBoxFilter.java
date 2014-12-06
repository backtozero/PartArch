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
package partarch.fx;

import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author <gotozero@yandex.com>
 */

public class ComboBoxFilter<T> extends ComboBox<T> {

    private FilteredList<T> filteredList;
    private String previousString;
    private int previousCaretPos;

    public ComboBoxFilter() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/partarch/fx/ComboBoxFilter.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(ComboBoxFilter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode() == KeyCode.UP) {
                moveCaret(getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.DOWN) {
                if (!isShowing()) {
                    show();                    
                }
                moveCaret(getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.END || event.getCode() == KeyCode.PAGE_DOWN || event.getCode() == KeyCode.PAGE_UP) {
                moveCaret(getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.HOME) {
                moveCaret(0);
                return;
            }
            
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                    || event.isControlDown() || event.getCode() == KeyCode.TAB) {
                return;
            }
            
            setPredicate();
        });

    }

    public void setData(ObservableList<T> filteredList) {
        this.filteredList = new FilteredList(filteredList, p -> true);
        super.setItems(this.filteredList);
    }

    private void moveCaret(int textLength) {
        getEditor().positionCaret(textLength);
    }

    private void setPrevious() {
        previousString = getEditor().getText();
        previousCaretPos = getEditor().getCaretPosition();
    }

    private void getPrevious() {
        getEditor().setText(previousString);
        getEditor().positionCaret(previousCaretPos);
    }

    private void setPredicate() {
        setPrevious();
        filteredList.setPredicate((T t) -> {
            if (getEditor().getText().length() == 0) {
                return true;
            }
            String lowerCaseFilter = getEditor().getText().toLowerCase();
            String value;
            if (t instanceof java.lang.String) {
                value = (java.lang.String) t;
                if (value.toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
            }
            return false;
        });
        getPrevious();
    }

}
