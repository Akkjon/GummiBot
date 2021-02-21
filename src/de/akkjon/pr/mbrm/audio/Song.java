package de.akkjon.pr.mbrm.audio;

import java.io.File;

public class Song {
    private final String name;
    private final String path;
    private final SongMeta meta;

    Song(String path) {
        this.path = path;
        this.name = path.substring(path.lastIndexOf(File.separator)+1, path.lastIndexOf("."));
        this.meta = new SongMeta(path);
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
        if(!song.getPath().equals(name)) return false;

        return true;
    }

    public SongMeta getMeta() {
        return meta;
    }
}
