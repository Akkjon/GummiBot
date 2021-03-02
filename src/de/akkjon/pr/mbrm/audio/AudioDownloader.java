package de.akkjon.pr.mbrm.audio;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class AudioDownloader {

    public static List<String> mediaExtensions = Arrays.asList("mp3", "wav");

    public static void init() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
                //TODO Usercheck
                List<Message.Attachment> attachments = event.getMessage().getAttachments();
                if(attachments.size()>0) {
                    for(Message.Attachment attachment: attachments) {
                        String fileName = attachment.getFileName();
                        if(!addSong(attachment, event.getChannel())) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.private.notValid", fileName))).complete();
                        }
                    }
                }
            }
        });
    }

    private static boolean addSong(Message.Attachment attachment, MessageChannel channel) {
        if(attachment.getFileExtension()==null) return false;
        if(mediaExtensions.contains(attachment.getFileExtension().toLowerCase())) {
            String name = attachment.getFileName().substring(0, attachment.getFileName().lastIndexOf("."));
            File folder = new File(Song.SONGS_FOLDER);
            boolean isSet = false;
            if(!folder.exists()) {
                folder.mkdirs();
            } else{
                isSet = Arrays.stream(folder.listFiles())
                        .anyMatch(file -> file.getName().matches(Pattern.quote(name) + "\\..*"));
            }
            if(!isSet) {
                String path = Song.SONGS_FOLDER + attachment.getFileName();
                attachment.downloadToFile(path).thenAccept(file ->
                        channel.sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.success"),
                            Locales.getString("msg.private.downloaded", file.getName()))).complete());
                return true;
            }
        }
        return false;
    }
}
