package me.McFusion.MemeMachine.controllers;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public abstract class Controller {

    private static String sceneName;

    protected static String getSceneName() {
        return sceneName;
    }

    public static void setSceneName(String sceneName) {
        Controller.sceneName = sceneName;
    }

    protected static void addTooltip(Node node, String msg) {
        Tooltip tooltip = new Tooltip(msg);
        tooltip.getStyleClass().add("tooltip");
        Tooltip.install(node, tooltip);
    }
}
