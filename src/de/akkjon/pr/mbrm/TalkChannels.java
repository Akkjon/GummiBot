package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class TalkChannels {
    final long guildId;
    final Category category;

    public TalkChannels(long guildId) {
        this.guildId = guildId;
        this.category = getOrCreateCategory();
        initListeners();
    }

    private void initListeners() {
        Main.jda.addEventListener(new ListenerAdapter() {
            @Override
            public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
                System.out.println("join");
                if (amountOfEmptyChannels() == 0) {
                    System.out.println("new Channel");
                    createNewChannel("Test1");
                }
            }

            @Override
            public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
                System.out.println("move");
                if (amountOfEmptyChannels() == 0) category.createVoiceChannel("Test1").queue();
                else removeUnnecessaryChannels();

            }

            @Override
            public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
                System.out.println("leave");
                removeUnnecessaryChannels();
            }
        });
    }

    private void removeUnnecessaryChannels() {
        if (amountOfEmptyChannels() >= 2) {
            VoiceChannel firstEmpty = null;
            for (VoiceChannel c : category.getVoiceChannels()) {
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

    void createNewChannel(String name) {
        category.createVoiceChannel(name).queue();
    }

    private int amountOfEmptyChannels() {
        int result = 0;
        for (VoiceChannel c : category.getVoiceChannels()) {
            if (c.getMembers().isEmpty()) {
                result++;
            }
        }
        return result;
    }

    public Category getOrCreateCategory() {
        Category cat = null;
        try {
            String id = Storage.getFileContent(
                    Storage.rootFolder + guildId + File.separator + "talkCategory.txt", null);
            if (id != null && !id.isBlank()) cat = Main.jda.getCategoryById(id);
        } catch (IOException ignored) {
            cat = Objects.requireNonNull(Main.jda.getGuildById(guildId)).createCategory("Talks").complete();
            cat.createVoiceChannel("Talk 1").queue();
        }
        if (cat == null) {
            cat = Objects.requireNonNull(Main.jda.getGuildById(guildId)).createCategory("Talks").complete();
            cat.createVoiceChannel("Talk 1").queue();
            try {
                Storage.saveFile(Storage.rootFolder + guildId + File.separator + "talkCategory.txt", cat.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return cat;
    }

}
