package de.akkjon.pr.mbrm;

import com.google.gson.Gson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends ListenerAdapter {

    private static String TOKEN;

    static {
        try {
            TOKEN = Storage.getFileContent(Storage.jarFolder + File.separator + "token.txt", "");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String VERSION_PRIOR = "";

    public static String getVersionPrior() {
        return Main.VERSION_PRIOR;
    }

    public static final long STARTUP_TIME = System.currentTimeMillis();
    public static JDA jda;
    public static boolean isEnabled = true;
    public static final Gson gson = new Gson();

    public static void main(String[] args) {
        Logger.init();
        try {
            Handler.initWebServer();
        } catch (IOException e) {
            System.err.println(Locales.getString("error.noPortAvailable"));
            e.printStackTrace();
            System.exit(0);
        }

        HashMap<String, String> argsMap = new HashMap<>();
        for (String str : args) {
            String[] arr = str.split("=", 2);
            if (!(arr[0] == null || arr[0].isBlank())) {
                if (!(arr[1] == null || arr[1].isBlank())) {
                    argsMap.put(arr[0], arr[1]);
                } else argsMap.put(arr[0], "");
            }
        }
        if (argsMap.getOrDefault("doUpdates", "true").equals("true")) {
            new Updater();
        }

        try {
            jda = JDABuilder.createDefault(TOKEN).build();
            jda.setAutoReconnect(true);
            jda.addEventListener(new ListenerAdapter() {
                @Override
                public void onShutdown(@NotNull ShutdownEvent event) {
                    try {
                        Updater.shutdownInternals();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            new Main();
            setIcon();

            new StatusChanger();

        } catch (LoginException e) {
            System.err.println(Locales.getString("error.loginException"));
            e.printStackTrace();

            try {
                Updater.shutdownInternals();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.exit(0);
            return;
        }

        //set prior version with key-value pair
        String versionPrior = argsMap.getOrDefault("versionPrior", null);

        //set prior version without key
        if (versionPrior == null) {
            for (String arg : args) {
                if (arg.matches("^([0-9]|\\.)*$")) {
                    versionPrior = arg;
                    break;
                }
            }
        }

        //send changelog if prior version was given in some way and is not the same as new version
        if (versionPrior != null) {
            Main.VERSION_PRIOR = versionPrior;
            if (!versionPrior.equals(Updater.getVersion())) {
                ServerWatcher.sendChangelog();
            }
        }

        long STARTUP_DURATION = System.currentTimeMillis() - STARTUP_TIME;
        System.out.println("Startup finished in " + STARTUP_DURATION + " ms");
    }

    public Main() {
        for (long server : Storage.getServers()) {
            try {
                new ServerWatcher(server);
                new TalkChannels(server);
            } catch (AlreadyBoundException e) {
                System.err.println(Locales.getString("error.startWatcher", server));
                e.printStackTrace();
            }
        }
        jda.addEventListener(this);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        long serverId = event.getGuild().getIdLong();
        try {
            new ServerWatcher(serverId);
        } catch (AlreadyBoundException e) {
            System.err.println(Locales.getString("error.startWatcher", serverId));
            e.printStackTrace();
        }
    }

    public static final int embedColor = new Color(0, 255, 0).getRGB();

    public static MessageEmbed getEmbedMessage(String title, String message) {
        return new MessageEmbed(
                null,
                title,
                message,
                EmbedType.UNKNOWN,
                null,
                embedColor,
                null,
                null,
                null,
                null,
                new MessageEmbed.Footer(Locales.getString("bot.name"), null, null),
                null,
                new ArrayList<>()
        );
    }

    private static void setIcon() {

        if (SystemTray.isSupported()) {
            Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/GummiIcon.png"));

            try {
                TrayIcon icon = new TrayIcon(image, Locales.getString("bot.name") + "-Dashboard");
                icon.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().browse(new URI("http://localhost:8088"));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                });
                icon.setImageAutoSize(true);
                SystemTray.getSystemTray().add(icon);
            } catch (AWTException e) {
                e.printStackTrace();
            }

        }
    }
}
