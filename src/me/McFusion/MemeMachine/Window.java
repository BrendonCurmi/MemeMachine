package me.McFusion.MemeMachine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.McFusion.MemeMachine.controllers.Controller;
import me.McFusion.MemeMachine.database.Database;

import java.io.IOException;
import java.util.*;

public class Window extends Application {

    private static final int WIDTH = 1040;
    private static final int HEIGHT = 700;
    private static final String PATH = "me/McFusion/MemeMachine/";
    public static final String DEFAULT_CSS = "Light";
    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Window.primaryStage = primaryStage;

        for (Scenes scene : Scenes.values()) loadScene(scene.index, scene.sceneName);

        primaryStage.setTitle(MemeMachine.TITLE);
        primaryStage.getIcons().add(new Image(getClass().getResource("imgs/pic.png").toExternalForm()));
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setOnCloseRequest(event -> MemeMachine.exit());
        primaryStage.setScene(scenes.get(0));// To place window in the center

        loadStyles();

        primaryStage.show();
    }

    public static void close() {
        primaryStage.close();
    }

    /* Scenes */
    private static List<Scene> scenes = new ArrayList<>();

    public enum Scenes {
        HOME(0, "Home"),
        SETTINGS(1, "Settings"),;

        private int index;
        private String sceneName;
        Scenes(int index, String sceneName) {
            this.index = index;
            this.sceneName = sceneName;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getSceneName() {
            return sceneName;
        }

        public void setSceneName(String sceneName) {
            this.sceneName = sceneName;
        }

        public static String getSceneName(int index) {
            for (Scenes scene : values()) if (scene.index == index) return scene.sceneName;
            return "";
        }
    }

    private void loadScene(int index, String name) throws IOException {
        Controller.setSceneName(name);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/" + name + ".fxml"));
        Scene scene = new Scene(loader.load(), WIDTH, HEIGHT);
        scene.setUserData(loader);
        scenes.add(index, scene);
    }

    public static void setCurrentScene(int index) { ;
        Platform.runLater(() -> primaryStage.setScene(scenes.get(index)));
        Controller.setSceneName(Scenes.getSceneName(index));
    }

    /* Stylesheets */
    private void loadStyles() {
        loadStylesheets();
        for (Scene scene : scenes) scene.setFill(Color.TRANSPARENT);
    }

    public static void loadStylesheets() {
        String name = Window.PATH + "css/" + Database.getThemeName() + ".css";
        for (Scene scene : scenes) scene.getStylesheets().add(name);
    }

    public static void removeStylesheets() {
        for (Scene scene : scenes) scene.getStylesheets().clear();
    }
}
