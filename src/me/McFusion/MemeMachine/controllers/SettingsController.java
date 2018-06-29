package me.McFusion.MemeMachine.controllers;

import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import me.McFusion.MemeMachine.nodes.Autocomplete;
import me.McFusion.MemeMachine.database.Database;
import me.McFusion.MemeMachine.nodes.iconics.FxIconicsButton;
import me.McFusion.MemeMachine.MemeMachine;
import me.McFusion.MemeMachine.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SettingsController extends Controller {

    @FXML
    private BorderPane pane;
    @FXML
    private Button pathBtn;
    @FXML
    private ComboBox settingsTheme;
    @FXML
    private HBox boxDatabase;

    private final String[] THEMES = {"Light", "Dark", "Red"};
    private final String[] EXTENSION_FILTER = {"SQLite files (*.sqlite)", "*.sqlite"};

    @SuppressWarnings("unchecked")
    @FXML
    public void initialize() {
        setupThemes(settingsTheme.getItems());
        settingsTheme.getSelectionModel().select(Database.getThemeName());
        settingsTheme.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Database.setTheme(newValue.toString());
            Window.removeStylesheets();
            Window.loadStylesheets();
        });

        pathBtn.setMaxWidth(200);
        pathBtn.setText(Database.getPath());

        databaseBtns();
    }

    private void setupThemes(List<String> list) {
        list.addAll(Arrays.asList(THEMES));
    }

    private void databaseBtns() {
        makeBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_cloud_upload), event -> onDatabaseUpload());
        makeBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_cloud_download), event -> onDatabaseDownload());
    }

    private void onDatabaseUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(EXTENSION_FILTER[0], EXTENSION_FILTER[1]));
        File file = fileChooser.showOpenDialog(Window.primaryStage);
        if (file != null) {
            MemeMachine.getDatabase().close();
            try {
                MemeMachine.deleteFile(MemeMachine.databaseFile);
                MemeMachine.copyFile(MemeMachine.databaseFile, new FileInputStream(file));
                MemeMachine.setDatabase(MemeMachine.databaseFile.getPath());
                Autocomplete.getAutocomplete(HomeController.AUTOCOMPLETE_NAME).reloadWith(Database.getAllTags());
                Window.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                MemeMachine.exit();
            }
        }
    }

    private void onDatabaseDownload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(EXTENSION_FILTER[0], EXTENSION_FILTER[1]));
        File file = fileChooser.showSaveDialog(Window.primaryStage);
        try {
            if (file != null) MemeMachine.copyFile(file, new FileInputStream(MemeMachine.databaseFile));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void makeBtn(FxIconicsButton.Builder builder, EventHandler<? super MouseEvent> value) {
        FxIconicsButton btn = (FxIconicsButton) builder.color("#000000").build();
        btn.setOnMouseClicked(value);
        boxDatabase.getChildren().add(btn);
    }

    @FXML
    private void onPathButtonAction(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        String path = Database.getPath();
        if (!path.equals("none")) chooser.setInitialDirectory(new File(path));
        File file = chooser.showDialog(pane.getScene().getWindow());
        if (file != null && file.isDirectory()) Database.setPath(file.getAbsolutePath());
        pathBtn.setText(Database.getPath());
    }
}
