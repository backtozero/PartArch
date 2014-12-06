/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package partarch.dialogs;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author jesus
 */
public class ExceptionDialogController extends Stage implements Initializable {

    private SQLException exception;
    private String shortMessage;

    @FXML
    private Button okButton;
    @FXML
    private TextArea longMessageTextArea;
    @FXML
    private Label shortMessageLabel;

    @FXML
    void onOkButton(ActionEvent event) {
        close();
    }


    private void handleException() {
        
        shortMessageLabel.setText(shortMessage);
        
        int errCount = 0;
        String longMessage = "";
        if (exception != null) {
            while (exception != null) {
                longMessage += errCount + ": SQLState: " + exception.getSQLState();
                longMessage += "\n";
                longMessage += errCount + ": Message: " + exception.getMessage();
                longMessage += "\n";
                longMessage += errCount + ": Error Code: " + exception.getErrorCode();
                longMessage += "\n";
                errCount++;
                exception = exception.getNextException();
            }
            longMessageTextArea.setText(longMessage);
        } else {
            longMessageTextArea.setDisable(true);
        }

    }
    
    
    
    
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        handleException();
    }

    public ExceptionDialogController(String shortMessage, SQLException exception, Stage parent) {
        setTitle("Exception");
        setAlwaysOnTop(true);
        initModality(Modality.APPLICATION_MODAL);

        if (parent != null) {
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);
        }

        this.exception = exception;
        this.shortMessage = shortMessage;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ExceptionDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
