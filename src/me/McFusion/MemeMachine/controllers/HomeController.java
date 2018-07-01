package me.McFusion.MemeMachine.controllers;

import com.pepperonas.fxiconics.awf.FxFontAwesome;
import com.pepperonas.fxiconics.gmd.FxFontGoogleMaterial;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import me.McFusion.MemeMachine.nodes.Autocomplete;
import me.McFusion.MemeMachine.database.Database;
import me.McFusion.MemeMachine.nodes.iconics.FxIconicsButton;
import me.McFusion.MemeMachine.MemeMachine;
import me.McFusion.MemeMachine.nodes.MemePlayer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeController extends Controller {

    @FXML
    private GridPane pane;
    @FXML
    private TextField search;
    @FXML
    private HBox box;

    private Autocomplete ac;
    static final String AUTOCOMPLETE_NAME = "Tags";
    private final String TEXTFIELD_PROMPT_TEXT = "Tags...";

    @FXML
    public void initialize() {
        makeSearchBarBtns();

        ac = new Autocomplete(AUTOCOMPLETE_NAME, search, Database.getAllTags(), false);

        search.setPromptText(TEXTFIELD_PROMPT_TEXT);
        search.setOnAction(event -> onSearchAction());

        width = pane.getPrefWidth() / 5;
//        pane.setGridLinesVisible(true); //TODO For debug only

        if (!Database.getPath().equals("none")) loadFromDefault();
    }

    @FXML
    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles() || event.getDragboard().hasUrl()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        if (Database.getPath().equals("none")) return;
        if (event.getDragboard().hasFiles()) {// From Local
            List<File> files = event.getDragboard().getFiles();
            String uuid;
            for (File file : files) {
                if (!isSupportedFormat(file.getName())) continue;
                uuid = saveFile(file, false);
                if (!search.getText().isEmpty()) {
                    for (String tag : getTagsFromString(search.getText())) {
                        try {
                            Database.addTagToMeme(uuid, tag);
                        } catch (SQLException ignored) {
                        }
                    }
                }
            }
        } else if (event.getDragboard().hasUrl()) {// From Browser
            try {
                String url = event.getDragboard().getUrl();
                if (url != null) {
                    File tempFile = new File(Database.getPath() + File.separator + "temp.png");
                    if (!isSupportedFormat(url)) {
                        MemeMachine.deleteFile(tempFile);
                        return;
                    }
                    MemeMachine.copyFile(tempFile, new URL(url).openStream());
                    saveFile(tempFile, true);
                    MemeMachine.deleteFile(tempFile);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean isSupportedFormat(String url) {
        for (String format : MemeMachine.SUPPORTED_FORMATS) if (url.contains("." + format)) return true;
        return false;
    }

    /* Text */

    /**
     * Formats the text(tag string) for internal use, such as the database and Autocomplete,
     * from the field.
     */
    private String formatText(String s) {
        return s.replace("_", " ");
    }

    /**
     * Formats the text(tag string) for external use, such as the textfield.
     */
    private String formatTextForTextfield(String s) {
        return s.replace(" ", "_");
    }

    private String[] getTagsFromString(String s) {
        String[] tags = s.split(" ");
        for (int i = 0; i < tags.length; i++) tags[i] = formatText(tags[i]);
        return tags;
    }

    /* Images and Media */
    private double width;
    private double height = 180;
    private int[] focus = {0, 0};

    private HBox load(String uuid) throws FileNotFoundException {
        String path = Database.getPath() + File.separator + uuid;

        HBox container;
        if (isPlayer(uuid)) {
            MemePlayer memePlayer = new MemePlayer(path);
            memePlayer.setViewStyle(view -> {
                view.setPreserveRatio(true);
                view.setFitWidth(width);
                view.setFitHeight(height);
            });
            container = memePlayer.getContainer();
        } else {
            ImageView view = new ImageView(new Image(new FileInputStream(new File(path))));
            view.setPreserveRatio(true);
            view.setFitWidth(width);
            view.setFitHeight(height);
            container = new HBox(view);
        }
        container.setAlignment(Pos.CENTER);
        container.setUserData(uuid);
        container.setOnMouseClicked(event -> {
            if (event.isControlDown()) onCellSelected(null, null);// Deselect Cell
            else switch (event.getButton()) {
                default:
                case PRIMARY:// Select cells
                    onCellSelected(container, uuid);
                    break;
                case SECONDARY:// Get cell options
                    ContextMenu clickMenu = new ContextMenu();
                    clickMenu.getItems().add(new MenuItem(uuid));
                    clickMenu.getItems().add(new SeparatorMenuItem());
                    MenuItem item = new MenuItem("Delete");
                    item.setOnAction(event1 -> Database.removeMeme(uuid));
                    clickMenu.getItems().add(item);
                    clickMenu.show(container, Side.BOTTOM, 1, 1);
                    break;
            }
        });
        pane.add(container, focus[0], focus[1]);

        if (focus[0]++ == 4) {
            focus[0] = 0;
            focus[1]++;
        }
        return container;
    }

    private void loadFromDefault() {
        try {
            ResultSet set = MemeMachine.getDatabase().select(Database.Table.MEMES.getName(), "ORDER BY id DESC LIMIT 100");
            String uuid;
            while (set.next()) {
                uuid = set.getString("uuid");
                try {
                    load(uuid);
                } catch (FileNotFoundException ex) {
                    Database.removeMeme(uuid);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadFromTags(String[] tags) {
        for (String tag : tags) {
            try {
                ResultSet set = MemeMachine.getDatabase().select(Database.Table.MEME_TAGS.getName(), "WHERE tag_id = " + Database.getTagID(formatText(tag)));
                String uuid;
                while (set.next()) {
                    uuid = Database.getMemeUUID(set.getInt("meme_id"));
                    try {
                        load(uuid);
                    } catch (FileNotFoundException ex) {
                        Database.removeMeme(uuid);
                    }
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private void clearPane() {
        pane.getChildren().clear();
        focus[0] = focus[1] = 0;
    }

    private String saveFile(File file, boolean select) {
        String uuid = UUID.randomUUID().toString();
        if (isPlayer(file.getName())) uuid += ".mp4";
        else if (file.getName().endsWith(".gif")) uuid += ".gif";
        else uuid += ".png";

        try {
            FileInputStream inputStream = new FileInputStream(file);
            MemeMachine.copyFile(new File(Database.getPath() + File.separator + uuid), inputStream);
            Database.addMeme(uuid);
            HBox container = load(uuid);
            if (select) onCellSelected(container, uuid);
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return uuid;
    }

    private boolean isPlayer(String uuid) {
        return uuid.endsWith(".mp4");
    }

    /* Cell Selection */
    private HBox selectedContainer = null;

    private void onCellSelected(HBox selectedContainer, String uuid) {
        if (isEditingMode) return;
        if (this.selectedContainer == selectedContainer && selectedContainer != null && !isPlayer(uuid)) {
            onCellSelected(null, null);
            return;
        }
        if (this.selectedContainer != null) {
            this.selectedContainer.getStyleClass().remove("selected-cell");
            if (this.selectedContainer != selectedContainer && MemePlayer.getMemePlayer(this.selectedContainer) != null && MemePlayer.getMemePlayer(this.selectedContainer).isPlaying())
                MemePlayer.getMemePlayer(this.selectedContainer).pause();
        }
        if (selectedContainer != null) {
            selectedContainer.getStyleClass().add("selected-cell");
            if (isPlayer(uuid)) {
                MemePlayer.getMemePlayer(selectedContainer).togglePlay();
                copyFileToClipboard(MemePlayer.getMemePlayer(selectedContainer).getFile());
            } else if (uuid.endsWith(".gif")) copyFileToClipboard(new File(Database.getPath() + File.separator + uuid));
            else copyImageToClipboard(((ImageView) selectedContainer.getChildren().get(0)).getImage());
        }
        this.selectedContainer = selectedContainer;
        search.setText(getTagsOfSelectedCell(uuid));
    }

    private String getTagsOfSelectedCell(String uuid) {
        if (uuid == null) return "";
        List<String> list = Database.getTagsFromMeme(uuid);
        StringBuilder builder = new StringBuilder();
        for (String tag : list) builder.append(formatTextForTextfield(tag)).append(" ");
        return builder.toString();
    }

    private void copyImageToClipboard(Image img) {
        ClipboardContent cb = new ClipboardContent();
        cb.putImage(img);
        Clipboard.getSystemClipboard().setContent(cb);
    }

    private void copyFileToClipboard(File file) {
        ClipboardContent cb = new ClipboardContent();
        List<File> files = new ArrayList<>();
        files.add(file);
        cb.putFiles(files);
        Clipboard.getSystemClipboard().setContent(cb);
    }

    /* Search Bar */
    private void onSearchAction() {
        if (oldEditingTag != null) onEditTagEnterAction();// Edit tag
        else if (selectedContainer == null) {// Searching for tags or resetting pane
            clearPane();
            if (search.getText().isEmpty()) loadFromDefault();
            else loadFromTags(search.getText().split(" "));
        } else {// Adding tags
            String uuid = (String) selectedContainer.getUserData();
            Database.removeTagsFromMeme(uuid);
            String[] tags = search.getText().split(" ");
            for (String tag : tags) {
                try {
                    Database.addTagToMeme(uuid, formatText(tag));
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private boolean isEditingMode = false;
    private String oldEditingTag = null;

    private void makeSearchBarBtns() {
        if (!isEditingMode)
            makeBtn(new FxIconicsButton.Builder(FxFontAwesome.Icons.faw_pencil), "Editing Mode", event -> onEnterEditingMode());
        else {
            makeBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_add), "Add Tag", event -> onAddTagAction());
            makeBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_remove), "Remove Tag", event -> onRemoveTagAction());
            makeBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_edit), "Edit Tag Name", event -> onEditTagAction());
            makeBtn(new FxIconicsButton.Builder(FxFontGoogleMaterial.Icons.gmd_clear), "Exit Editing Mode", event -> onExitEditingMode());
        }
    }

    private void onAddTagAction() {
        if (search.getText().isEmpty()) return;
        String val = search.getText();
        Database.addTag(val);
        ac.addPossibleSuggestion(val);
    }

    private void onRemoveTagAction() {
        if (search.getText().isEmpty()) return;
        String val = formatText(search.getText());
        Database.removeTag(val);
        ac.removePossibleSuggestion(val);
    }

    private void onEditTagAction() {
        if (oldEditingTag != null || search.getText().isEmpty()) return;
        oldEditingTag = search.getText();
        search.setPromptText(oldEditingTag + " ---> ");
        search.clear();
        ac.hide();
        search.positionCaret(1);
    }

    private void onEditTagEnterAction() {
        String oldName = formatText(oldEditingTag);
        String newName = formatText(search.getText());
        MemeMachine.getDatabase().update(Database.Table.TAGS.getName(), "name = '" + newName + "'", "name = '" + oldName + "'");
        ac.removePossibleSuggestion(oldName);
        ac.addPossibleSuggestion(newName);
        oldEditingTag = null;
        search.setPromptText(TEXTFIELD_PROMPT_TEXT);
        search.clear();
        ac.show();
    }

    private void onEnterEditingMode() {
        if (selectedContainer != null) return;
        isEditingMode = true;
        box.getChildren().clear();
        makeSearchBarBtns();
    }

    private void onExitEditingMode() {
        isEditingMode = false;
        box.getChildren().clear();
        makeSearchBarBtns();
    }

    private void makeBtn(FxIconicsButton.Builder builder, String msg, EventHandler<? super MouseEvent> value) {
        FxIconicsButton btn = (FxIconicsButton) builder.color("#000000").build();
        addTooltip(btn, msg);
        btn.setOnMouseClicked(value);
        box.getChildren().add(btn);
    }
}
