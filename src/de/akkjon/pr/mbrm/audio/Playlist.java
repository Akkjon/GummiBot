package de.akkjon.pr.mbrm.audio;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Storage;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.naming.NameNotFoundException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Playlist {

    @NotNull
    private static String getPlaylistPath(long guildId, @NotNull String name) {
        return Storage.rootFolder + File.separator + guildId + File.separator + "playlists" + File.separator + name;
    }

    public static List<Playlist> getList(long guildId) {
        File folder = new File(getPlaylistPath(guildId, ""));

        List<Playlist> playlists = new ArrayList<>();
        for(File file : folder.listFiles()) {
            playlists.add(Playlist.getFromName(guildId, file.getName()));
        }

        return playlists;
    }

    public static List<Playlist> fromName(long guildId, String name) {
        return getList(guildId).stream().filter(playlist->playlist.getName().matches(".*" + Pattern.quote(name) + ".*")).collect(Collectors.toList());
    }

    @Nullable
    public static Playlist getFromName(long guildId, @NotNull String name) {
        String path = getPlaylistPath(guildId, name);
        File file = new File(path);
        if(!file.exists()) return null;

        try {
            String content = Storage.getFileContent(path, "");
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(content, JsonObject.class);
            return getFromJson(guildId, object);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Nullable
    public static Playlist getFromJson(long guildId, @NotNull JsonObject object) {
        if(!(object.has("name")
                && object.has("songs")
                && object.has("isLooping")
                && object.has("isShuffling"))) return null;

        Playlist playlist = new Playlist(
                guildId,
                object.get("name").getAsString(),
                object.get("isLooping").getAsBoolean(),
                object.get("isShuffling").getAsBoolean()
        );

        JsonArray songs = object.get("songs").getAsJsonArray();
        for(JsonElement songElement : songs) {
            JsonObject songObject = songElement.getAsJsonObject();
            Song song = Song.fromJsonObject(songObject);
            if(song != null) playlist.addSong(song);
        }
        return playlist;
    }

    private final List<Song> songs = new ArrayList<>();
    private final List<Song> songsRemaining = new ArrayList<>();
    private int nowPlayingIndex = -1;
    private String name;
    private boolean isNameSet;
    private final long guildId;
    private boolean isLooping;
    private boolean isShuffling;

    Playlist(long guildId) {
        this(guildId,"Playlist_" + System.currentTimeMillis());
        this.isNameSet = false;
    }

    Playlist(long guildId, String name) {
        this(guildId, name, false, false);
    }

    Playlist(long guildId, String name, boolean isLooping, boolean isShuffling) {
        this.name = name;
        this.guildId = guildId;
        this.isNameSet = true;
        this.isLooping = isLooping;
        this.isShuffling = isShuffling;
    }

    public int getNowPlayingIndex() {
        return nowPlayingIndex;
    }


    void setNowPlayingIndex(int index) throws  IllegalArgumentException {
        if(index < 0 || index > songs.size()) throw new IllegalArgumentException("");
        this.nowPlayingIndex = index;
        songsRemaining.remove(songs.get(index));
    }

    void addSong(Song song) {
        if(song == null) return;
        if(songs.contains(song)) return;
        songs.add(song);
        songsRemaining.add(song);
    }

    public Song getSong(int index) {
        if(index <0 || index >= songs.size()) return null;
        return songs.get(index);
    }

    void removeSong(int index) {
        if(index >= 0 && index<= songs.size()) {
            if(index < nowPlayingIndex) {
                nowPlayingIndex--;
            }
            songsRemaining.remove(songs.get(index));
            songs.remove(index);
        }
    }

    void removeSong(Song song) {
        int index = songs.indexOf(song);
        removeSong(index);
    }

    public List<Song> getSongs() {
        return new ArrayList<>(this.songs);
    }

    /*void setSongs(Playlist playlist) {
        if(playlist == null) {
            setSongs(new Playlist(guildId));
            return;
        }
        this.nowPlayingIndex = 0;
        this.songs = playlist.songs;
        this.songsRemaining = new ArrayList<>(songs);
    }*/

    void addSongs(Playlist playlist) {
        if(playlist == null) return;
        for(Song song : playlist.getSongs()) {
            this.addSong(song);
        }
    }

    public Song getCurrent() {
        if(nowPlayingIndex >= 0 && nowPlayingIndex<songs.size()) {
            return songs.get(nowPlayingIndex);
        }
        return null;
    }

    public Song getNext() {
        if(songsRemaining.size()==0 && !isLooping) {
            nowPlayingIndex = songs.size();
            return null;
        }
        Song nextSong;
        if(isShuffling) {
            nextSong = songsRemaining.get((int)(Math.random()*songsRemaining.size()));
        } else {
            nextSong = songsRemaining.get(0);
        }
        songsRemaining.remove(nextSong);
        nowPlayingIndex = songs.indexOf(nextSong);

        return getCurrent();
    }

    public int getSize() {
        return songs.size();
    }

    public void setName(String name) {
        this.name = name;
        this.isNameSet = true;
    }

    public String getName() {
        return name;
    }

    public boolean isNameSet() {
        return this.isNameSet;
    }

    public boolean isLooping() {
        return isLooping;
    }

    public void toggleLooping() {setLooping(!isLooping);}

    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    public void toggleShuffling() {setShuffling(!isShuffling);}

    public boolean isShuffling() {
        return isShuffling;
    }

    public void setShuffling(boolean shuffling) {
        isShuffling = shuffling;
    }

    public boolean save(long userId) throws NameNotFoundException, IllegalAccessException {
        if(!isNameSet()) {
            throw new NameNotFoundException("The name has to be set");
        }

        String path = getPlaylistPath(guildId, this.name);
        if(new File(path).exists()) {

            Gson gson = new Gson();
            try {
                JsonObject object = gson.fromJson(Storage.getFileContent(path, ""), JsonObject.class);
                long userIdSaved = object.get("savedBy").getAsLong();

                if(userIdSaved != userId) throw new IllegalAccessException("Playlist saved by another one");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        try {
            Storage.saveFile(path, getJson(userId).toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove(long userId) throws FileNotFoundException, IllegalAccessException {
        String path = getPlaylistPath(guildId, this.name);
        File file = new File(path);
        if(!file.exists()) throw new FileNotFoundException("Not saved");

        Gson gson = new Gson();
        try {
            JsonObject object = gson.fromJson(Storage.getFileContent(path, ""), JsonObject.class);
            long userIdSaved = object.get("savedBy").getAsLong();

            if(userIdSaved != userId) throw new IllegalAccessException("Not Permitted");

            return file.delete();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private JsonObject getJson(long userId) {
        JsonObject playlist = new JsonObject();
        playlist.addProperty("name", name);
        playlist.addProperty("isLooping", isLooping);
        playlist.addProperty("isShuffling", isShuffling);
        playlist.addProperty("savedBy", userId);

        JsonArray songs = new JsonArray();
        for(Song song : this.songs) {
            songs.add(song.getElement());
        }
        playlist.add("songs", songs);

        return playlist;
    }

    public void sendToChannel(MessageChannel channel) {
        StringBuilder out = new StringBuilder(getName()).append("\n");

        out.append("Looping: ").append(Locales.getString(isLooping ? "msg.primitive.true.on" : "msg.primitive.false.off")).append("\n");
        out.append("Shuffling: ").append(Locales.getString(isShuffling ? "msg.primitive.true.on" : "msg.primitive.false.off")).append("\n");


        for(int i = 0; i<songs.size(); i++) {
            out.append(i == nowPlayingIndex ? "->" : "  ");
            out.append(String.format(" [ %d ] %s", i, songs.get(i).getName()));
        }
        channel.sendMessage("```" + out + "```").complete();
    }
}
