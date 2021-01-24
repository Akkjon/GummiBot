package de.akkjon.pr.mbrm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TruthOrDare extends ListenerAdapter {

    private static final Gson gson = new Gson();
    final ArrayList<Long> players = new ArrayList<>();
    private long lastPlayer;
    private final long guildId;
    private final TextChannel channel;
    private boolean isStarted = false;
    private boolean isChoosing;


    public TruthOrDare(long serverID) {
        this.guildId = serverID;
        this.channel = Main.jda.getCategoryById(802719239723024414L).createTextChannel("Truth-or-Dare").complete();
        Message message = channel.sendMessage(Main.getEmbedMessage("Truth or Dare",
                "Who wants to play a game?\n" +
                        "React with 👍 to enter the game.\n" +
                        "Click ➡ to start the game.\n" +
                        "Click ❌ to end the current game.")).complete();
        message.addReaction("👍").queue();
        message.addReaction("➡").queue();
        message.addReaction("❌").queue();
        message.pin().complete();
        initReactionListeners();
    }

    public static String getTruth(long serverId) throws IOException {
//		JsonArray global = getGlobal("truth");
//		JsonArray server = getServer("truth", serverId);
//		return getFromLists(global, server);
        return "aaa";
    }

    public static String getDare(long serverId) throws IOException {
//		JsonArray global = getGlobal("dare");
//		JsonArray server = getServer("dare", serverId);
//		return getFromLists(global, server);
        return "bbb";
    }

    public static boolean addTruth(String element, long serverId) throws IOException {
        return add(element, "truth", serverId);
    }

    public static boolean addDare(String element, long serverId) throws IOException {
        return add(element, "dare", serverId);
    }

    public static boolean add(String element, String mode, long serverId) throws IOException {
        String path = Storage.rootFolder + serverId + File.separator + "tod.txt";
        JsonObject object = gson.fromJson(Storage.getFileContent(path, "{}"), JsonObject.class);
        if (!object.has(mode)) {
            object.add(mode, new JsonArray());
        }
        JsonArray array = object.get(mode).getAsJsonArray();
        if (!array.contains(new JsonPrimitive(element))) {
            array.add(element);
            String content = gson.toJson(object);

            File file = new File(path);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            return true;
        }
        return false;
    }

    private static String getFromLists(JsonArray global, JsonArray server) {
        int max = global.size() + server.size();
        int value = (int) (Math.random() * max);
        if (value < global.size()) {
            return global.get(value).getAsString();
        } else {
            value = value % global.size();
            return server.get(value).getAsString();
        }
    }

    private static JsonArray getGlobal(String mode) {
        String filecontent = Storage.getInternalFile("/de/akkjon/pr/mbrm/resource/tod.json");
        Gson gson = new Gson();
        JsonObject element = gson.fromJson(filecontent, JsonObject.class);
        JsonArray array = element.get(mode).getAsJsonArray();
        return array;
    }

    private static JsonArray getServer(String mode, long serverId) throws IOException {
        JsonObject element = getServerInternal(serverId);
        if (element.has(mode)) {
            return element.get(mode).getAsJsonArray();
        }
        return new JsonArray(0);
    }

    private static JsonObject getServerInternal(long serverId) throws IOException {
        String fileContent = Storage.getFileContent(Storage.rootFolder + serverId + File.separator + "tod.txt", "{}");
        JsonObject element = gson.fromJson(fileContent, JsonObject.class);
        return element;
    }

    void addPlayer(Long id) {
        if (!players.contains(id)) players.add(id);
    }

    void removePlayer(long id) {
        if (players.size() <= 1) {
            channel.delete().complete();
            return;
        }
        if (this.lastPlayer == id) sendNextPlayerMessage();
        players.remove(id);
    }

    long getNextPlayer() {
        int indexOflastPlayer = players.indexOf(lastPlayer);
        if (++indexOflastPlayer >= players.size()) indexOflastPlayer = 0;
        this.lastPlayer = players.get(indexOflastPlayer);
        return this.lastPlayer;
    }

    long getCurrentPlayer() {
        return lastPlayer;
    }

    public void initReactionListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
                Message message = MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId());

                //skip if message was not from bot or reaction was from bot
                User bot = event.getJDA().getSelfUser();
                if (!message.getAuthor().equals(bot)) {
                    return;
                }
                if (event.getMember().getUser().equals(bot)) {
                    return;
                }

                if (message.getEmbeds().size() != 0) {
                    String title = message.getEmbeds().get(0).getTitle();

                    if (title.equals("Truth") || title.equals("Dare")) {
                        if (event.getReactionEmote().getName().equals("➡")) {
                            if (isChoosing) return;
                            sendNextPlayerMessage();
                        }
                    } else if (title.equals("Truth or Dare")) {
                        if (event.getReactionEmote().getName().equals("👍")) {
                            addPlayer(event.getMember().getIdLong());
                        } else if (event.getReactionEmote().getName().equals("➡")) {
                            startGame();
                        } else if (event.getReactionEmote().getName().equals("❌")) {
                            channel.delete().complete();
                        }
                    } else if (title.startsWith("Let the games begin...") || title.equals("Next player:")) {
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
            }

            @Override
            public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
                Message message = MessageHistory.getHistoryAround(channel, event.getMessageId()).complete().getMessageById(event.getMessageId());

                //skip if message was not from bot
                if (!message.getAuthor().equals(event.getJDA().getSelfUser())) {
                    return;
                }

                if (message.getEmbeds().size() != 0) {
                    String title = message.getEmbeds().get(0).getTitle();
                    if (title.equals("Truth or Dare")) {
                        if (event.getReactionEmote().getName().equals("👍")) {
                            removePlayer(event.getMember().getIdLong());
                        }
                    }
                }
            }
        });
    }

    private void startGame() {
        if(players.size() <= 1) {
            channel.sendMessage(Main.getEmbedMessage("Nope!", "Get yourself some friends nigga.")).complete();
            return;
        }
        if (isStarted) return;
        isStarted = true;
        isChoosing = true;
        Message msg = channel.sendMessage(Main.getEmbedMessage("Let the games begin... " + players.size() + " players", "<@" + getNextPlayer() + ">... Truth or Dare?")).complete();
        msg.addReaction("1️⃣").queue();
        msg.addReaction("2️⃣").queue();
    }

    private void sendNextPlayerMessage() {
        Message msg = channel.sendMessage(Main.getEmbedMessage("Next player:", "<@" + getNextPlayer() + ">... Truth or Dare?")).complete();
        isChoosing = true;
        msg.addReaction("1️⃣").queue();
        msg.addReaction("2️⃣").queue();
    }

    void sendTruth(MessageChannel channel) {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage("Truth", TruthOrDare.getTruth(guildId))).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendDare(MessageChannel channel) {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage("Dare", TruthOrDare.getDare(guildId))).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    long getChannelId() {
        return channel.getIdLong();
    }
}
