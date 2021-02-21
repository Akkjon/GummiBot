package de.akkjon.pr.mbrm.audio;

public class AudioManager {

    private Playlist playlist;
    private final long guildId;
    private final AudioSender audioSender;
    private Runnable onSongEnd;


    public AudioManager(long guildId) {
        this.guildId = guildId;
        resetPlaylist();
        initOnSongEnd();
        audioSender = new AudioSender(onSongEnd);
    }

    private void initOnSongEnd() {
        onSongEnd = () -> {
            Song next = playlist.getNext();
            audioSender.playSong(next);
            //TODO Out
        };
    }

    public void start() {
        audioSender.play();
    }

    public void pause() {
        audioSender.pause();
    }


    public long getGuildId() {
        return guildId;
    }

    public void resetPlaylist() {
        playlist = new Playlist();
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof  AudioManager)) return false;
        AudioManager audioSender = (AudioManager) o;
        return audioSender.getGuildId() == guildId;
    }
}
