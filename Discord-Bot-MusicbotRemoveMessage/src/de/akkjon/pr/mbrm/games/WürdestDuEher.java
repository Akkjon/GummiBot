package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WürdestDuEher extends Game{

    final ArrayList<Long> players = new ArrayList<>();
    private long lastPlayer;
    private boolean isStarted = false;

    private List<String> remainingList;
    private static String fileName = "wde";

    public WürdestDuEher(long serverId) {
        super(serverId);
        this.channel = Main.jda.getCategoryById(802719239723024414L).createTextChannel("Würdest-du-eher").complete();
        Message message = channel.sendMessage(Main.getEmbedMessage("Würdest du eher",
                "Who wants to play a game?\n" +
                        "React with 👍 to enter the game.\n" +
                        "Click ➡ to start the game.\n" +
                        "Click ❌ to end the current game.")).complete();
        message.addReaction("👍").queue();
        message.addReaction("➡").queue();
        message.addReaction("❌").queue();
        message.pin().complete();
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
        return add(element, "questions", serverId, fileName+".txt");
    }

    void addPlayer(Long id) {
        if (!players.contains(id)) players.add(id);
    }

    void removePlayer(long id) {
        if (players.size() <= 1) {
            channel.delete().complete();
            return;
        }
        if (this.lastPlayer == id) sendMessage();
        players.remove(id);
    }

    long getNextPlayer() {
        int indexOflastPlayer = players.indexOf(lastPlayer);
        if (++indexOflastPlayer >= players.size()) indexOflastPlayer = 0;
        this.lastPlayer = players.get(indexOflastPlayer);
        return this.lastPlayer;
    }

    long getCurrentPlayer() {
        return lastPlayer;
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

                    if (title.equals("Würdest du eher")) {
                        if (event.getReactionEmote().getName().equals("➡")) {
                            sendMessage();
                        } else if (event.getReactionEmote().getName().equals("❌")) {
                            channel.delete().complete();
                        }
                    }
                }
            }

            @Override
            public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
                Message message = MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId());

                //skip if message was not from bot
                if (!message.getAuthor().equals(event.getJDA().getSelfUser())) {
                    return;
                }

                if (message.getEmbeds().size() != 0) {
                    String title = message.getEmbeds().get(0).getTitle();
                    if (title.equals("Würdest du eher")) {
                        if (event.getReactionEmote().getName().equals("👍")) {
                            removePlayer(event.getMember().getIdLong());
                        }
                    }
                }
            }
        });
    }

    private void startGame() {
        if (players.size() <= 1) {
            channel.sendMessage(Main.getEmbedMessage("Nope!", "Get yourself some friends nigga.")).complete();
            return;
        }
        if (isStarted) return;
        isStarted = true;
        sendMessage();
    }

    void sendMessage() {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage("Würdest du eher", "<@" + getNextPlayer() + ">\n..." + getMessage())).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRemainingList() throws IOException {
        this.remainingList = loadRemaining("questions", fileName);
    }
}
