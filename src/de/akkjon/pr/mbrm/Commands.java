package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.List;

public class Commands {

    public static List<Command> commands;
    static {
        commands.add(new Command(new String[]{"addchannel"}, true, new CommandRunnable() {
            @Override
            public void run(MessageReceivedEvent event, String[] args) {
                if (!ServerWatcher.isChannelRegistered(event.getChannel().getIdLong())) {
                    try {
                        ServerWatcher.getInstance(event.getGuild().getIdLong()).channels = Storage.addChannel(serverId, event.getChannel().getIdLong());
                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                Locales.getString("msg.onAddChannelSuccess"))).complete();
                    } catch (IOException e) {
                        event.getChannel().sendMessage(Main.getEmbedMessage("Internal error",
                                Locales.getString("error.internalError"))).complete();
                        e.printStackTrace();
                    }

                } else {
                    event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                            Locales.getString("msg.onAddChannelError"))).complete();
                }
            }
        }));
    }
}
