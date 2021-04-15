package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

interface BlackjackCallback {
    void run(int number);
}

public class BlackJack extends Game {

    private class Player {
        int number;

        void drawCard(boolean isDealer, BlackjackCallback callback) {
            int value = Dice.throwDice(13);
            if (value == 1) {
                if (!isDealer) {
                    Message msg = channel.sendMessage("Du hast ein Ass gezogen. Soll es den Wert 1 oder 11 haben?").complete();
                    msg.addReaction("1️⃣").complete();
                    msg.addReaction("2️⃣").complete();
                    Main.jda.addEventListener(new ListenerAdapter() {
                        @Override
                        public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
                            if (msg == MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId())) {

                                int number = 1;
                                switch (event.getReactionEmote().getName()) {
                                    case "1️⃣":
                                        number = 1;
                                        break;
                                    case "2️⃣":
                                        number = 11;
                                }
                                channel.deleteMessageById(event.getMessageId());
                                callback.run(number);
                            }
                        }
                    });
                    return;
                } else {
                    if (number >= 11)
                        number += 1;
                    else
                        number += 11;
                }
            }
            if (value >= 11 && value <= 13) {
                number = +10;
            } else {
                number += value;
            }
            callback.run(value);
        }
    }

    public BlackJack(long serverID) {
        super(serverID);
        createTextChannel(Locales.getString("msg.games.blackJack.channelName"));
        sendStartMessage("blackJack");
        initReactionListeners();
    }

    Player player = new Player();
    Player dealer = new Player();

    private static String getCardString(int card) {
        return Locales.getString("msg.games.blackJack.cards." + card);
    }

    public void initReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
                Message message = checkMessage(event);
                if (message == null) return;

                String title = message.getEmbeds().get(0).getTitle();

                if (title.equals(Locales.getString("msg.games.blackJack.title"))) {
                    switch (event.getReactionEmote().getName()) {
                        case "➡":
                            newGame();
                            break;
                        case "❌":
                            channel.delete().complete();
                            break;
                        case "🆕":
                            msgDrawCard();
                            break;
                        case "⭕":
                            msgSkip();
                            break;
                    }
                }
            }
        });
    }

    private void msgSkip() {
        do {
            dealer.drawCard(true, number -> channel.sendMessage("Dealer zieht..." + getCardString(number)).complete());
        } while ((player.number >= dealer.number) && (dealer.number <= 14));

        String text;

        if ((dealer.number >= 22)) {
            text = "Gewonnen! Dealer hat sich überkauft.";
        } else if (dealer.number < player.number) {
            text = "Gewonnen! Der Dealer hat " + dealer.number + " Punkte und damit weniger als du.";
        } else if (dealer.number == player.number) {
            text = "Unentschieden! Dealer hat gleich viele Punkte wie du.";
        } else {
            text = "Looser! Der Dealer hat " + dealer.number + " Punkte und damit mehr als du.";
        }
        Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), text + "Klicke ➡ für ein neues Spiel.")).complete();
        msg.addReaction("➡").complete();
    }

    private void newGame() {
        dealer.number = 0;
        player.number = 0;
        dealer.drawCard(true, number -> {
            channel.sendMessage("Start! Der Dealer hat " + getCardString(number) + " vor sich.").complete();
            Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), "Ziehe eine Karte mit 🆕")).complete();
            msg.addReaction("🆕").complete();
        });
    }

    private void msgDrawCard() {
        player.drawCard(false, card -> {
            Message msg;

            if (player.number >= 22) {
                msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), "Looser! Du hast " + getCardString(card) + " gezogen und damit " + player.number + " Punkte. Klicke ➡ für ein neues Spiel.")).complete();
                msg.addReaction("➡").complete();
            } else {
                msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), "Du hast " + getCardString(card) + " gezogen. Damit hast du " + player.number + " Punkte.")).complete();
                msg.addReaction("🆕").complete();
                msg.addReaction("⭕").complete();
            }
        });
    }
}
