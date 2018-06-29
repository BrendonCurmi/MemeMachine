package me.McFusion.MemeMachine.controllers;

import com.pepperonas.fxiconics.awf.FxFontAwesome;
import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.McFusion.MemeMachine.MemeMachine;
import me.McFusion.MemeMachine.nodes.iconics.FxIconicsButton;
import me.McFusion.MemeMachine.nodes.iconics.FxIconicsLabel;
import me.McFusion.MemeMachine.Window;

public class TitleBarController extends Controller {

    @FXML
    private HBox titleBar;
    @FXML
    private ImageView titleBarIcon;
    @FXML
    private Label titleBarTitle;
    @FXML
    private GridPane menuBar;
    @FXML
    private HBox titlebarBtnsBox;

    @FXML
    public void initialize() {
        titleBarTitle.setText("MemeMachine");

        titleBarIcon.setFitWidth(16);
        titleBarIcon.setImage(new Image(MemeMachine.class.getResource("imgs/pic.png").toString()));

        setTitleBarBtns();

        dragFunction();

        setMenuBarBtns();
    }

    private void setTitleBarBtns() {
        FxIconicsButton minimiseBtn = makeTitleBarBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_remove), 0, event -> onMinimiseAction());
        addTooltip(minimiseBtn, "Minimise");

        onTopAction();

        FxIconicsButton closeBtn = makeTitleBarBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_clear), 2, event -> onExitAction());
        addTooltip(closeBtn, "Close");
    }

    private void onMinimiseAction() {
        Window.primaryStage.setIconified(true);
    }

    private boolean isOnTop = true;

    private void onTopAction() {
        this.isOnTop = !isOnTop;
        try {
            titlebarBtnsBox.getChildren().remove(1);
        } catch (IndexOutOfBoundsException ignored) {
        } finally {
            Window.primaryStage.setAlwaysOnTop(isOnTop);
            FxFontGoogleMaterial.Icons icons = (isOnTop) ? FxFontGoogleMaterial.Icons.gmd_bookmark : FxFontGoogleMaterial.Icons.gmd_bookmark_border;
            FxIconicsButton onTopBtn = makeTitleBarBtn(new FxIconicsButton.Builder(icons), 1, event -> onTopAction());
            addTooltip(onTopBtn, isOnTop ? "Is On top" : "Is Not On Top");
        }
    }

    private void onExitAction() {
        Stage stage = (Stage) titlebarBtnsBox.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private FxIconicsButton makeTitleBarBtn(FxIconicsButton.Builder builder, int index, EventHandler<? super MouseEvent> value) {
        FxIconicsButton btn = (FxIconicsButton) builder.color("#000000").size(20).build();
        btn.setOnMouseClicked(value);
        btn.getStyleClass().add("titleBarExitBtn");
        titlebarBtnsBox.getChildren().add(index, btn);
        return btn;
    }

    private double mx, my;

    private void dragFunction() {
        titleBar.setOnMousePressed(event -> {
            javafx.stage.Window window = titleBar.getScene().getWindow();
            mx = window.getX() - event.getScreenX();
            my = window.getY() - event.getScreenY();
        });
        titleBar.setOnMouseDragged(event -> {
            javafx.stage.Window window = titleBar.getScene().getWindow();
            window.setX(event.getScreenX() + mx);
            window.setY(event.getScreenY() + my);
        });
    }

    private void setMenuBarBtns() {
        makeMenuBarBtn(FxFontAwesome.Icons.faw_home, MenuBar.LEFT, Window.Scenes.HOME);
        makeMenuBarBtn(FxFontAwesome.Icons.faw_cog, MenuBar.RIGHT, Window.Scenes.SETTINGS);
    }

    private void makeMenuBarBtn(FxFontAwesome.Icons icons, MenuBar menuBar, Window.Scenes focusedScene) {
        if (!getSceneName().equals(focusedScene.getSceneName())) {
            FxIconicsLabel label = (FxIconicsLabel) new FxIconicsLabel.Builder(icons).size(40).build();
            label.setOnMouseClicked(e -> Window.setCurrentScene(focusedScene.getIndex()));
            ((HBox) this.menuBar.getChildren().get(menuBar.getIndex())).getChildren().add(label);
            addTooltip(label, focusedScene.getSceneName());
        }
    }

    private enum MenuBar {
        LEFT(0),
        RIGHT(1);

        private int index;

        MenuBar(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
