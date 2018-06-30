package me.McFusion.MemeMachine.nodes;

import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class MemePlayer {

    private HBox container;
    private MediaPlayer player;
    private boolean isPlaying = false;
    private File file;

    public MemePlayer(String path) throws FileNotFoundException {
        file = new File(path);
        if (!file.exists()) throw new FileNotFoundException("Media '" + path + "' not found");
        player = new MediaPlayer(new Media(file.toURI().toString()));
        player.setAutoPlay(false);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        this.container = new HBox(new MediaView(player));
        memePlayers.put(this.container, this);
    }

    public void setViewStyle(ViewStyle viewStyle) {
        viewStyle.run((MediaView) container.getChildren().get(0));
    }

    public HBox getContainer() {
        return container;
    }

    public File getFile() {
        return file;
    }

    private static HashMap<HBox, MemePlayer> memePlayers = new HashMap<>();

    public static MemePlayer getMemePlayer(HBox container) {
        return memePlayers.getOrDefault(container, null);
    }

    public interface ViewStyle {
        void run(MediaView view);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void play() {
        if (!isPlaying) {
            player.play();
            isPlaying = true;
        }
    }

    public void pause() {
        if (isPlaying) {
            player.pause();
            isPlaying = false;
        }
    }

    public void togglePlay() {
        if (isPlaying) pause();
        else play();
    }
}
