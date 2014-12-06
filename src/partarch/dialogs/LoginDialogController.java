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
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import oracle.jdbc.pool.OracleDataSource;
import partarch.PartArchController;
import partarch.util.Prefs;

/**
 * FXML Controller class
 *
 * @author jesus
 */
public class LoginDialogController extends Stage implements Initializable {

    private Stage stage;
    private OracleDataSource ods = null;
    
    private Prefs prefs = new Prefs();
    private ObservableList<String> savedCredentials = FXCollections.observableArrayList();
    private BooleanBinding booleanBinding;
    
    @FXML
    private SplitMenuButton connectButton;
    @FXML
    private Button cancelButton;
    @FXML
    private ComboBox<String> savedCredentialsComboBox;
    @FXML
    private TextField aliasTextField;
    @FXML
    private TextField userNameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField sidTextField;
    @FXML
    private TextField hostnameTextField;
    @FXML
    private TextField driverTypeTextField;
    @FXML
    private TextField listenerPortTextField;
    
    
    @FXML
    private void onCancelButton(ActionEvent event) {
        close();
    }
    
    @FXML
    private void connectAndSaveHandle(ActionEvent event) {
        saveCredential();
        prefs.putLastUsedLoginAlias(savedCredentialsComboBox.getSelectionModel().getSelectedItem());
        fillOracleDataSource();
        close();
        
    }
    
    @FXML
    private void deleteConnection(ActionEvent event){
        prefs.deleteAlias(savedCredentialsComboBox.getSelectionModel().getSelectedItem());
        refreshCredentials();
    }
    
    @FXML
    private void saveConnection(ActionEvent event) {
        saveCredential();
        refreshCredentials();
    }
    
    
    /**
     * Get OracleDataSource
     */
    private void fillOracleDataSource() {
        OracleDataSource ds = null;
        try {
            ds = new OracleDataSource();
            ds.setDriverType(driverTypeTextField.getText());
            ds.setUser(userNameTextField.getText());
            ds.setPassword(passwordTextField.getText());
            ds.setDatabaseName(sidTextField.getText());
            ds.setServerName(hostnameTextField.getText());
            ds.setPortNumber(Integer.parseInt(listenerPortTextField.getText()));
        } catch (SQLException ex) {
            ExceptionDialogController exceptionDialog = new ExceptionDialogController(ex.getMessage(), ex, stage);
            exceptionDialog.showAndWait();
            Logger.getLogger(PartArchController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ods = ds;        
    }
    
    private void saveCredential() {
        if (aliasTextField.getLength() > 0) {
            if (savedCredentials.indexOf(aliasTextField.getText()) == -1 ) {
                savedCredentials.add(aliasTextField.getText());
            }
            
            TreeMap<String, String> credentials = new TreeMap<>();
            credentials.put("username", userNameTextField.getText());
            credentials.put("password", passwordTextField.getText());
            credentials.put("sid", sidTextField.getText());
            credentials.put("hostname", hostnameTextField.getText());
            credentials.put("drivertype", driverTypeTextField.getText());
            credentials.put("listenerport", listenerPortTextField.getText());
            
            prefs.putAlias(aliasTextField.getText(), credentials);           
            
        }
    }
    
    private void getCredential(String alias){
        TreeMap<String, String> credential = prefs.getAlias(alias);
        credential.entrySet().stream().forEach((entry) -> {
            switch (entry.getKey()) {
                case "username": {userNameTextField.setText(entry.getValue());break;}
                case "password": {passwordTextField.setText(entry.getValue());break;}
                case "sid": {sidTextField.setText(entry.getValue()); break;}
                case "hostname" : {hostnameTextField.setText(entry.getValue()); break;}
                case "drivertype" : {driverTypeTextField.setText(entry.getValue()); break;}
                case "listenerport" : {listenerPortTextField.setText(entry.getValue()); break;}
            }
        });        
    }
    
    private void refreshCredentials(){
        savedCredentials.clear();
        savedCredentials.addAll(prefs.getAllAliasNames());
    }
    
    public OracleDataSource create() {
        this.showAndWait();
        return ods;
    }

    


    
    
    
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshCredentials();
        savedCredentialsComboBox.setItems(savedCredentials);        
        savedCredentialsComboBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (newValue != null) {
                getCredential(newValue);
                aliasTextField.setText(newValue);
            }
        });
        savedCredentialsComboBox.getSelectionModel().select(prefs.getLastUsedLoginAlias());
        
        booleanBinding = aliasTextField.textProperty().isEqualTo("")
                .or(userNameTextField.textProperty().isEqualTo(""))
                .or(passwordTextField.textProperty().isEqualTo(""))
                .or(sidTextField.textProperty().isEqualTo(""))
                .or(hostnameTextField.textProperty().isEqualTo(""))
                .or(listenerPortTextField.textProperty().isEqualTo(""));
        connectButton.disableProperty().bind(booleanBinding);
        
    }
    
    public LoginDialogController(Stage parent) {
        setTitle("Login");
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);

        if (parent != null) {
            this.stage = parent;
            setY(parent.getY() + parent.getHeight() / 4 );
            setX(parent.getX() + parent.getWidth() / 4 );  

        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginDialog.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
