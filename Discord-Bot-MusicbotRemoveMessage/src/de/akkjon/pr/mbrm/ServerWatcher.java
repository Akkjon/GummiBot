package de.akkjon.pr.mbrm;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.akkjon.pr.mbrm.games.Dice;
import de.akkjon.pr.mbrm.games.IchHabNochNie;
import de.akkjon.pr.mbrm.games.TruthOrDare;
import de.akkjon.pr.mbrm.games.WürdestDuEher;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerWatcher {

    private static final List<WeakReference<ServerWatcher>> serverWatchers = new ArrayList<>();

    private final long serverId;
    private Long[] channels;
    private final String prefix = "~";

    public ServerWatcher(long serverId) throws AlreadyBoundException {
        for (WeakReference<ServerWatcher> weakReference : serverWatchers) {
            if (weakReference.get().getGuildId() == serverId) {
                throw new AlreadyBoundException("Server Watcher already exists");
            }
        }
        this.serverId = serverId;
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
                            event.getChannel().sendMessage("Fresse halten, du störst mich in meiner kreativen Phase").complete();
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
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Success", "You wanna make me your slave? Sure...")).complete();
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I am dumb... I encountered an error.")).complete();
                                            e.printStackTrace();
                                        }

                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "What do you want from me, bitch? I am already working here.")).complete();
                                    }
                                }
                                case "removechannel" -> {
                                    if (isChannelRegistered(channelId)) {
                                        try {
                                            ServerWatcher.this.channels = Storage.removeChannel(serverId, channelId);
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Alright, fuck you. Bye")).complete();
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I am dumb... I encountered an error.")).complete();
                                            e.printStackTrace();
                                        }

                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I was never watchning this fookin channel.")).complete();
                                    }
                                }
                                case "addqotd" -> {
                                    try {
                                        boolean isAdded = QuoteOfTheDay.addQotd(event.getChannel().getIdLong(), event.getGuild().getIdLong());
                                        if (isAdded) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Ok my master.")).complete();
                                        } else {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I am allready here, don't you see me? OPEN YOUR GOD DAMNED EYES.")).complete();
                                        }
                                    } catch (Exception e) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I fucking hate my life... AN ERROR AGAIN")).complete();
                                        e.printStackTrace();
                                    }
                                }
                                case "removeqotd" -> {
                                    try {
                                        boolean isRemoved = QuoteOfTheDay.removeQotd(event.getChannel().getIdLong(), event.getGuild().getIdLong());
                                        if (isRemoved) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Doby is free.")).complete();
                                        } else {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Error", "You cannot remove me, IF I AM NOT HERE. Fuck off.")).complete();
                                        }
                                    } catch (Exception e) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "Fuck, shit, ahhh, NOOOOOO. I made a mistake...")).complete();
                                        e.printStackTrace();
                                    }
                                }
                                case "addtruth" -> {
                                    if (args.length > 1) {
                                        String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
                                        try {
                                            boolean isAdded = TruthOrDare.addTruth(newElement, serverId);
                                            if (isAdded) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Added \"" + newElement + "\"")).complete();
                                            } else {
                                                event.getChannel().sendMessage(Main.getEmbedMessage("Error", "You fucking goblin.")).complete();
                                            }
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I am fucking retarded... You are as well.")).complete();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I don't think so...")).complete();
                                    }
                                }
                                case "adddare" -> {
                                    if (args.length > 1) {
                                        String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
                                        try {
                                            boolean isAdded = TruthOrDare.addDare(newElement, serverId);
                                            if (isAdded) {
                                                event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Added \"" + newElement + "\"")).complete();
                                            } else {
                                                event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I fucking hate you. It's already registered...")).complete();
                                            }
                                        } catch (IOException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "You are annoying... I made a mistake because of you.")).complete();
                                            e.printStackTrace();
                                        }
                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "How dare you?")).complete();
                                    }
                                }
                                case "addquestion" -> {
                                    if (args.length == 0) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "Hustensohn!")).complete();
                                    }
                                    if(args.length < 2) {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "Ich hasse dich!")).complete();
                                    }
                                    String gameName = args[0].toLowerCase();
                                    String newElement = String.join(" ", Arrays.asList(args).subList(2, args.length));

                                    String succesTitle          =   "Success";
                                    String succesDescription    =   "Added \"" + newElement + "\"";
                                    String ErrorTitle           =   "Error";
                                    String ErrorDescription     =   "I fucking hate you. It's already registered...";
                                    String InternalErrorTitle   =   "Internal error";
                                    String InternalErrorDescription="You are annoying... I made a mistake because of you.";

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
                                                boolean isAdded = WürdestDuEher.addMessage(newElement, serverId);
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
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Error", "Bist du behindert? Das gibt es nicht!")).complete();
                                        }
                                    }


                                }
                                case "play" -> {
                                    if (args.length > 1) {
                                        if (args[1].equalsIgnoreCase("tod") || args[1].equalsIgnoreCase("truthOrDare")) {
                                            TruthOrDare tod = new TruthOrDare(serverId);
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Fine fucker.. Here's your game.", "<#" + tod.getChannelId() + ">")).complete();
                                        } else if (args[1].equalsIgnoreCase("ihnn") || args[1].equalsIgnoreCase("ichHabNochNie")) {
                                            IchHabNochNie ihnn = new IchHabNochNie(serverId);
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Fine fucker.. Here's your game.", "<#" + ihnn.getChannelId() + ">")).complete();
                                        } else if (args[1].equalsIgnoreCase("wde") || args[1].equalsIgnoreCase("würdestDuEher")) {
                                            WürdestDuEher wde = new WürdestDuEher(serverId);
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Fine fucker.. Here's your game.", "<#" + wde.getChannelId() + ">")).complete();
                                        }
                                    } else {
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Error", "What u wanna play bitch?")).complete();
                                    }
                                }
                                case "throwdice" -> {
                                    if (args.length > 1) {
                                        try {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Dice", String.valueOf(Dice.throwDice(Integer.parseInt(args[1]))))).complete();
                                        } catch (NumberFormatException e) {
                                            event.getChannel().sendMessage(Main.getEmbedMessage("Error", "Emilia, was soll der Shit. Was zum Fick ist " + args[1])).complete();
                                        }
                                    } else
                                        event.getChannel().sendMessage(Main.getEmbedMessage("Dice", String.valueOf(Dice.throwDice(6)))).complete();
                                }
                                default -> event.getChannel().sendMessage(
                                        "```Channel-Cleaning:\n" +
                                                "~addchannel\n" +
                                                "~removechannel\n" +
                                                "\n" +
                                                "Quote of the day\n" +
                                                "~addqotd\n" +
                                                "~removeqotd\n" +
                                                "\n" +
                                                "Games\n" +
                                                "~play tod - TruthOrDare\n" +
                                                "~addtruth\n" +
                                                "~adddare\n" +
                                                "\n" +
                                                "~play ihnn - IchHabNochNie" +
                                                "```").complete();
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
