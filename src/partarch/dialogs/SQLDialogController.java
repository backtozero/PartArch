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
package partarch.dialogs;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import partarch.db.DBBridge;
import partarch.fx.CodeEditor;
import partarch.util.ClipboardTool;

/**
 * FXML Controller class
 *
 * @author <gotozero@yandex.com>
 */
public class SQLDialogController extends Stage implements Initializable {
    
    private Stage stage;
    private DBBridge db;
    private final ClipboardTool ct;
    private String sql;    
    private List<String> sqlBlocks;
    
    @FXML
    private CodeEditor sqlEditorCodeEditor;
    @FXML
    private ListView sqlListView;
    
    
    @FXML
    protected void closeButton(ActionEvent event) {
        close();
    }
    
    @FXML
    protected void copyToClipBoard(ActionEvent event){ 
        ct.putToClipboard(sqlEditorCodeEditor.getText());
    }
    @FXML
    protected void executeButton(ActionEvent event){
        if (sqlBlocks != null){
            sqlBlocks.stream().forEach((item) -> {
                db.addDBTaskToQueue(db.getDDLTask(item));
            });
        }
    }
    
    
    
    public void setSQLText(String sql) {
        this.sql = sql;
        sqlEditorCodeEditor.setText(sql);
    }

    public void setSQLBlocks(List<String> sqlBlocks) {
        this.sqlBlocks = sqlBlocks;
        StringBuilder buffer = new StringBuilder();
        sqlBlocks.stream().forEach((item) -> {buffer.append(item);});
        sqlEditorCodeEditor.setText(buffer.toString());
    }
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setOnShown((WindowEvent event) -> {
            
        });
    }

    public SQLDialogController(Stage parent, DBBridge db, ClipboardTool ct) {
        setTitle("SQL");
        this.stage = parent;
        this.db = db;
        this.ct = ct;
        
        initModality(Modality.APPLICATION_MODAL);

        if (parent != null) {
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SQLDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
