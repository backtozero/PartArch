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

package partarch.util;

import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * 
 * @author <gotozero@yandex.com>
 */
public class Prefs {
    
    private Preferences mainPrefs;
    private Preferences loginCredentials;

    public Prefs() {
        mainPrefs =  Preferences.userRoot().node("partarch");  // root node for this application.
        loginCredentials = mainPrefs.node("logincredentials"); // node for store login information
    }

    public void saveStageProperties(Stage stage) {
        if (!stage.isMaximized()) {
        mainPrefs.putDouble("stage.x", stage.getX());
        mainPrefs.putDouble("stage.y", stage.getY());
        mainPrefs.putDouble("stage.height", stage.getHeight());
        mainPrefs.putDouble("stage.width", stage.getWidth());
        }
        mainPrefs.putBoolean("stage.maximized", stage.isMaximized());
    }
    

    
    public void loadStageProperties(Stage stage) {
        stage.setX(mainPrefs.getDouble("stage.x", 40));
        stage.setY(mainPrefs.getDouble("stage.y", 40));
        stage.setHeight(mainPrefs.getDouble("stage.height", 700));
        stage.setWidth(mainPrefs.getDouble("stage.width", 1200));
        stage.setMaximized(mainPrefs.getBoolean("stage.maximized", false));
        
        if (Screen.getScreensForRectangle(mainPrefs.getDouble("stage.x", 40),
                mainPrefs.getDouble("stage.y", 40),
                mainPrefs.getDouble("stage.width", 1200),
                mainPrefs.getDouble("stage.height", 700)).size() == 0) {
            stage.setX(40);
            stage.setY(40);
            stage.setHeight(600);
            stage.setWidth(1024);
            stage.setMaximized(false);
        }
        
    }


    /**
     * Connection information part such like "username" "SID" etc.
     * 
     */
    public void putLastUsedLoginAlias(String name) {
        mainPrefs.put("lastusedloginalias", name);
    }

    public String getLastUsedLoginAlias() {
        return mainPrefs.get("lastusedloginalias", "");
    }

    
    public void putAlias(String name, TreeMap<String,String> credentials) {
        Preferences alias = loginCredentials.node(name);
        
        credentials.entrySet().stream().forEach((entry) -> {
            alias.put(entry.getKey(), entry.getValue());
        });                
    }
    
    public TreeMap<String, String> getAlias(String name) {
        
        Preferences alias;        
        TreeMap<String, String> credentials = new TreeMap<>();
        
        try {
            if (loginCredentials.nodeExists(name)) {
                alias = loginCredentials.node(name);
                Arrays.asList(alias.keys()).stream().forEach((entry) -> {credentials.put(entry, alias.get(entry, "fuck"));});
                return credentials;        
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(Prefs.class.getName()).log(Level.SEVERE, null, ex);
        }
        return credentials;
    }
    
    public ObservableList<String> getAllAliasNames() {
        ObservableList<String> aliasNames = FXCollections.observableArrayList();
        try {
            if (loginCredentials.childrenNames().length > 0) {
                Arrays.asList(loginCredentials.childrenNames()).stream().forEach((entry) -> {aliasNames.add(entry);});
            }

        } catch (BackingStoreException ex) {
            Logger.getLogger(Prefs.class.getName()).log(Level.SEVERE, null, ex);
        }
        return aliasNames;
    }
    
    public void deleteAlias(String name) {
        try {
            loginCredentials.node(name).removeNode();
        } catch (BackingStoreException ex) {
            Logger.getLogger(Prefs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
