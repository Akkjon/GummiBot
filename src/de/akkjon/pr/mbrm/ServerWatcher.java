package de.akkjon.pr.mbrm;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.AlreadyBoundException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.akkjon.pr.mbrm.games.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerWatcher {

    private static final List<WeakReference<ServerWatcher>> serverWatchers = new ArrayList<>();

    private final long serverId;
    private Long[] channels;
    private final String prefix = "~";
    private final Insult insult;

    public ServerWatcher(long serverId) throws AlreadyBoundException {
        for (WeakReference<ServerWatcher> weakReference : serverWatchers) {
            if (weakReference.get().getGuildId() == serverId) {
                throw new AlreadyBoundException("Server Watcher already exists");
            }
        }
        this.serverId = serverId;
        serverWatchers.add(new WeakReference<>(this));
        this.insult = new Insult(serverId);
        initChannels();
        initCommandListeners();
        new QuoteOfTheDay(serverId);
    }


    public long getGuildId() {
        return this.serverId;
    }

    private void initChannels() {
        this.channels = Storage.getChannels(serverId);
    }

    private boolean isChannelRegistered(long channelId) {
        for (Long long1 : channels) {
            if (long1 == channelId) return true;
        }
        return false;
    }

    private void initCommandListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                if (event.isFromGuild()) {
                    if (event.getGuild().getIdLong() == serverId) {
                        long channelId = event.getChannel().getIdLong();
                        String content = event.getMessage().getContentRaw();
                        long selfUserId = Main.jda.getSelfUser().getIdLong();
                        if(event.getMessage().getMentionedMembers().stream().filter(member -> member.getIdLong()==selfUserId).count()>0) {
                            event.getChannel().sendMessage(Locales.getString("msg.onMentioned")).complete();
                        }
                        if (content.startsWith(prefix)) {
                            content = content.substring(prefix.length());
                            String[] args = Arrays.stream(content.split(" ")).filter(e -> e.length() > 0).toArray(String[]::new);
                            if (args.length == 0) {
                                return;
                            }
                            switch (args[0].toLowerCase()) {
                                case "addchannel" -> {
                                    if (!isChannelRegistered(channelId)) {
                                        try {
                                            ServerWatcher.this.channels = Storage.addChannel(serverId, channelId);
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                                    Locales.getString("msg.onAddChannelSuccess"))).complete();
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Internal error",
                                                    Locales.getString("error.internalError"))).complete();
                                            e.printStackTrace();
                                        }

                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.onAddChannelError"))).complete();
                                    }
                                }
                                case "removechannel" -> {
                                    if (isChannelRegistered(channelId)) {
                                        try {
                                            ServerWatcher.this.channels = Storage.removeChannel(serverId, channelId);
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                                    Locales.getString("msg.onRemoveChannelSuccess"))).complete();
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Internal error",
                                                    Locales.getString("error.internalError"))).complete();
                                            e.printStackTrace();
                                        }

                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.onRemoveChannelError"))).complete();
                                    }
                                }
                                case "addqotd", "addmotd" -> {
                                    try {
                                        boolean isAdded = QuoteOfTheDay.addQotd(event.getChannel().getIdLong(), event.getGuild().getIdLong());
                                        if (isAdded) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                                    Locales.getString("msg.onAddQotdSuccess"))).complete();
                                        } else {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                    Locales.getString("msg.onAddQotdError"))).complete();
                                        }
                                    } catch (Exception e) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.internalError"),
                                                Locales.getString("error.internalError2"))).complete();
                                        e.printStackTrace();
                                    }
                                }
                                case "removeqotd" -> {
                                    try {
                                        boolean isRemoved = QuoteOfTheDay.removeQotd(event.getChannel().getIdLong(), event.getGuild().getIdLong());
                                        if (isRemoved) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                                    Locales.getString("msg.onRemoveQotdSuccess"))).complete();
                                        } else {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                    Locales.getString("msg.onRemoveQotdError"))).complete();
                                        }
                                    } catch (Exception e) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.internalError"),
                                                Locales.getString("error.internalError3"))).complete();
                                        e.printStackTrace();
                                    }
                                }
                                case "addtruth" -> {
                                    if (args.length > 1) {
                                        String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
                                        try {
                                            boolean isAdded = TruthOrDare.addTruth(newElement, serverId);
                                            if (isAdded) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                                        Locales.getString("msg.onAddTruthSuccess", newElement))).complete();
                                            } else {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                        Locales.getString("msg.onAddTruthError"))).complete();
                                            }
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.internalError"),
                                                    Locales.getString("error.internalError4"))).complete();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.onAddTruthNoArgument"))).complete();
                                    }
                                }
                                case "adddare" -> {
                                    if (args.length > 1) {
                                        String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
                                        try {
                                            boolean isAdded = TruthOrDare.addDare(newElement, serverId);
                                            if (isAdded) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.success"),
                                                        Locales.getString("msg.onAddTruthSuccess", newElement))).complete();
                                            } else {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                        Locales.getString("msg.onAddDareError"))).complete();
                                            }
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.internalError"),
                                                    Locales.getString("error.internalError5"))).complete();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.onAddDareNoArgument"))).complete();
                                    }
                                }
                                case "addquestion" -> {
                                    if (args.length == 1) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.onAddQuestionError1"))).complete();
                                        return;
                                    }
                                    if(args.length < 3) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.onAddQuestionError2"))).complete();
                                        return;
                                    }
                                    String gameName = args[1].toLowerCase();
                                    String newElement = String.join(" ", Arrays.asList(args).subList(2, args.length));

                                    String succesTitle          =   Locales.getString("msg.commands.success");
                                    String succesDescription    =   Locales.getString("msg.commands.addGame.added", newElement);
                                    String ErrorTitle           =   Locales.getString("msg.commands.error");
                                    String ErrorDescription     =   Locales.getString("msg.commands.addGame.alreadyExists");
                                    String InternalErrorTitle   =   Locales.getString("msg.commands.internalError");
                                    String InternalErrorDescription=Locales.getString("msg.commands.addGame.internalError");

                                    switch (gameName) {
                                        case "ihnn" -> {
                                            try {
                                                boolean isAdded = IchHabNochNie.addMessage(newElement, serverId);
                                                if (isAdded) {
                                                    event.getChannel().sendMessage(Main.getEmbedMessage(succesTitle, succesDescription)).complete();
                                                } else {
                                                    event.getChannel().sendMessage(Main.getEmbedMessage(ErrorTitle, ErrorDescription)).complete();
                                                }
                                            } catch (IOException e) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(InternalErrorTitle, InternalErrorDescription)).complete();
                                                e.printStackTrace();
                                            }
                                        }
                                        case "wde" -> {
                                            try {
                                                boolean isAdded = WuerdestDuEher.addMessage(newElement, serverId);
                                                if (isAdded) {
                                                    event.getChannel().sendMessage(Main.getEmbedMessage(succesTitle, succesDescription)).complete();
                                                } else {
                                                    event.getChannel().sendMessage(Main.getEmbedMessage(ErrorTitle, ErrorDescription)).complete();
                                                }
                                            } catch (IOException e) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(InternalErrorTitle, InternalErrorDescription)).complete();
                                                e.printStackTrace();
                                            }
                                        }
                                        case "insult" -> {
                                            try {
                                                boolean isAdded = Insult.addMessage(newElement, serverId);
                                                if (isAdded) {
                                                    event.getChannel().sendMessage(Main.getEmbedMessage(succesTitle, succesDescription)).complete();
                                                } else {
                                                    event.getChannel().sendMessage(Main.getEmbedMessage(ErrorTitle, ErrorDescription)).complete();
                                                }
                                            } catch (IOException e) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(InternalErrorTitle, InternalErrorDescription)).complete();
                                                e.printStackTrace();
                                            }
                                        }
                                        default -> {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                    Locales.getString("msg.commands.addGame.gameNotExists"))).complete();
                                        }
                                    }
                                }
                                case "play" -> {
                                    if (args.length > 1) {
                                        args[1] = args[1].toLowerCase();
                                        switch (args[1]) {
                                            case "tod", "truthordare" -> {
                                                TruthOrDare tod = new TruthOrDare(serverId);
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.games.start"),
                                                        "<#" + tod.getChannelId() + ">")).complete();
                                            }
                                            case "ihnn", "ichhabnochnie" -> {
                                                IchHabNochNie ihnn = new IchHabNochNie(serverId);
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.games.start"),
                                                        "<#" + ihnn.getChannelId() + ">")).complete();
                                            }
                                            case "wde", "würdestdueher" -> {
                                                WuerdestDuEher wde = new WuerdestDuEher(serverId);
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.games.start"),
                                                        "<#" + wde.getChannelId() + ">")).complete();
                                            }
                                            case "blackjack" -> {
                                                BlackJack blackJack = new BlackJack(serverId);
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.games.start"),
                                                        "<#" + blackJack.getChannelId() + ">")).complete();
                                            }
                                            default -> {
                                                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                        Locales.getString("msg.commands.addGame.gameNotExists"))).complete();
                                            }
                                        }
                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                Locales.getString("msg.commands.games.noGame"))).complete();
                                    }
                                }
                                case "throwdice" -> {
                                    if (args.length > 1) {
                                        try {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.games.Dice.name"),
                                                    String.valueOf(Dice.throwDice(Integer.parseInt(args[1]))))).complete();
                                        } catch (NumberFormatException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                                                    Locales.getString("msg.commands.games.Dice.error", args[1]))).complete();
                                        }
                                    } else
                                        event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.games.Dice.name"),
                                                String.valueOf(Dice.throwDice(6)))).complete();
                                }
                                case "insult" -> {
                                    long id;
                                    if (args.length == 1) {
                                        id = event.getAuthor().getIdLong();
                                    } else {
                                        id = event.getMessage().getMentionedMembers().get(0).getIdLong();
                                    }
                                    event.getChannel().sendMessage(Main.getEmbedMessage("A", MessageFormat.format(insult.getMessage(), "<@" + id + ">"))).complete();
                                }
                                case "info" -> {
                                    long uptime = System.currentTimeMillis() - Main.STARTUP_TIME;
                                    double version = Updater.getVersion();

                                    String strUptime = "";
                                    uptime /= 1000;
                                    long sec, min, hour, day;
                                    if((sec = uptime % 60) > 0) {
                                        strUptime = sec + "sek" + strUptime;
                                    }
                                    uptime /= 60;
                                    if((min = uptime % 60) > 0) {
                                        strUptime = min + "min " + strUptime;
                                    }
                                    uptime /= 60;
                                    if((hour = uptime % 60) > 0) {
                                        strUptime = hour + "h " + strUptime;
                                    }
                                    uptime /=24;
                                    if((day = uptime) > 0) {
                                        strUptime = day + "d " + strUptime;
                                    }

                                    String strMsg = "```" +
                                            "-- System-Info --\n" +
                                            "Software-Version: " + version + "\n" +
                                            "Uptime:           " + strUptime + "\n" +
                                            "```";

                                    event.getChannel().sendMessage(strMsg).complete();
                                }
                                default -> {
                                    String helpMsg = Storage.getInternalFile("help.txt");
                                    event.getChannel().sendMessage(helpMsg).complete();
                                }
                            }
                        }

                        if (isChannelRegistered(channelId)) {
                            removeMessages(event.getChannel(), event.getMessageId());
                        }
                    }
                }
            }
        });
    }

    private void removeMessages(MessageChannel channel, String messageId) {
        new Thread(() -> {
            MessageHistory messageHistory = MessageHistory.getHistoryBefore(channel, messageId).complete();
            List<Message> messages = messageHistory.getRetrievedHistory();
            for (int i = 0; i < messages.size() - 1; i++) {
                try {
                    messages.get(i).delete().complete();
                } catch (Exception ignored) {
                }
            }
        }).start();
    }


}
