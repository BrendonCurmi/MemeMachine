package me.McFusion.MemeMachine.nodes;

import javafx.geometry.Side;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Framework implementing autocomplete functionality to a
 * TextField node.
 */
public class Autocomplete {

    private final int MAX_SUGGESTIONS = 10;
    private TextField field;
    private List<String> possibleSuggestions, suggestions;
    private boolean allowSpacesInCorrectedWords;
    private ContextMenu contextMenu;
    private boolean isHidden = false;

    public Autocomplete(String name, TextField field, List<String> possibleSuggestions, boolean allowSpacesInCorrectedWords) {
        this.field = field;
        possibleSuggestions.sort(String::compareToIgnoreCase);
        this.possibleSuggestions = possibleSuggestions;
        this.allowSpacesInCorrectedWords = allowSpacesInCorrectedWords;
        this.suggestions = new ArrayList<>();
        this.contextMenu = new ContextMenu();

        field.setContextMenu(contextMenu);

        field.setOnMouseClicked(event -> {
            if (!isHidden) showList();
        });

        field.setOnKeyReleased(event -> {
            if (isHidden) return;
            switch (event.getCode()) {
                default:
                    break;
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    return;
                case ENTER:
                    if (suggestions.isEmpty()) hideList();
                    return;
            }

            String val = field.getText();
            if (val.contains(" ")) val = val.substring(val.lastIndexOf(" ") + 1);

            if (val.equals("")) {// New word or empty
                hideList();
                suggestions.clear();
                floodList(this.possibleSuggestions);
                showList();
            } else if (val.length() == 1) {// First character entered
                suggestions.clear();
                startSuggestions(val);
            } else narrowDownSuggestions(val);// More characters entered
        });
        floodList(this.possibleSuggestions);
        autocompletes.put(name, this);
    }

    private static HashMap<String, Autocomplete> autocompletes = new HashMap<>();

    public static Autocomplete getAutocomplete(String name) {
        return autocompletes.get(name);
    }

    public void hide() {
        isHidden = true;
    }

    public void show() {
        isHidden = false;
    }

    public void reloadWith(List<String> possibleSuggestions) {
        hideList();
        suggestions.clear();
        possibleSuggestions.sort(String::compareToIgnoreCase);
        this.possibleSuggestions = possibleSuggestions;
        floodList(possibleSuggestions);
        showList();
    }

    public void addPossibleSuggestion(String s) {
        if (possibleSuggestions != null) {
            possibleSuggestions.add(s);
            possibleSuggestions.sort(String::compareToIgnoreCase);
        }
    }

    public void removePossibleSuggestion(String s) {
        if (possibleSuggestions != null) possibleSuggestions.remove(s);
    }

    private void startSuggestions(String val) {
        for (String s : possibleSuggestions)
            if (s.toLowerCase().startsWith(val.toLowerCase()) && !suggestions.contains(s))
                suggestions.add(s);
        floodList(suggestions);
    }

    private void narrowDownSuggestions(String val) {
        for (ListIterator<String> iter = suggestions.listIterator(); iter.hasNext(); ) {
            String s = iter.next();
            if (!s.toLowerCase().startsWith(val.toLowerCase())) {
                iter.remove();
                removeFromList(s);
            }
        }
    }

    private void floodList(List<String> suggestionsList) {
        contextMenu.getItems().clear();
        for (int i = 0; i < MAX_SUGGESTIONS && i < suggestionsList.size(); i++) {
            MenuItem item = new MenuItem(suggestionsList.get(i));
            item.setOnAction(event -> replaceLastWord(field.getCaretPosition(), item.getText()));
            contextMenu.getItems().add(item);
        }
    }

    private void replaceLastWord(int caratPosition, String replacement) {
        String val = field.getText();
        int start = -1;
        while (val.contains(" ")) {
            if (val.indexOf(" ") > caratPosition) break;
            start = val.indexOf(" ");
            val = val.replaceFirst(" ", "-");
        }
        field.replaceText(start + 1, caratPosition, (allowSpacesInCorrectedWords) ? replacement : replacement.replace(" ", "_"));
    }

    private void removeFromList(String string) {
        contextMenu.getItems().removeIf(item -> item.getText().equalsIgnoreCase(string));
    }

    private void hideList() {
        contextMenu.hide();
    }

    private void showList() {
        contextMenu.show(field, Side.BOTTOM, 1, 1);
    }
}
