package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Werwolf extends MultiPlayerGame {

    private int progress;
    private Player[] players;

    private interface Player {
        long id = 0;

        void nightAction();

        void dayAction();
    }

    private class Dorfbewohner implements Player {

        @Override
        public void nightAction() {

        }

        @Override
        public void dayAction() {

        }
    }

    private class Amor extends Dorfbewohner implements Player {

        @Override
        public void nightAction() {

        }

        @Override
        public void dayAction() {

        }
    }


    private static final String fileName = "ww";

    public Werwolf(long serverID) {
        super(serverID);
        createTextChannel(Locales.getString("msg.games.werwolf.channelName"));
        sendStartMessage(fileName);
        initReactionListeners();
    }

    public void initReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
                Message message = checkMessage(event);
                if (message == null) return;

                if (event.getReactionEmote().getName().equals("➡")) {
                    startGame();
                } else if (event.getReactionEmote().getName().equals("❌")) {
                    channel.delete().complete();
                }
            }
        });
    }
}
