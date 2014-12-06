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
package partarch;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import partarch.util.Prefs;

/**
 *
 * @author <gotozero@yandex.com>
 */
public class PartArch extends Application {

    private PartArchController controller;
    private Stage primaryStage;
    private final Prefs prefs = new Prefs();
    
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        
        this.primaryStage = primaryStage;                            
        prefs.loadStageProperties(primaryStage);
        
        
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("PartArch.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(primaryStage);
        Scene scene = new Scene(root);
        scene.setFill(Color.GRAY);

        primaryStage.getIcons().addAll(new Image("/partarch/icons/partition128x.png"), new Image("/partarch/icons/partition.png"));
        primaryStage.setTitle("Partitioning Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        controller.cleanUp();
        prefs.saveStageProperties(primaryStage);
    }

}
