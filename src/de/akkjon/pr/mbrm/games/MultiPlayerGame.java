package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MultiPlayerGame extends Game {

    final ArrayList<Long> players = new ArrayList<>();


    protected MultiPlayerGame(long serverId) {
        super(serverId);
    }

    @Override
    void initGameReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
                Message message = checkMessage(event);
                if (message == null) return;

                if (event.getReactionEmote().getName().equals("👍")) {
                    addPlayer(event.getMember().getIdLong());
                } else if (event.getReactionEmote().getName().equals("➡")) {
                    startGame();
                } else if (event.getReactionEmote().getName().equals("❌")) {
                    channel.delete().complete();
                }
            }

            @Override
            public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
                Message message = checkMessage(event);
                if (message == null) return;

                if (event.getReactionEmote().getName().equals("👍")) {
                    removePlayer(event.getMember().getIdLong());
                }
            }
        });
    }

    void addPlayer(long id) {
        if (!players.contains(id)) players.add(id);
    }

    void removePlayer(long id) {
        if (players.size() <= 1) {
            channel.delete().complete();
            return;
        }
        players.remove(id);
    }

}
