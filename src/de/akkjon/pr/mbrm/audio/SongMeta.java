package de.akkjon.pr.mbrm.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SongMeta {

    private final String path;
    private int byteLength = 0;
    private AudioFileFormat format;
    private File file;

    SongMeta(String path) {
        this.path = path;
        init();
    }

    private void init() {
        file = new File(this.path);
        if(!file.exists()) return;
        try {
            format = AudioSystem.getAudioFileFormat(file);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
            return;
        }

        float bitrate = (format.getFormat().getChannels() * (format.getFormat().getFrameRate() / 1000) * format.getFormat().getSampleSizeInBits());
        this.byteLength = (int) (bitrate / 8 * 20);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof  SongMeta)) return false;
        SongMeta meta = (SongMeta) o;
        return meta.getPath().equals(path);
    }

    public String getPath() {
        return path;
    }

    public AudioInputStream getAudioInputStream() {
        try {
            return AudioSystem.getAudioInputStream(
                    new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            format.getFormat().getSampleRate(),
                            format.getFormat().getSampleSizeInBits(),
                            format.getFormat().getChannels(),
                            format.getFormat().getFrameSize(),
                            format.getFormat().getFrameRate(),
                            true
                    ),
                    AudioSystem.getAudioInputStream(file)
            );
        } catch (Exception e) {
            return null;
        }
    }

    public int getByteLength() {
        return byteLength;
    }
}
