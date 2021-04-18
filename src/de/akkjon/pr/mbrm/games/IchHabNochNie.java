package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class IchHabNochNie extends Game {

    private List<String> remainingList;

    private static final String fileName = "ihnn";

    public IchHabNochNie(long serverID) {
        super(serverID);
        createTextChannel(Locales.getString("msg.games.ihnn.channelName"));
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
        return add(element, "message", serverId, fileName + ".txt");
    }

    @Override
    void startGame() {
        sendMessage();
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
