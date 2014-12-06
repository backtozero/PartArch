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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author <gotozero@yandex.com>
 */
public class MessageDialogController extends Stage implements Initializable {
    
    private Stage stage;
    private String message;
    
    @FXML
    private Label messageLabel;
    
    
    @FXML
    protected void closeButton(ActionEvent event){
        close();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messageLabel.setText(message);
    }

    public MessageDialogController(Stage parent, String message) {
        setTitle("Message");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        
        this.message = message;

        if (parent != null) {
            this.stage = parent;
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);

        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MessageDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
}
