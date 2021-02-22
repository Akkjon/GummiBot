package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class TruthOrDare extends SuccessiveMultiPlayerGame {

    private boolean isChoosing;

    private List<String> remainingListTruth;
    private List<String> remainingListDare;

    private static final String fileName = "tod";


    public TruthOrDare(long serverID) {
        super(serverID);
        createTextChannel(Locales.getString("msg.games.tod.channelName"));
        sendStartMessage(fileName);
        try {
            loadRemainingDare();
            loadRemainingTruth();
        } catch (IOException e) {
            e.printStackTrace();
            this.channel.delete();
        }
        initReactionListeners();
    }

    public String getTruth() throws IOException {
        if (this.remainingListTruth.size() == 0) {
            loadRemainingTruth();
        }
        return getFromLists(this.remainingListTruth);
    }

    public String getDare() throws IOException {
        if (this.remainingListDare.size() == 0) {
            loadRemainingDare();
        }
        return getFromLists(this.remainingListDare);
    }

    public static boolean addTruth(String element, long serverId) throws IOException {
        return add(element, "truth", serverId, fileName + ".txt");
    }

    public static boolean addDare(String element, long serverId) throws IOException {
        return add(element, "dare", serverId, fileName + ".txt");
    }





    long getCurrentPlayer() {
        return lastPlayer;
    }

    public void initReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {

                Message message = checkMessage(event);
                if (message == null) return;

                String title = message.getEmbeds().get(0).getTitle();

                if (title.equals(Locales.getString("msg.games.tod.truthTitle"))
                        || title.equals(Locales.getString("msg.games.tod.dareTitle"))) {
                    if (event.getReactionEmote().getName().equals("➡")) {
                        if (isChoosing) return;
                        sendMessage();
                    }
                } else if (title.startsWith(Locales.getString("msg.games.tod.gamestartTitlePrefix"))
                        || title.equals(Locales.getString("msg.games.tod.nextPlayerTitle"))) {
                    if (event.getMember().getIdLong() == (getCurrentPlayer())) {
                        isChoosing = false;
                        if (event.getReactionEmote().getName().equals("1️⃣")) {
                            sendTruth(channel);
                        } else if (event.getReactionEmote().getName().equals("2️⃣")) {
                            sendDare(channel);
                        }
                    }
                }
            }
        });
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

        Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.tod.gamestartTitlePrefix")
                        + Locales.getString("msg.games.tod.gamestartTitleSuffix", players.size()),
                Locales.getString("msg.games.tod.gameTruthOrDareQuestion", getNextPlayer()))).complete();
        isChoosing = true;
        msg.addReaction("1️⃣").queue();
        msg.addReaction("2️⃣").queue();
    }

    @Override
    void sendMessage() {
        Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.tod.nextPlayerTitle"),
                Locales.getString("msg.games.tod.gameTruthOrDareQuestion", getNextPlayer()))).complete();
        isChoosing = true;
        msg.addReaction("1️⃣").queue();
        msg.addReaction("2️⃣").queue();
    }

    void sendTruth(MessageChannel channel) {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.tod.truthTitle"), getTruth())).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendDare(MessageChannel channel) {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.tod.dareTitle"), getDare())).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRemainingTruth() throws IOException {
        this.remainingListTruth = loadRemaining("truth", fileName);
    }

    private void loadRemainingDare() throws IOException {
        this.remainingListDare = loadRemaining("dare", fileName);
    }
}
