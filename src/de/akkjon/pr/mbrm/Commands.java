package de.akkjon.pr.mbrm;

import de.akkjon.pr.mbrm.games.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {

    public static List<Command> commands = new ArrayList<>();
    public static CommandRunnable helpCommand;
    public static final String COMMAND_PREFIX = "~";

    static {
        commands.add(new Command(new String[]{"addchannel"}, true, (event, args, serverWatcher) -> {
            long channelId = event.getChannel().getIdLong();
            if (!serverWatcher.isChannelRegistered(channelId)) {
                try {
                    serverWatcher.channels = Storage.addChannel(serverWatcher.getGuildId(), channelId);
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
        }));

        commands.add(new Command(new String[]{"removechannel"}, true, (event, args, serverWatcher) -> {
            long channelId = event.getChannel().getIdLong();
            if (serverWatcher.isChannelRegistered(channelId)) {
                try {
                    serverWatcher.channels = Storage.removeChannel(serverWatcher.getGuildId(), channelId);
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
        }));

        commands.add(new Command(new String[]{"addqotd", "addmotd"}, true, (event, args, serverWatcher) -> {
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
        }));

        commands.add(new Command(new String[]{"removeqotd"}, true, (event, args, serverWatcher) -> {
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
        }));

        commands.add(new Command(new String[]{"addtruth"}, true, (event, args, serverWatcher) -> {
            if (args.length > 1) {
                String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
                try {
                    boolean isAdded = TruthOrDare.addTruth(newElement, serverWatcher.getGuildId());
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
        }));

        commands.add(new Command(new String[]{"adddare"}, true, (event, args, serverWatcher) -> {
            if (args.length > 1) {
                String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
                try {
                    boolean isAdded = TruthOrDare.addDare(newElement, serverWatcher.getGuildId());
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
        }));

        commands.add(new Command(new String[]{"addquestion"}, true, (event, args, serverWatcher) -> {
            if (args.length == 1) {
                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                        Locales.getString("msg.onAddQuestionError1"))).complete();
                return;
            }
            if (args.length < 3) {
                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                        Locales.getString("msg.onAddQuestionError2"))).complete();
                return;
            }
            String gameName = args[1].toLowerCase();
            String newElement = String.join(" ", Arrays.asList(args).subList(2, args.length));

            String succesTitle = Locales.getString("msg.commands.success");
            String succesDescription = Locales.getString("msg.commands.addGame.added", newElement);
            String ErrorTitle = Locales.getString("msg.commands.error");
            String ErrorDescription = Locales.getString("msg.commands.addGame.alreadyExists");
            String InternalErrorTitle = Locales.getString("msg.commands.internalError");
            String InternalErrorDescription = Locales.getString("msg.commands.addGame.internalError");

            long serverId = serverWatcher.getGuildId();
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
                default -> event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                        Locales.getString("msg.commands.addGame.gameNotExists"))).complete();
            }
        }));

        commands.add(new Command(new String[]{"play"}, false, (event, args, serverWatcher) -> {
            if (args.length > 1) {
                args[1] = args[1].toLowerCase();
                long serverId = serverWatcher.getGuildId();
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
                    default -> event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                            Locales.getString("msg.commands.addGame.gameNotExists"))).complete();
                }
            } else {
                event.getChannel().sendMessage(Main.getEmbedMessage(Locales.getString("msg.commands.error"),
                        Locales.getString("msg.commands.games.noGame"))).complete();
            }
        }));

        commands.add(new Command(new String[]{"throwdice"}, false, (event, args, serverWatcher) -> {
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
        }));

        commands.add(new Command(new String[]{"insult"}, false, (event, args, serverWatcher) -> {
            long id;
            if (args.length == 1) {
                id = event.getAuthor().getIdLong();
            } else {
                id = event.getMessage().getMentionedMembers().get(0).getIdLong();
            }
            event.getChannel().sendMessage(Main.getEmbedMessage("A", MessageFormat.format(serverWatcher.getInsult().getMessage(), "<@" + id + ">"))).complete();
        }));

        commands.add(new Command(new String[]{"info"}, true, (event, args, serverWatcher) -> {
            long uptime = System.currentTimeMillis() - Main.STARTUP_TIME;
            double version = Updater.getVersion();

            String strUptime = "";
            uptime /= 1000;
            long sec, min, hour, day;
            if ((sec = uptime % 60) > 0) {
                strUptime = sec + "sek" + strUptime;
            }
            uptime /= 60;
            if ((min = uptime % 60) > 0) {
                strUptime = min + "min " + strUptime;
            }
            uptime /= 60;
            if ((hour = uptime % 60) > 0) {
                strUptime = hour + "h " + strUptime;
            }
            uptime /= 24;
            if ((day = uptime) > 0) {
                strUptime = day + "d " + strUptime;
            }

            String strMsg = "```" +
                    "-- System-Info --\n" +
                    "Software-Version: " + version + "\n" +
                    "Uptime:           " + strUptime + "\n" +
                    "```";

            event.getChannel().sendMessage(strMsg).complete();
        }));

        commands.add(new Command(new String[]{"setlog"}, true, (event, args, serverWatcher) -> {
            try {
                Storage.saveFile(Storage.rootFolder + serverWatcher.getGuildId() + File.separator + "logChannel.txt", event.getChannel().getId());

                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.success"),
                        Locales.getString("msg.commands.log.added")
                )).complete();
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.internalError"),
                        Locales.getString("error.internalError6")
                )).complete();
            }
        }));

        commands.add(new Command(new String[]{"removelog"}, true, (event, args, serverWatcher) -> {
            File file = new File(Storage.rootFolder + serverWatcher.getGuildId() + File.separator + "logChannel.txt");
            if (file.exists()) {
                try {
                    file.delete();
                    event.getChannel().sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.success"),
                            Locales.getString("msg.commands.log.removed")
                    )).complete();
                } catch (Exception e) {
                    event.getChannel().sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.success"),
                            Locales.getString("error.internalError7")
                    )).complete();
                }

            } else {
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.success"),
                        Locales.getString("msg.commands.log.notSet")
                )).complete();
            }
        }));

        commands.add(new Command(new String[]{"addchangelog"}, true, (event, args, serverWatcher) -> {
            List<Long> arrChangelog;
            try {
                arrChangelog = Arrays.asList(serverWatcher.getChangelogChannelsList());
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.internalError"),
                        Locales.getString("msg.commands.changelog.error.channelsGetException")
                )).complete();
                return;
            }
            Long channelId = event.getChannel().getIdLong();
            if (arrChangelog.contains(channelId)) {
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.error"),
                        Locales.getString("msg.commands.changelog.error.alreadyExists")
                )).complete();
                return;
            }
            arrChangelog.add(channelId);

            try {
                serverWatcher.saveChangelogChannelsList(arrChangelog.toArray(new Long[0]));
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.internalError"),
                        Locales.getString("msg.commands.changelog.error.cannotSave")
                )).complete();
                return;
            }
            event.getChannel().sendMessage(Main.getEmbedMessage(
                    Locales.getString("msg.commands.success"),
                    Locales.getString("msg.commands.changelog.added")
            )).complete();
        }));

        commands.add(new Command(new String[]{"removechangelog"}, true, (event, args, serverWatcher) -> {
            List<Long> arrChangelog;
            try {
                arrChangelog = Arrays.asList(serverWatcher.getChangelogChannelsList());
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.internalError"),
                        Locales.getString("msg.commands.changelog.error.channelsGetException")
                )).complete();
                return;
            }
            Long channelId = event.getChannel().getIdLong();
            if (!arrChangelog.contains(channelId)) {
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.error"),
                        Locales.getString("msg.commands.changelog.error.notExists")
                )).complete();
                return;
            }
            arrChangelog.remove(channelId);

            try {
                serverWatcher.saveChangelogChannelsList(arrChangelog.toArray(new Long[0]));
            } catch (IOException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.internalError"),
                        Locales.getString("msg.commands.changelog.error.cannotSave")
                )).complete();
                return;
            }
            event.getChannel().sendMessage(Main.getEmbedMessage(
                    Locales.getString("msg.commands.success"),
                    Locales.getString("msg.commands.changelog.removed")
            )).complete();
        }));

        helpCommand = (event, args, serverWatcher) -> {
            String helpMsg = Storage.getInternalFile("help.txt");
            event.getChannel().sendMessage(helpMsg).complete();
        };
    }

    public static void runCommands(MessageReceivedEvent event, ServerWatcher watcher) {
        String content = event.getMessage().getContentRaw();

        if (content.startsWith(COMMAND_PREFIX)) {
            content = content.substring(COMMAND_PREFIX.length());
            String[] args = Arrays.stream(content.split(" ")).filter(e -> e.length() > 0).toArray(String[]::new);

            if (args.length == 0) {
                return;
            }

            args[0] = args[0].toLowerCase();
            boolean isAdmin = event.getMember().getRoles().contains(event.getGuild().getRolesByName("admin", true).get(0));

            for (Command command : commands) {
                if (command.getLabels().contains(args[0])) {
                    if (command.isAdminRequired() && !isAdmin) break;
                    command.run(event, args, watcher);
                    return;
                }
            }
            helpCommand.run(event, args, watcher);
        }
    }

    public static boolean isAdmin(MessageReceivedEvent event) {
        return event.getMember().getRoles().contains(event.getGuild().getRolesByName("admin", true).get(0));
    }
}
