/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * @author gotozero@yandex.com
 */
public class ConfirmationDialogController extends Stage implements Initializable {

    private String message;
    private boolean response = false;
    
    @FXML
    private Label messageLabel;
    
    
    @FXML
    protected void yesButtonHandle(ActionEvent event){
        response = true;
        this.close();
    }
    
    @FXML
    protected void noButtonHandle(ActionEvent event) {
        response = false;
        this.close();
    }
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (message != null) {
            messageLabel.setText(message);
        }
    }

    public ConfirmationDialogController(Stage parent, String message) {
        setTitle("Confirm");
        setAlwaysOnTop(true);
        initModality(Modality.APPLICATION_MODAL);
        
        this.message = message;

        if (parent != null) {
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConfirmationDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public boolean confirm() {
        this.showAndWait();
        return response;
    }

}
