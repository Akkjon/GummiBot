package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class IchHabNochNie extends Game {

    private List<String> remainingList;

    private static final String fileName = "ihnn";

    public IchHabNochNie(long serverID) {
        super(serverID);
        this.channel = Main.jda.getCategoryById(802719239723024414L).createTextChannel(Locales.getString("msg.games.ihnn.channelName")).complete();
        /*Message message = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.ihnn.title", "👍", "➡", "❌"),
                Locales.getString("msg.games.ihnn.start"))).complete();
        message.addReaction("➡").queue();
        message.addReaction("❌").queue();
        message.pin().complete();*/
        sendStartMessage(fileName);
        try {
            loadRemainingList();
        } catch (IOException e) {
            e.printStackTrace();
            this.channel.delete();
        }
        initReactionListeners();
    }

    public String getMessage() throws IOException {
        if(this.remainingList.size()==0) {
            loadRemainingList();
        }
        return getFromLists(this.remainingList);
    }

    public static boolean addMessage(String element, long serverId) throws IOException {
        return add(element, "message", serverId, fileName + ".txt");
    }

    public void initReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
            Message message = MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId());

            //skip if message was not from bot or reaction was from bot
            if(!shouldReactToMessage(event, message)) {
                return;
            }

            if (message.getEmbeds().size() != 0) {
                String title = message.getEmbeds().get(0).getTitle();

                if (title.equals(Locales.getString("msg.games.ihnn.title"))) {
                    if (event.getReactionEmote().getName().equals("➡")) {
                        sendMessage();
                    } else if (event.getReactionEmote().getName().equals("❌")) {
                        channel.delete().complete();
                    }
                }
            }
            }
        });
    }

    void sendMessage() {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.ihnn.title"), "..." + getMessage())).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRemainingList() throws IOException {
        this.remainingList = loadRemaining("questions", fileName);
    }
}
