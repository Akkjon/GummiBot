package de.akkjon.pr.mbrm.games;

import com.google.gson.JsonArray;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

public class IchHabNochNie extends Game {

    private boolean isStarted = false;


    public IchHabNochNie(long serverID) {
        super(serverID);
        this.channel = Main.jda.getCategoryById(802719239723024414L).createTextChannel("Ich-hab-noch-nie").complete();
        Message message = channel.sendMessage(Main.getEmbedMessage("Ich hab noch nie",
                "Who wants to play a game?\n" +
                        "Click ➡ to start the game.\n" +
                        "Click ❌ to end the current game.")).complete();
        message.addReaction("➡").queue();
        message.addReaction("❌").queue();
        message.pin().complete();
        initReactionListeners();
    }

    public static String getMessage(long serverId) throws IOException {
        JsonArray global = getGlobal("questions", "ihnn.json");
        JsonArray server = getServer("questions", serverId, "ihnn.txt");
        return getFromLists(global, server);
    }

    public static boolean addMessage(String element, long serverId) throws IOException {
        return add(element, "message", serverId, "ihnn.txt");
    }

    public void initReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
                Message message = MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId());

                //skip if message was not from bot or reaction was from bot
                User bot = event.getJDA().getSelfUser();
                if (!message.getAuthor().equals(bot)) {
                    return;
                }
                if (event.getMember().getUser().equals(bot)) {
                    return;
                }

                if (message.getEmbeds().size() != 0) {
                    String title = message.getEmbeds().get(0).getTitle();

                    if (title.equals("Ich hab noch nie")) {
                        if (event.getReactionEmote().getName().equals("➡")) {
                            startGame();
                        } else if (event.getReactionEmote().getName().equals("❌")) {
                            channel.delete().complete();
                        }
                    }
                }
            }
        });
    }

    private void startGame() {
        if (isStarted) return;
        isStarted = true;
        sendMessage();
    }

    void sendMessage() {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage("Ich hab noch nie", "..." + getMessage(guildId))).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}