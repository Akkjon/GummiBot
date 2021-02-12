package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
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

    private static double VERSION_PRIOR = -1;

    public static double getVersionPrior() {
        return Main.VERSION_PRIOR;
    }

    public static final long STARTUP_TIME = System.currentTimeMillis();
    public static JDA jda;
    public static boolean isEnabled = true;

    public static void main(String[] args) {
        Logger.init();
        try {
            Handler.initWebServer();
        } catch (IOException e) {
            System.err.println(Locales.getString("error.noPortAvailable"));
            e.printStackTrace();
            System.exit(0);
        }

        new Updater();
        if (args.length > 0) {
            try {
                double versionPrior = Double.parseDouble(args[0]);
                Main.VERSION_PRIOR = versionPrior;

                if (versionPrior < Updater.getVersion()) {
                    Updater.sendChangelog();
                }
            } catch (Exception err) {
            }
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
            setStatus();
        } catch (LoginException e) {
            System.err.println(Locales.getString("error.loginException"));
            e.printStackTrace();

            try {
                Updater.shutdownInternals();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.exit(0);
        }
    }

    public Main() {
        for (long server : Storage.getServers()) {
            try {
                new ServerWatcher(server);
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

    public static void setStatus() {
        Main.jda.getPresence().setActivity(Activity.watching("Pornhub"));
    }

    public static void removeStatus() {
        Main.jda.getPresence().setActivity(null);
    }
}
