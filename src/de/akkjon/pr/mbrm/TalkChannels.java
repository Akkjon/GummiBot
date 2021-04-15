package de.akkjon.pr.mbrm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TalkChannels {
    static final Gson gson = new Gson();
    final long guildId;
    final Category category;
    final List<String> names = getNames();


    public TalkChannels(long guildId) {
        this.guildId = guildId;
        this.category = getOrCreateCategory();
        initListeners();
    }

    private void initListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
                if (getEmptyChannels().size() == 0) {
                    createNewChannel();
                }
            }

            @Override
            public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
                if (getEmptyChannels().size() == 0)
                    createNewChannel();
                else removeUnnecessaryChannels();

            }

            @Override
            public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
                removeUnnecessaryChannels();
            }
        });
    }

    private void removeUnnecessaryChannels() {
        List<VoiceChannel> channels = getEmptyChannels();
        if (channels.size() >= 2) {
            VoiceChannel firstEmpty = null;
            for (VoiceChannel c : channels) {
                if (c.getMembers().isEmpty()) {
                    if (firstEmpty == null) {
                        firstEmpty = c;
                    } else {
                        c.delete().queue();
                    }
                }
            }
        }
    }

    static List<String> getNames() {
        JsonArray array = gson.fromJson(Storage.getInternalFile("channelNames.json"), JsonObject.class).get("names").getAsJsonArray();

        return gson.fromJson(array, new TypeToken<List<String>>() {
        }.getType());
    }

    void createNewChannel() {
        category.createVoiceChannel(names.get((int) (Math.random() * names.size()))).queue();
    }

    private List<VoiceChannel> getEmptyChannels() {
        List<VoiceChannel> channels = new ArrayList<>();

        for (VoiceChannel c : category.getVoiceChannels()) {
            if (c.getMembers().isEmpty()) {
                boolean perm = false;
                for (PermissionOverride p : c.getPermissionOverrides()) {
                    if (p.isRoleOverride()) {
                        if (Objects.requireNonNull(p.getRole()).getName().equals("Perm")) {
                            perm = true;
                        }
                    }
                }
                if (!perm) channels.add(c);
            }
        }
        return channels;
    }

    public Category getOrCreateCategory() {
        Category cat = null;
        try {
            String id = Storage.getFileContent(
                    Storage.rootFolder + guildId + File.separator + "talkCategory.txt", null);
            if (id != null && !id.isBlank()) cat = Main.jda.getCategoryById(id);
        } catch (IOException ignored) {
        }
        if (cat == null) {
            cat = Objects.requireNonNull(Main.jda.getGuildById(guildId)).createCategory("Talks").complete();
            cat.createVoiceChannel(names.get((int) (Math.random() * names.size()))).queue();
            try {
                Storage.saveFile(Storage.rootFolder + guildId + File.separator + "talkCategory.txt", cat.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return cat;
    }

}
