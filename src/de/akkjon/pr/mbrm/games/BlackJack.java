package de.akkjon.pr.mbrm.games;

import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BlackJack extends Game {

    private class Player {
        int number;

        int drawCard(boolean isDealer) {
            int value = Dice.throwDice(13);
            number = switch (value) {
                case 11, 12, 13 -> number + 10;
                case 1 -> (isDealer) ? ((number >= 11) ? number + 1 : number + 11) : chooseValueOfAce();
                default -> number + value;
            };
            return value;
        }

        private int chooseValueOfAce() {
            final int[] res = new int[1];
            Message msg = channel.sendMessage("Du hast ein Ass gezogen. Soll es den Wert 1 oder 11 haben?").complete();
            msg.addReaction("1️⃣").complete();
            msg.addReaction("2️⃣").complete();
            Main.jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
                    if (msg == MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId())) {
                        switch (event.getReactionEmote().getName()) {
                            case "1️⃣" -> res[0] = 1;
                            case "2️⃣" -> res[0] = 11;
                        }
                        channel.deleteMessageById(event.getMessageId());
                    }
                }
            });
            return res[0];
        }

    }

    public BlackJack(long serverID) {
        super(serverID);
        this.channel = Main.jda.getCategoryById(802719239723024414L).createTextChannel(Locales.getString("msg.games.blackJack.channelName")).complete();
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
                Message message = MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId());

                //skip if message was not from bot or reaction was from bot
                if (!shouldReactToMessage(event, message)) {
                    return;
                }

                if (message.getEmbeds().size() != 0) {
                    String title = message.getEmbeds().get(0).getTitle();

                    if (title.equals(Locales.getString("msg.games.blackJack.title"))) {
                        switch (event.getReactionEmote().getName()) {
                            case "➡" -> newGame();
                            case "❌" -> channel.delete().complete();
                            case "🆕" -> msgDrawCard();
                            case "⭕" -> msgSkip();
                        }
                    }
                }
            }
        });
    }

    private void msgSkip() {
        do {
            channel.sendMessage("Dealer zieht... " + getCardString(dealer.drawCard(true))).complete();
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
        channel.sendMessage("Start! Der Dealer hat " + getCardString(dealer.drawCard(true)) + " vor sich.").complete();
        Message msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), "Ziehe eine Karte mit 🆕")).complete();
        msg.addReaction("🆕").complete();
    }

    private void msgDrawCard() {
        Message msg;
        int card = player.drawCard(false);
        if (player.number >= 22) {
            msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), "Looser! Du hast " + getCardString(card) + " gezogen und damit " + player.number + " Punkte. Klicke ➡ für ein neues Spiel.")).complete();
            msg.addReaction("➡").complete();
        } else {
            msg = channel.sendMessage(Main.getEmbedMessage(Locales.getString("msg.games.blackJack.title"), "Du hast " + getCardString(card) + " gezogen. Damit hast du " + player.number + " Punkte.")).complete();
            msg.addReaction("🆕").complete();
            msg.addReaction("⭕").complete();
        }
    }
}
