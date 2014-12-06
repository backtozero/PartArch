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
import javafx.scene.control.Button;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import partarch.util.ClipboardTool;

/**
 * FXML Controller class
 *
 * @author jesus
 */
public class AboutDialogController extends Stage implements Initializable {
    
    private final ClipboardTool ct;
    
    
    @FXML
    private Button closeButton;

    
    
    @FXML
    protected void closeButton(ActionEvent event) {
        close();
    }

    @FXML
    protected void emailHyperLink() {
        ct.putToClipboard("gotozero@yandex.com");
    }

    public AboutDialogController(Stage parent, ClipboardTool ct) {
        setTitle("About");
        setAlwaysOnTop(true);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        
        this.ct = ct;

        if (parent != null) {
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AboutDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
