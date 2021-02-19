package de.akkjon.pr.mbrm.games;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import de.akkjon.pr.mbrm.Locales;
import de.akkjon.pr.mbrm.Main;
import de.akkjon.pr.mbrm.Storage;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class Game extends ListenerAdapter {

    static final Gson gson = new Gson();
    final long guildId;
    TextChannel channel;


    protected Game(long serverId) {
        this.guildId = serverId;

    }

    protected static String getFromLists(List<String> list) {
        int value = (int) (Math.random() * list.size());

        String returnVal = list.get(value);
        list.remove(value);
        return returnVal;
    }

    public static boolean add(String element, String mode, long serverId, String fileName) throws IOException {
        String path = Storage.rootFolder + serverId + File.separator + fileName;
        JsonObject object = gson.fromJson(Storage.getFileContent(path, "{}"), JsonObject.class);
        if (!object.has(mode)) {
            object.add(mode, new JsonArray());
        }
        JsonArray array = object.get(mode).getAsJsonArray();
        if (!array.contains(new JsonPrimitive(element))) {
            array.add(element);
            String content = gson.toJson(object);

            Storage.saveFile(path, content);

            return true;
        }
        return false;
    }

    static JsonObject getServerInternal(long serverId, String fileName) throws IOException {
        String fileContent = Storage.getFileContent(Storage.rootFolder + serverId + File.separator + fileName, "{}");
        return gson.fromJson(fileContent, JsonObject.class);
    }

    static JsonArray getServer(String mode, long serverId, String fileName) throws IOException {
        JsonObject element = getServerInternal(serverId, fileName);
        if (element.has(mode)) {
            return element.get(mode).getAsJsonArray();
        }
        return new JsonArray(0);
    }

    static JsonArray getGlobal(String mode, String fileName) {
        String fileContent = Storage.getInternalFile(fileName);
        Gson gson = new Gson();
        JsonObject element = gson.fromJson(fileContent, JsonObject.class);
        return element.get(mode).getAsJsonArray();
    }

    public long getChannelId() {
        return channel.getIdLong();
    }

    public List<String> loadRemaining(String mode, String fileName) throws IOException {
        JsonArray global = getGlobal(mode, fileName + ".json");
        JsonArray server = getServer(mode, guildId, fileName + ".txt");

        Type collectionType = new TypeToken<List<String>>() {
        }.getType();
        List<String> strGlobal = gson.fromJson(global, collectionType);
        List<String> strServer = gson.fromJson(server, collectionType);

        strGlobal.addAll(strServer);

        return strGlobal;
    }

    static boolean shouldReactToMessage(GenericGuildMessageReactionEvent event, Message message) {
        //skip if message was not from bot or reaction was from bot
        User bot = event.getJDA().getSelfUser();
        if (!message.getAuthor().equals(bot)) {
            return false;
        }
        return !event.getMember().getUser().equals(bot);
    }

    void sendStartMessage(String gameName) {
        String[] emotes = new String[]{"👍", "➡", "❌"};
        String text = Locales.getString("msg.games." + gameName + ".start", emotes[0], emotes[1], emotes[2]);
        Message message = channel.sendMessage(Main.getEmbedMessage(
                Locales.getString("msg.games." + gameName + ".title"),
                text)).complete();
        for (String emote : emotes) {
            if (text.contains(emote)) {
                message.addReaction(emote).queue();
            }
        }
        message.pin().complete();
    }

    void createTextChannel(String name) {
        try {
            Category cat = null;
            String id = Storage.getFileContent(
                    Storage.rootFolder + guildId + File.separator + "gameCategory.txt", null);
            if (id != null && !id.isBlank()) cat = Main.jda.getCategoryById(id);

            if (cat == null)
                cat = Objects.requireNonNull(Main.jda.getGuildById(guildId)).createCategory("Tolle Spielchen").complete();
            Storage.saveFile(Storage.rootFolder + guildId + File.separator + "gameCategory.txt", cat.getId());
            this.channel = cat.createTextChannel(name).complete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable Message checkMessage(GenericGuildMessageReactionEvent event) {
        Message message = MessageHistory.getHistoryAround(channel,
                event.getMessageId()).complete().getMessageById(event.getMessageId());

        //skip if message was not from bot or reaction was from bot
        if (!shouldReactToMessage(event, message)) return null;

        //skip if message has no embeds
        if (message.getEmbeds().size() == 0) return null;

        return message;
    }
}
