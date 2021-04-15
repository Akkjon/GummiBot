package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class WuerdestDuEher extends SuccessiveMultiPlayerGame {

    private List<String> remainingList;

    private static final String fileName = "wde";

    public WuerdestDuEher(long serverId) {
        super(serverId);
        createTextChannel(Locales.getString("msg.games.wde.channelName"));
        sendStartMessage(fileName);
        try {
            loadRemainingList();
        } catch (IOException e) {
            e.printStackTrace();
            this.channel.delete();
        }
    }

    public String getMessage() throws IOException {
        if (this.remainingList.size() == 0) {
            loadRemainingList();
        }
        return getFromLists(this.remainingList);
    }

    public static boolean addMessage(String element, long serverId) throws IOException {
        return add(element, "questions", serverId, fileName + ".txt");
    }

    @Override
    void startGame() {
        if (isStarted) {
            sendMessage();
            return;
        }
        if (players.size() <= 1) {
            channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.error.noPlayersTitle"),
                    Locales.getString("msg.games.error.noPlayersMessage"))).complete();
            return;
        }
        isStarted = true;
        sendMessage();
    }

    @Override
    void sendMessage() {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.wde.title"),
                    "<@" + getNextPlayer() + ">\n..." + getMessage())).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRemainingList() throws IOException {
        this.remainingList = loadRemaining("questions", fileName);
    }
}
