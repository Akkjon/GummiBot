package de.akkjon.pr.mbrm.games;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.akkjon.pr.mbrm.Storage;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Game extends ListenerAdapter {

    static final Gson gson = new Gson();
    final long guildId;
    TextChannel channel;


    Game(long serverID) {
        this.guildId = serverID;

    }

    static String getFromLists(JsonArray global, JsonArray server) {
        int max = global.size() + server.size();
        int value = (int) (Math.random() * max);
        if (value < global.size()) {
            return global.get(value).getAsString();
        } else {
            value = value % global.size();
            return server.get(value).getAsString();
        }
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

    static JsonObject getServerInternal(long serverId, String fileName) throws IOException {
        String fileContent = Storage.getFileContent(Storage.rootFolder + serverId + File.separator + fileName, "{}");
        JsonObject element = gson.fromJson(fileContent, JsonObject.class);
        return element;
    }

    static JsonArray getServer(String mode, long serverId, String fileName) throws IOException {
        JsonObject element = getServerInternal(serverId, fileName);
        if (element.has(mode)) {
            return element.get(mode).getAsJsonArray();
        }
        return new JsonArray(0);
    }

    static JsonArray getGlobal(String mode, String fileName) {
        String filecontent = Storage.getInternalFile(fileName);
        Gson gson = new Gson();
        JsonObject element = gson.fromJson(filecontent, JsonObject.class);
        JsonArray array = element.get(mode).getAsJsonArray();
        return array;
    }

    public long getChannelId() {
        return channel.getIdLong();
    }
}
