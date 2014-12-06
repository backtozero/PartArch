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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class CodeEditor extends AnchorPane {

    private String currenText;

    @FXML
    private WebView webview;

    public void setText(String newCode) {
        newCode = newCode.replace("'", "\\'");
        newCode = newCode.replace(System.getProperty("line.separator"), "\\n");
        newCode = newCode.replace("\n", "\\n");
        newCode = newCode.replace("\r", "\\n");
        this.currenText = newCode;
        
        Worker w = webview.getEngine().getLoadWorker();
        SimpleBooleanProperty b = new SimpleBooleanProperty(false);
        b.bind(w.runningProperty());
        
        // If Page still loading wait until finish before calling script
        Thread t = new Thread(() -> {
            while (b.getValue()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CodeEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    webview.getEngine().executeScript("editor.setValue('" + currenText + "');");
                }
            });

        });
        t.start();

    }

    public String getText() {
        this.currenText = (String) webview.getEngine().executeScript("editor.getValue();");
        return currenText;
    }

    public void undoEdit() {
        webview.getEngine().executeScript("editor.undo();");
    }

    /**
     * Constructor
     *
     */
    public CodeEditor() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/partarch/fx/CodeEditor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            Logger.getLogger(ComboBoxFilter.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }

        webview.setPrefSize(650, 325);
        webview.setMinSize(650, 325);
        webview.getEngine().load(this.getClass().getResource("/partarch/js/codemirror/index.html").toExternalForm());
    }
}
