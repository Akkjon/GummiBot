package de.akkjon.pr.mbrm.audio;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AudioSender implements AudioSendHandler {

    private boolean playing = false;
    private final Runnable onSongEnd;

    private SongMeta nowPlayingMeta;
    private AudioInputStream stream;

    private List<ByteBuffer> packages;

    AudioSender(Runnable onSongEnd) {
        this.onSongEnd = onSongEnd;
    }

    void playSong(Song song) {
        if(song==null) return;
        nowPlayingMeta = song.getMeta();
        stream = nowPlayingMeta.getAudioInputStream();
        preparePackages();
        play();
    }

    private void preparePackages() {
        packages = new ArrayList<>();
        while(true) {
            try {
                int available = stream.available();
                if(available == 0) {
                    return;
                }

                int toByte = Math.min(nowPlayingMeta.getByteLength(), available);

                byte[] output = new byte[toByte];
                stream.read(output, 0, toByte);

                packages.add(ByteBuffer.wrap(output));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void play() {
        playing = true;
    }

    void pause() {
        playing = false;
    }

    private void onSongEnd() {
        pause();
        onSongEnd.run();
    }

    boolean isPlaying() {
        return playing;
    }



    @Override
    public boolean canProvide() {
        return playing;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        int size = packages.size();
        if(size > 0) {
            ByteBuffer next = packages.get(0);
            packages.remove(0);
            if(size==1) {
                onSongEnd();
            }
            return next;
        } else {
            playing = false;
        }
        return null;
    }
}
