package me.McFusion.MemeMachine.database;

import me.McFusion.MemeMachine.MemeMachine;
import me.McFusion.MemeMachine.Window;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLite {

    public enum Table {
        SETTINGS("settings"),// CREATE TABLE settings (key TEXT, value TEXT)
        MEMES("memes"),// CREATE TABLE memes (id INTEGER PRIMARY KEY ASC, uuid TEXT NOT NULL UNIQUE)
        TAGS("tags"),// CREATE TABLE tags (id INTEGER PRIMARY KEY ASC, name TEXT NOT NULL UNIQUE)
        MEME_TAGS("meme_tags");// CREATE TABLE meme_tags (meme_id INTEGER, tag_id INTEGER)

        private String tblName;

        Table(String tblName) {
            this.tblName = tblName;
        }

        public String getName() {
            return tblName;
        }

        public void setName(String tblName) {
            this.tblName = tblName;
        }
    }

    public Database(String path) {
        super(path);
    }

    public static String getPath() {
        return String.valueOf(get(Table.SETTINGS, "WHERE key = 'path'", "value", null));
    }

    public static void setPath(String path) {
        set(Table.SETTINGS, "key = 'path'", "value", path);
    }

    private static Object get(Table table, String conditionQuery, String column, String defaultReturnValue) {
        try {
            return get(table, conditionQuery, column);
        } catch (SQLException ignore) {
            return defaultReturnValue;
        }
    }

    private static Object get(Table table, String conditionQuery, String column) throws SQLException {
        return MemeMachine.getDatabase().select(table.getName(), conditionQuery).getObject(column);
    }

    private static void set(Table table, String tblLocation, String column, String newValue) {
        MemeMachine.getDatabase().update(table.getName(), column + " = '" + newValue + "'", tblLocation);
    }

    /* Getters and Setters */
    public static String getThemeName() {
        return String.valueOf(get(Table.SETTINGS, "WHERE key = 'theme'", "value", Window.DEFAULT_CSS));
    }

    public static void setTheme(String theme) {
        set(Table.SETTINGS, "key = 'theme'", "value", theme);
    }

    public static int getMemeID(String uuid) {
        return Integer.parseInt(get(Table.MEMES, "WHERE uuid = '" + uuid + "'", "id", "").toString());
    }

    public static String getMemeUUID(int memeID) {
        return get(Table.MEMES, "WHERE id = " + memeID, "uuid", "").toString();
    }

    public static int getTagID(String tagName) throws SQLException {
        return Integer.parseInt(get(Table.TAGS, "WHERE name = '" + tagName + "'", "id").toString());
    }

    public static String getTagName(int tagID) {
        return get(Table.TAGS, "WHERE id = " + tagID, "name", "").toString();
    }

    public static List<String> getTagsFromMeme(String uuid) {
        List<String> tags = new ArrayList<>();
        try {
            ResultSet set = MemeMachine.getDatabase().select(Table.MEME_TAGS.getName(), "WHERE meme_id = " + getMemeID(uuid));
            while (set.next()) tags.add(getTagName(set.getInt("tag_id")));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tags;
    }

    public static List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        try {
            ResultSet set = MemeMachine.getDatabase().select(Table.TAGS.getName(), "ORDER BY id ASC");
            while (set.next()) tags.add(set.getString("name"));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tags;
    }

    /* Adders and Removers */
    public static void addMeme(String uuid) {
        MemeMachine.getDatabase().insertInto(Table.MEMES.getName(), "uuid", "'" + uuid + "'");
    }

    public static void removeMeme(String uuid) {
        int id = getMemeID(uuid);
        MemeMachine.getDatabase().delete(Table.MEMES.getName(), "uuid = '" + uuid + "'");
        MemeMachine.getDatabase().delete(Table.MEME_TAGS.getName(), "meme_id = '" + id + "'");
    }

    public static void addTag(String tagName) {
        MemeMachine.getDatabase().insertInto(Table.TAGS.getName(), "name", "'" + tagName + "'");
    }

    public static void removeTag(String tagName) {
        try {
            int id = getTagID(tagName);
            MemeMachine.getDatabase().delete(Table.TAGS.getName(), "name = '" + tagName + "'");
            MemeMachine.getDatabase().delete(Table.MEME_TAGS.getName(), "tag_id = '" + id + "'");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void addTagToMeme(String uuid, String tagName) throws SQLException {
        addTagToMeme(getMemeID(uuid), getTagID(tagName));
    }

    public static void addTagToMeme(int memeID, int tagID) {
        MemeMachine.getDatabase().insertInto(Table.MEME_TAGS.getName(), "meme_id, tag_id", memeID + ", " + tagID);
    }

    public static void removeTagsFromMeme(String uuid) {
        MemeMachine.getDatabase().delete(Table.MEME_TAGS.getName(), "meme_id = " + getMemeID(uuid));
    }
}
