package de.akkjon.pr.mbrm;

import de.akkjon.pr.mbrm.audio.AudioManager;
import de.akkjon.pr.mbrm.audio.Playlist;
import de.akkjon.pr.mbrm.audio.Song;
import de.akkjon.pr.mbrm.games.*;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nullable;
import javax.naming.NameNotFoundException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            String version = Updater.getVersion();

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
                arrChangelog = new ArrayList<>(Arrays.asList(serverWatcher.getChangelogChannelsList()));
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
            Updater.sendChangelog(true);
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

        commands.add(new Command(new String[]{"songs", "songlist"}, false, (event, args, serverWatcher) -> {
            List<Song> songs = Song.getList();
            StringBuilder out = new StringBuilder("Songs:");
            for(Song song : songs) {
                out.append("\n").append(song.getName());
            }
            event.getChannel().sendMessage("````" + out.toString() + "```").complete();
        }));

        commands.add(new Command(new String[]{"musicplaylist", "musikplaylist"}, false, (event, args, serverWatcher) -> {
            if(event.getMember()==null) return;
            GuildVoiceState voiceState = event.getMember().getVoiceState();
            if(isBotVoicePermitted(voiceState, serverWatcher)) {
                String name = event.getMessage().getContentRaw().substring(args[0].length() + 1 + 1).trim();
                List<Playlist> playlists = Playlist.fromName(event.getGuild().getIdLong(), name);
                List<Playlist> playlistsEquals = playlists.stream().filter(playlist->playlist.getName().equalsIgnoreCase(name)).collect(Collectors.toList());

                if (playlists.size() == 1 || playlistsEquals.size()==1) {

                    Playlist playlist;
                    if(playlists.size() == 1) playlist = playlists.get(0);
                    else playlist = playlistsEquals.get(0);

                    serverWatcher.getAudioManager().addPlaylist(playlist, voiceState.getChannel(), event.getChannel());
                } else {
                    event.getChannel().sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.error"),
                            Locales.getString("msg.music.playlist.noResult", name))).complete();
                }
            }
        }));

        commands.add(new Command(new String[]{"music", "musik"}, false, (event, args, serverWatcher) -> {

            if(event.getMember()==null) return;
            GuildVoiceState voiceState = event.getMember().getVoiceState();
            if(isBotVoicePermitted(voiceState, serverWatcher)) {

                String name = event.getMessage().getContentRaw().substring(args[0].length() + 1 + 1).trim();
                List<Song> songs = Song.fromName(name);
                List<Song> songsEquals = songs.stream().filter(song->song.getName().equalsIgnoreCase(name)).collect(Collectors.toList());

                if (songs.size() == 1 || songsEquals.size()==1) {

                    Song song;
                    if(songs.size() == 1) song = songs.get(0);
                    else song = songsEquals.get(0);

                    serverWatcher.getAudioManager().addSong(song, voiceState.getChannel(), event.getChannel());

                } else {
                    event.getChannel().sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.error"),
                            Locales.getString("msg.music.song.noResult", name))).complete();
                }

            } else {
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.error"),
                        Locales.getString("msg.music.error.notInVoiceOrBotChannel"))).complete();
            }
        }));

        commands.add(new Command(new String[]{"queue"}, false, (event, args, serverWatcher) ->
            serverWatcher.getAudioManager().getPlaylist().sendToChannel(event.getChannel())
        ));

        commands.add(new Command(new String[]{"playlist"}, false, ((event, args, serverWatcher) -> {
            if(event.getMember()==null) return;
            GuildVoiceState voiceState = event.getMember().getVoiceState();
            if(isBotVoicePermitted(voiceState, serverWatcher)) {
                switch (args[1].toLowerCase()) {
                    case "list" -> serverWatcher.getAudioManager().getPlaylist().sendToChannel(event.getChannel());
                    case "setname" -> {
                        String name = String.join(" ", Arrays.asList(args).subList(2, args.length - 1).toArray(String[]::new));
                        serverWatcher.getAudioManager().getPlaylist().setName(name);
                        event.getChannel().sendMessage(AudioManager.getMusicEmbed(
                                Locales.getString("msg.music.playlist.rename", name))).complete();
                    }
                    case "save" -> {
                        try {
                            boolean saved = serverWatcher.getAudioManager().getPlaylist().save(event.getMember().getIdLong());
                            if(saved) {
                                event.getChannel().sendMessage(AudioManager.getMusicEmbed(
                                        Locales.getString("msg.music.playlist.save.success"))).complete();
                            } else  {
                                event.getChannel().sendMessage(Main.getEmbedMessage(
                                        Locales.getString("msg.commands.internalError"),
                                        Locales.getString("msg.music.playlist.save.error.internalError"))).complete();
                            }

                        } catch (NameNotFoundException e) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.playlist.save.error.noName"))).complete();

                        } catch (IllegalAccessException e) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.playlist.save.error.notPermitted"))).complete();
                        }
                    }
                    case "delete" -> {
                        try {
                            boolean removed = serverWatcher.getAudioManager().getPlaylist().remove(event.getMember().getIdLong());
                            if(removed) {
                                event.getChannel().sendMessage(AudioManager.getMusicEmbed(
                                        Locales.getString("msg.music.playlist.delete.success"))).complete();
                            } else  {
                                event.getChannel().sendMessage(Main.getEmbedMessage(
                                        Locales.getString("msg.commands.internalError"),
                                        Locales.getString("msg.music.playlist.delete.error.internalError"))).complete();
                            }
                        } catch (FileNotFoundException e) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.playlist.delete.error.notSaved"))).complete();
                        } catch (IllegalAccessException e) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.playlist.delete.error.notPermitted"))).complete();
                        }
                    }
                    case "clear" -> {
                        serverWatcher.getAudioManager().resetPlaylist();
                        event.getChannel().sendMessage(AudioManager.getMusicEmbed(
                                Locales.getString("msg.music.playlist.cleared"))).complete();
                    }
                    case "jump" -> {
                        if(args.length < 3) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.jump.noNumber"))).complete();
                        }
                        try {
                            int index = Integer.parseInt(args[2]);
                            serverWatcher.getAudioManager().jumpToSong(index);
                            Song song = serverWatcher.getAudioManager().getPlaylist().getSong(index);
                            event.getChannel().sendMessage(AudioManager.getMusicEmbed(
                                    Locales.getString("msg.music.jump.success", song.getName()))).complete();
                        } catch (IllegalArgumentException e) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.jump.invalidNumber", args[2]))).complete();
                        }
                    }
                    case "remove" -> {
                        int index = serverWatcher.getAudioManager().getPlaylist().getNowPlayingIndex();
                        if(args.length >= 3) {
                            try {
                                index = Integer.parseInt(args[2]);
                            } catch (NumberFormatException ignored) {}
                        }
                        if(index < 0 || index>=serverWatcher.getAudioManager().getPlaylist().getSize()) {
                            event.getChannel().sendMessage(Main.getEmbedMessage(
                                    Locales.getString("msg.commands.error"),
                                    Locales.getString("msg.music.playlist.remove.invalidNumber"))).complete();
                            return;
                        }
                        Song song = serverWatcher.getAudioManager().getPlaylist().getSong(index);
                        serverWatcher.getAudioManager().removeSong(index);
                        event.getChannel().sendMessage(AudioManager.getMusicEmbed(
                                Locales.getString("msg.music.playlist.remove.success", song.getName()))).complete();
                    }
                }
            }
        })));

        commands.add(new Command(new String[]{"loop"}, false, (event, args, serverWatcher) -> {
            if(event.getMember()==null) return;
            GuildVoiceState voiceState = event.getMember().getVoiceState();
            if(isBotVoicePermitted(voiceState, serverWatcher)) {
                if(serverWatcher.getAudioManager().isConnected()) {
                    serverWatcher.getAudioManager().getPlaylist().toggleLooping();
                    String key = serverWatcher.getAudioManager().getPlaylist().isLooping() ? "msg.primitive.true.on" : "msg.primitive.false.off";
                    AudioManager.getMusicEmbed(Locales.getString("msg.music.looping", Locales.getString(key)));
                } else {
                    event.getChannel().sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.error"),
                            Locales.getString("msg.music.error.notConnected"))).complete();
                }
            } else {
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.error"),
                        Locales.getString("msg.music.error.notInVoiceOrBotChannel"))).complete();
            }
        }));

        commands.add(new Command(new String[]{"shuffle"}, false, (event, args, serverWatcher) -> {
            if(event.getMember()==null) return;
            GuildVoiceState voiceState = event.getMember().getVoiceState();
            if(isBotVoicePermitted(voiceState, serverWatcher)) {
                if(serverWatcher.getAudioManager().isConnected()) {
                    serverWatcher.getAudioManager().getPlaylist().toggleShuffling();
                    String key = serverWatcher.getAudioManager().getPlaylist().isShuffling() ? "msg.primitive.true.on" : "msg.primitive.true.off";
                    AudioManager.getMusicEmbed(Locales.getString("msg.music.shuffling", Locales.getString(key)));
                } else {
                    event.getChannel().sendMessage(Main.getEmbedMessage(
                            Locales.getString("msg.commands.error"),
                            Locales.getString("msg.music.error.notConnected"))).complete();
                }
            } else {
                event.getChannel().sendMessage(Main.getEmbedMessage(
                        Locales.getString("msg.commands.error"),
                        Locales.getString("msg.music.error.notInVoiceOrBotChannel"))).complete();
            }
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
            if(event.getMember()==null) return;
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
        if(event.getMember()==null) return false;
        return event.getMember().getRoles().contains(event.getGuild().getRolesByName("admin", true).get(0));
    }

    public static boolean isBotVoicePermitted(@Nullable GuildVoiceState voiceState, ServerWatcher serverWatcher) {
        if(voiceState==null || voiceState.getChannel()==null) return false;
        if(voiceState.inVoiceChannel()) {
            return (!serverWatcher.getAudioManager().isConnected()
                    || voiceState.getChannel().equals(serverWatcher.getAudioManager().getChannel()));
        }
        return false;
    }
}
