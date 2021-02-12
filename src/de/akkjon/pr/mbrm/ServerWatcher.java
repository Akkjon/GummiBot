package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerWatcher {

    private static final List<WeakReference<ServerWatcher>> serverWatchers = new ArrayList<>();

    private final long serverId;
    Long[] channels;
    private final Insult insult;
    private final String changelogFilePath;

    public static void logError(String throwable) {
        if (Main.jda.getStatus() != JDA.Status.CONNECTED) return;
        for (WeakReference<ServerWatcher> serverWatcher : serverWatchers) {
            ServerWatcher watcher = serverWatcher.get();
            if (watcher != null) watcher.logErrorInternal(throwable);
        }
    }

    public static void sendChangelog(String changelog) {
        if (Main.jda.getStatus() != JDA.Status.CONNECTED) return;
        for (WeakReference<ServerWatcher> serverWatcher : serverWatchers) {
            ServerWatcher watcher = serverWatcher.get();
            if (watcher != null) watcher.sendChangelogInternal(changelog);
        }
    }

    public ServerWatcher(long serverId) throws AlreadyBoundException {
        for (WeakReference<ServerWatcher> weakReference : serverWatchers) {
            if (weakReference.get().getGuildId() == serverId) {
                throw new AlreadyBoundException("Server Watcher already exists");
            }
        }
        this.serverId = serverId;
        serverWatchers.add(new WeakReference<>(this));
        this.insult = new Insult(serverId);
        this.changelogFilePath = Storage.rootFolder + serverId + File.separator + "changelogChannels.txt";
        initChannels();
        initCommandListeners();
        new QuoteOfTheDay(serverId);
    }


    public long getGuildId() {
        return this.serverId;
    }

    public Insult getInsult() {
        return this.insult;
    }

    private void initChannels() {
        this.channels = Storage.getChannels(serverId);
    }

    boolean isChannelRegistered(long channelId) {
        for (Long long1 : channels) {
            if (long1 == channelId) return true;
        }
        return false;
    }

    private void initCommandListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                if (event.isFromGuild()) {
                    if (event.getGuild().getIdLong() == serverId) {

                        boolean isPermitted = Commands.isAdmin(event);

                        if (event.getMessage().getContentRaw().equals(Commands.COMMAND_PREFIX + "disable") && isPermitted) {
                            Main.isEnabled = false;
                            Main.removeStatus();
                        } else if (event.getMessage().getContentRaw().equals(Commands.COMMAND_PREFIX + "enable") && isPermitted) {
                            Main.isEnabled = true;
                            Main.setStatus();
                        }
                        if (!Main.isEnabled) return;

                        long channelId = event.getChannel().getIdLong();

                        long selfUserId = Main.jda.getSelfUser().getIdLong();
                        if (event.getMessage().getMentionedMembers().stream().anyMatch(member -> member.getIdLong() == selfUserId)) {
                            event.getChannel().sendMessage(Locales.getString("msg.onMentioned")).complete();
                        }


                        Commands.runCommands(event, ServerWatcher.this);

                        if (isChannelRegistered(channelId)) {
                            removeMessages(event.getChannel(), event.getMessageId());
                        }
                    }
                }
            }
        });
    }

    private void removeMessages(MessageChannel channel, String messageId) {
        if (!Main.isEnabled) return;
        new Thread(() -> {
            MessageHistory messageHistory = MessageHistory.getHistoryBefore(channel, messageId).complete();
            List<Message> messages = messageHistory.getRetrievedHistory();
            for (int i = 0; i < messages.size() - 1; i++) {
                try {
                    messages.get(i).delete().complete();
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    private void logErrorInternal(String error) {
        String strChannelId;
        try {
            strChannelId = Storage.getFileContent(Storage.rootFolder + serverId + File.separator + "logChannel.txt", "-1");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int intChannelId;
        try {
            intChannelId = Integer.parseInt(strChannelId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        try {
            MessageChannel messageChannel = (MessageChannel) Main.jda.getGuildChannelById(intChannelId);
            if (messageChannel != null) {
                messageChannel.sendMessage("```" + error + "```").complete();
            }
        } catch (Exception ignored) {
        }
    }

    private void sendChangelogInternal(String changelog) {
        Long[] strArrChangelogChannels;
        try {
            strArrChangelogChannels = getChangelogChannelsList();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for(Long strChangelogChannel : strArrChangelogChannels) {
            MessageChannel channel = (MessageChannel) Main.jda.getGuildChannelById(strChangelogChannel);
            if(channel != null)
                channel.sendMessage("```" + changelog + "```").complete();
        }
    }

    public Long[] getChangelogChannelsList() throws IOException {
        String changelogChannels = Storage.getFileContent(changelogFilePath, "[]");
        return Storage.arrayFromString(changelogChannels);
    }

    public void saveChangelogChannelsList(Long[] channels) throws IOException {
        File file = new File(changelogFilePath);
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileWriter writer = new FileWriter(file);
        writer.write(Arrays.toString(channels));
        writer.close();
    }
}