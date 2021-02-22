package de.akkjon.pr.mbrm.audio;

import com.google.gson.JsonObject;
import de.akkjon.pr.mbrm.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Song {
    private final String name;
    private final String path;
    private final SongMeta meta;

    public static String SONGS_FOLDER = Storage.jarFolder + File.separator + "songs" + File.separator;

    public static Song fromJsonObject(JsonObject object) {
        if(object.has("path")) {
            String path = object.get("path").getAsString();
            if(!new File(path).exists()) return null;
            try {
                return new Song(object.get("path").getAsString());
            } catch (FileNotFoundException ignored) {}
        }
        return null;
    }

    public static List<Song> getList() {
        List<Song> list = new ArrayList<>();
        if(!new File(SONGS_FOLDER).exists()) return list;
        for(File file : new File(SONGS_FOLDER).listFiles()) {
            try {
                Song song = new Song(file.getAbsolutePath());
                list.add(song);
            } catch (FileNotFoundException ignored) {}
        }

        return list;
    }

    public static List<Song> fromName(String name) {
        String nameLow = name.toLowerCase();
        List<Song> songs = getList();
        return songs.stream().filter(song -> song.getName().toLowerCase().matches(String.format(".*%s.*", Pattern.quote(nameLow)))).collect(Collectors.toList());
    }

    Song(String path) throws FileNotFoundException {
        this.path = path;
        if(!new File(path).exists()) throw new FileNotFoundException("File " + path + " was not found");
        this.name = path.substring(path.lastIndexOf(File.separator)+1, path.lastIndexOf("."));
        this.meta = new SongMeta(path);
    }

    public JsonObject getElement() {
        JsonObject song = new JsonObject();
        song.addProperty("path", this.path);
        return song;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof  Song)) return false;
        Song song = (Song) o;
        if(!song.getName().equals(name)) return false;
        return song.getPath().equals(name);
    }

    public SongMeta getMeta() {
        return meta;
    }
}
