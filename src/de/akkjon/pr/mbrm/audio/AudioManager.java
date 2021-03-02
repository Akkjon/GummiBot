package de.akkjon.pr.mbrm.audio;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.*;

public class AudioManager {

    public static MessageEmbed getMusicEmbed(String description) {
        return Main.getEmbedMessage(Locales.getString("msg.music.title"), description);
    }

    private Playlist playlist;
    private final long guildId;
    private final AudioSender audioSender;
    private Runnable onSongEnd;
    private net.dv8tion.jda.api.managers.AudioManager audioManager = null;
    private MessageChannel messageChannel = null;
    private Message nowPlayingMessage = null;

    public AudioManager(long guildId) {
        this.guildId = guildId;
        Guild guild = Main.jda.getGuildById(guildId);
        if(guild!=null) audioManager = guild.getAudioManager();
        else new Exception("Audio manager guild returned null for guild " + guildId).printStackTrace(System.err);
        initOnSongEnd();
        audioSender = new AudioSender(onSongEnd);
        resetPlaylist();



    }

    private void initOnSongEnd() {
        onSongEnd = () -> {
            Song next = playlist.getNext();
            if(next != null) playSong(next);
            else disconnect();
        };
    }

    public void connect(VoiceChannel channel) {
        if(audioManager==null) return;
        if(!audioManager.isConnected()) {
            audioManager.openAudioConnection(channel);
        }
    }

    public void disconnect() {
        if(audioManager==null) return;
        if(audioManager.isConnected()) {
            pause();
            audioManager.closeAudioConnection();
            resetPlaylist();
            messageChannel = null;
            if(nowPlayingMessage != null) nowPlayingMessage.delete().complete();
            nowPlayingMessage = null;
        }
    }

    public boolean isConnected() {
        if(audioManager==null) return false;
        return audioManager.isConnected();
    }

    public boolean isStarted() {
        if(audioManager==null) return false;
        return isConnected() && audioSender.isPlaying() && audioManager.getSendingHandler() != null;
    }

    public void start() {
        if(audioManager==null) return;
        audioManager.setSendingHandler(audioSender);
        audioSender.play();
    }

    public void pause() {
        if(audioManager==null) return;
        audioManager.setSendingHandler(null);
        audioSender.pause();
    }


    public long getGuildId() {
        return guildId;
    }

    public void resetPlaylist() {
        pause();
        playlist = new Playlist(guildId);
    }

    public void addSong(Song song, VoiceChannel channel, MessageChannel messageChannel) {
        this.playlist.addSong(song);
        addStart(channel, messageChannel, song.getName());
    }

    public void addPlaylist(Playlist playlist, VoiceChannel channel, MessageChannel messageChannel) {
        this.playlist.addSongs(playlist);
        addStart(channel, messageChannel, playlist.getName());
    }

    private void addStart(VoiceChannel channel, MessageChannel messageChannel, String name) {
        if(this.messageChannel == null) {
            this.messageChannel = messageChannel;
        }
        if(!isConnected()) {
            connect(channel);
        }
        if(!isStarted()) {
            playSong(playlist.getNext());
        } else {
            this.messageChannel.sendMessage(
                    AudioManager.getMusicEmbed(Locales.getString("msg.music.queued", name))).complete();
        }
    }

    private void playSong(Song song) {
        audioSender.playSong(song);
        start();
        if(nowPlayingMessage != null) nowPlayingMessage.delete().complete();
        messageChannel.sendMessage(
                AudioManager.getMusicEmbed(Locales.getString("msg.music.nowPlaying", song.getName()))).complete();
    }

    public void jumpToSong(int index) throws IllegalArgumentException {
        playlist.setNowPlayingIndex(index);
        audioSender.playSong(playlist.getCurrent());
    }

    public void removeSong(Song song) {
        boolean nextSong = playlist.getCurrent().equals(song);
        playlist.removeSong(song);
        if(playlist.getSize()==0) {
            disconnect();
            return;
        }
        if(nextSong) {
            audioSender.playSong(playlist.getCurrent());
        }
    }

    public void removeSong(int index) {
        removeSong(playlist.getSong(index));
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public VoiceChannel getChannel() {
        if(audioManager==null) return null;
        return audioManager.getConnectedChannel();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof  AudioManager)) return false;
        AudioManager audioSender = (AudioManager) o;
        return audioSender.getGuildId() == guildId;
    }
}
