package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

interface WerwolfCallback {
    void run(String answer);

}

public class Werwolf extends MultiPlayerGame {

    private int progress;
    private final ArrayList<Player> playersArray = new ArrayList<>();

    private final ArrayList<Player> hasReacted = new ArrayList<>();

    private interface Player {

        void action();

        void sendMessage(long id, String message, WerwolfCallback callback);
    }

    private class Everyone implements Player {
        private final long id;

        long getId() {
            return this.id;
        }

        Everyone(long id) {
            this.id = id;
        }

        @Override
        public void action() {

        }

        @Override
        public void sendMessage(long id, String message, WerwolfCallback callback) {

        }
    }

    private class Dorfbewohner extends Everyone {



        Dorfbewohner(long id) {
            super(id);
        }

        @Override
        public void action() {
            switch (progress) {
                case 0:
                    sendMessage(getId(), "Du kannst gar nix.", System.out::println);
            }
        }

        @Override
        public void sendMessage(long id, String message, WerwolfCallback callback) {
            PrivateChannel channel = Objects.requireNonNull(Main.jda.getUserById(id)).openPrivateChannel().complete();
            channel.sendMessage(message).queue();
            callback.run("");
        }
    }

    private class Amor extends Dorfbewohner {

        Amor(long id) {
            super(id);
        }

        @Override
        public void action() {

        }

        @Override
        public void sendMessage(long id, String message, WerwolfCallback callback) {
            super.sendMessage(id, message, callback);
        }
    }

    private class SuperWerwolf extends Everyone {

        SuperWerwolf(long id) {
            super(id);
        }

        @Override
        public void action() {
            switch (progress) {
                case 0:
                    sendMessage(getId(), "Wen willst du töten?", System.out::println);
            }
        }

        public void sendMessage(long id, String message, WerwolfCallback callback) {
            PrivateChannel channel = Objects.requireNonNull(Main.jda.getUserById(id)).openPrivateChannel().complete();
            channel.sendMessage(message).queue();
            Main.jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
                    Message answer = MessageHistory.getHistoryAround(event.getChannel(), event.getMessageId()).complete().getMessageById(event.getMessageId());
                    if (answer != null) {
                        callback.run(answer.getContentRaw());
                    } else callback.run("HELP. ERROR");
                }
            });
            callback.run("a");
        }
    }

    public Werwolf(long serverID) {
        super(serverID);
        createTextChannel(Locales.getString("msg.games.werwolf.channelName"));
        sendStartMessage("werwolf");
    }

    @Override
    void startGame() {
        for (long player : players) {
            SuperWerwolf db = new SuperWerwolf(player);
            playersArray.add(db);
        }

        for (Player player : playersArray) {
            player.action();
        }

        progress++;

    }
}
