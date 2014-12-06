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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class ComboBoxAutoComplete<T> extends ComboBox<T> {

    private boolean moveCaretToPos = false;
    private int caretPos;
    private ObservableList<T> data;

    public ComboBoxAutoComplete() {
        setOnKeyReleased((KeyEvent event) -> {

            if (data == null) {
                data = getItems();
            }

            if (event.getCode() == KeyCode.UP) {
                caretPos = -1;
                moveCaret(getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.DOWN) {
                if (!isShowing()) {
                    show();
                }
                caretPos = -1;
                moveCaret(getEditor().getText().length());
                return;
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                moveCaretToPos = true;
                caretPos = getEditor().getCaretPosition();
            } else if (event.getCode() == KeyCode.DELETE) {
                moveCaretToPos = true;
                caretPos = getEditor().getCaretPosition();
            }

            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
                    || event.isControlDown() || event.getCode() == KeyCode.HOME
                    || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
                return;
            }

            ObservableList list = FXCollections.observableArrayList();
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).toString().toLowerCase().startsWith(
                        getEditor().getText().toLowerCase())) {
                    list.add(data.get(i));
                }
            }
            java.lang.String t = getEditor().getText();

            setItems(list);
            getEditor().setText(t);
            if (!moveCaretToPos) {
                caretPos = -1;
            }
            moveCaret(t.length());
            if (!list.isEmpty()) {
                show();
            }

        });
    }

    private void moveCaret(int textLength) {
        if (caretPos == -1) {
            getEditor().positionCaret(textLength);
        } else {
            getEditor().positionCaret(caretPos);
        }
        moveCaretToPos = false;
    }

}
