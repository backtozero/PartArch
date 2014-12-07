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
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;
import partarch.db.DBBridge;
import partarch.db.util.ddl.ExchangePartition;
import partarch.db.util.ddl.PartitionClause;
import partarch.db.util.ddl.TableLockType;
import partarch.model.Table;
import partarch.fx.ComboBoxFilter;
import partarch.util.ClipboardTool;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class ExchangePartitionController extends Stage implements Initializable, PartitionClause, TableLockType {

    private Stage stage;
    private Table table;
    private TreeMap<String,Table> tableList;    
    private DBBridge db;
    private ObservableList<String> tableListObservable = FXCollections.observableArrayList();
    private final ClipboardTool ct;
    private final SQLDialogController sqld;
    
    
    @FXML
    private TextField sourceTableTextField;
    @FXML
    private ComboBoxFilter<String> intermediateTableComboBox;
    @FXML
    private ComboBoxFilter<String> destinationTableComboBox;    
    @FXML
    private TableView partitionsTableView;
    @FXML
    private ToggleGroup validationToggleGroup;
    @FXML
    private RadioButton withValidationRadioButton;
    @FXML
    private RadioButton withoutValidationRadioButton;
    @FXML
    private ToggleGroup indexesToggleGroup;
    @FXML
    private RadioButton updateIndexesRadioButton;
    @FXML
    private RadioButton updateGlobalIndexesRadioButton;    
    @FXML
    private CheckBox includingIndexesCheckBox;
    @FXML
    private ToggleGroup lockTableToggleGroup;
    @FXML
    private RadioButton sharedModeRadioButton;
    @FXML
    private RadioButton exclusiveModeNowaitRadioButton;
    
    
    @FXML
    protected void cancelButton(ActionEvent event){
        close();
    }
    
    @FXML
    protected void countButton(ActionEvent event){
        if (intermediateTableComboBox.getEditor().getText().length() > 0) {
            Task task = db.getRowCountTask(intermediateTableComboBox.getEditor().getText());
            db.addDBTaskToQueue(task);
            
            Thread tr = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        int count = (Integer) task.get();
                        if (count != -1) {
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    MessageDialogController md = new MessageDialogController(stage, "Row Count " + count);
                                    md.showAndWait();
                                }
                            });

                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ExchangePartitionController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(ExchangePartitionController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, "rowCountWait");
            tr.start();
        }

    }
    
    @FXML
    protected void generateDDLButton(ActionEvent event) {
        
        sqld.setSQLBlocks(new ExchangePartition().setPartitions(table.getPartitions(false))
                .setSourceTable(sourceTableTextField.getText())
                .setTableLockType(lockTableToggleGroup.getSelectedToggle().getUserData().toString())
                .setIntermediateTable(intermediateTableComboBox.getEditor().getText())
                .setIncludingIndexes(includingIndexesCheckBox.isSelected() ? includingIndexesCheckBox.getUserData().toString() : null)
                .setValidation(validationToggleGroup.getSelectedToggle().getUserData().toString())
                .setUpdateIndexes(indexesToggleGroup.getSelectedToggle() != null ? indexesToggleGroup.getSelectedToggle().getUserData().toString() : null)
                .setDestinationTable(destinationTableComboBox.getEditor().getText()).getList());
        sqld.show();
    }
    
    
    
    
    
    
    
    
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableList.entrySet().stream().forEach((item) -> {tableListObservable.add(item.getKey());});
        
        intermediateTableComboBox.setData(tableListObservable);
        destinationTableComboBox.setData(tableListObservable);
        
        sourceTableTextField.setText(table.getTableSchema().concat(".").concat(table.getTableName()));
        
        partitionsTableView.setItems(table.getPartitions(false));
        
        withValidationRadioButton.setUserData(WITH_VALIDATION);
        withoutValidationRadioButton.setUserData(WITHOUT_VALIDATION);
        updateIndexesRadioButton.setUserData(UPDATE_INDEXES);
        updateGlobalIndexesRadioButton.setUserData(UPDATE_GLOBAL_INDEXES);
        sharedModeRadioButton.setUserData(LOCK_MODE_SHARE);
        exclusiveModeNowaitRadioButton.setUserData(LOCK_MODE_EXCLUSIVE_NOWAIT);
        includingIndexesCheckBox.setUserData(INCLUDING_INDEXES);
        
        
    }

    public ExchangePartitionController(Stage parent, Table table, TreeMap<String,Table> tableList, DBBridge db, ClipboardTool ct) {
        setTitle("Exchange");
        this.stage = parent;
        this.table = table;
        this.tableList = tableList;
        this.db = db;
        this.ct = ct;
        
        initModality(Modality.APPLICATION_MODAL);
        
       
        
        if (parent != null) {
            setY(parent.getY() + parent.getHeight() / 4);
            setX(parent.getX() + parent.getWidth() / 4);
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ExchangePartition.fxml"));
        fxmlLoader.setController(this);

        try {
            setScene(new Scene((Parent) fxmlLoader.load()));
        } catch (IOException ex) {
            Logger.getLogger(ExceptionDialogController.class.getName()).log(Level.SEVERE, null, ex);
        }

        sqld = new SQLDialogController(stage, db, ct);
    }

    
}
