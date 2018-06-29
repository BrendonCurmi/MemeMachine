package me.McFusion.MemeMachine;

import javafx.application.Platform;
import me.McFusion.MemeMachine.database.Database;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;

public class MemeMachine {

    static final String TITLE = "MemeMachine";
    private static final String AUTHOR = "Brendon Curmi";
    public static final String[] SUPPORTED_FORMATS = {"png", "jpg", "jpeg", "mp4", "webm"};
    public static File databaseFile;

    public static void main(String[] args) {
        if (!System.getProperty("os.name").toUpperCase().contains("WIN")) return;

        MemeMachine memeMachine = new MemeMachine();
        if (SystemTray.isSupported()) memeMachine.systemTray();

        String dataPath = System.getenv("AppData") + File.separator + TITLE;
        databaseFile = new File(dataPath + File.separator + "database.sqlite");
        copyFile(databaseFile, MemeMachine.class.getResourceAsStream("database.sqlite"));
        MemeMachine.setDatabase(databaseFile.getPath());

        Window.main(args);
    }

    /**
     * Properly closes the program.
     */
    public static void exit() {
        database.close();
        if (tray != null && trayIcon != null) tray.remove(trayIcon);
        Platform.exit();
        System.exit(0);
    }

    /* Tray Icons */
    private static TrayIcon trayIcon;
    private static SystemTray tray;

    private void systemTray() {
        PopupMenu popup = new PopupMenu();
        addMenuItem(popup, "About", event -> trayIcon.displayMessage(TITLE, "by " + AUTHOR, TrayIcon.MessageType.NONE));
        popup.addSeparator();
        addMenuItem(popup, "Exit", event -> exit());

        tray = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(new ImageIcon(MemeMachine.class.getResource("imgs/pic.png")).getImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("MemeMachine");
        trayIcon.setPopupMenu(popup);
//        trayIcon.displayMessage(TITLE, "Starting...", TrayIcon.MessageType.NONE);
//        trayIcon.addActionListener(event -> System.out.println("HI"));
        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
    }

    private void addMenuItem(PopupMenu popup, String label, ActionListener listener) {
        MenuItem about = new MenuItem(label);
        about.addActionListener(listener);
        popup.add(about);
    }

    /* Files */
    public static void copyFile(File destination, InputStream sourceInputStream) {
        if (!destination.exists()) {
            try {
                if ((destination.getParentFile().exists() || destination.getParentFile().mkdirs()) && destination.createNewFile())
                    Files.copy(sourceInputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void deleteFile(File file) throws Exception {
        if (file.exists() && !file.delete()) throw new Exception("File isn't deleted");
    }

    /* Database */
    private static Database database;

    public static void setDatabase(String path) {
        MemeMachine.database = new Database(path);
    }

    public static Database getDatabase() {
        return database;
    }
}
