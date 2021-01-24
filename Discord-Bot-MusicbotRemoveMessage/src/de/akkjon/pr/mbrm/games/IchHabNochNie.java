package de.akkjon.pr.mbrm.games;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.akkjon.pr.mbrm.Main;
import de.akkjon.pr.mbrm.Storage;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IchHabNochNie extends ListenerAdapter {

    private static final Gson gson = new Gson();
    private final long guildId;
    private final TextChannel channel;
    private boolean isStarted = false;


    public IchHabNochNie(long serverID) {
        this.guildId = serverID;
        this.channel = Main.jda.getCategoryById(802719239723024414L).createTextChannel("Ich-hab-noch-nie").complete();
        Message message = channel.sendMessage(Main.getEmbedMessage("Ich hab noch nie",
                "Who wants to play a game?\n" +
                        "Click ➡ to start the game.\n" +
                        "Click ❌ to end the current game.")).complete();
        message.addReaction("➡").queue();
        message.addReaction("❌").queue();
        message.pin().complete();
        initReactionListeners();
    }

    public static String getMessage(long serverId) throws IOException {
		JsonArray global = getGlobal("message");
		JsonArray server = getServer("message", serverId);
		return getFromLists(global, server);
    }

    public static boolean addMessage(String element, long serverId) throws IOException {
        return add(element, "message", serverId);
    }


    public static boolean add(String element, String mode, long serverId) throws IOException {
        String path = Storage.rootFolder + serverId + File.separator + "ihnn.txt";
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
        String filecontent = Storage.getInternalFile("/ihnn.json");
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
        String fileContent = Storage.getFileContent(Storage.rootFolder + serverId + File.separator + "ihnn.txt", "{}");
        JsonObject element = gson.fromJson(fileContent, JsonObject.class);
        return element;
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

                    if (title.equals("Ich hab noch nie")) {
                        if (event.getReactionEmote().getName().equals("➡")) {
                            sendMessage();
                        } else if (event.getReactionEmote().getName().equals("❌")) {
                            channel.delete().complete();
                        }
                    }
                }
            }
        });
    }

    private void startGame() {
        if (isStarted) return;
        isStarted = true;
        sendMessage();
    }

    void sendMessage() {
        try {
            Message msg = channel.sendMessage(Main.getEmbedMessage("Ich hab noch nie", "..." + getMessage(guildId))).complete();
            msg.addReaction("➡").queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public long getChannelId() {
        return channel.getIdLong();
    }
}
