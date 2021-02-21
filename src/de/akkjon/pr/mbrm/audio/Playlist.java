package de.akkjon.pr.mbrm.audio;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private final List<Song> songs = new ArrayList<>();
    private int nowPlayingIndex = -1;

    public void addSong(Song song) {
        if(songs.contains(song)) return;
        songs.add(song);
    }

    public void removeSong(Song song) {
        int index = songs.indexOf(song);
        if(index >= 0) {
            if(index <= nowPlayingIndex) {
                nowPlayingIndex--;
            }
            songs.remove(song);
        }
    }

    public List<Song> getSongs() {
        return this.songs;
    }

    public Song getCurrent() {
        return songs.get(nowPlayingIndex);
    }

    public Song getNext() {
        return songs.get(++nowPlayingIndex);
    }

    public int getSize() {
        return songs.size();
    }
}
