package de.akkjon.pr.mbrm;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {
	private static final String token = "Nzk5MzcxNTE3NzgzMzEwMzM2.YACmvQ.eVLv8VH9MYL7k3UjWaWsEWIcoCo";
	public static JDA jda;
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			jda.getCategoryById(802719239723024414L).getTextChannels().forEach(channel -> channel.delete().complete());
		}));
		Logger.init();
		try {
			Handler.initWebServer();
		} catch (IOException e) {
			System.err.println("NEIN! KEIN WEBSERVER, ARSCH, KEIN PORT FREI, MEH");
			e.printStackTrace();
			System.exit(0);
		}
		
		new Updater();
		try {
			jda = JDABuilder.createDefault(token).build();
			try {
				jda.awaitReady();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			new Main();
			setIcon();
		} catch (LoginException e) {
			System.err.println("What? No");
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
		for(long server : Storage.getServers()) {
			try {
				new ServerWatcher(server);
			} catch (AlreadyBoundException e) {
				System.err.println("Cannot start Watcher for server " + server);
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
			System.err.println("Cannot start Watcher for server " + serverId);
			e.printStackTrace();
		}
	}
	
	public static MessageEmbed getEmbedMessage(String title, String message) {
		MessageEmbed embed = new MessageEmbed(
				null,
				title,
				message,
				EmbedType.UNKNOWN,
				null,
				new Color(0, 255, 0).getRGB(),
				null,
				null,
				null,
				null,
				new MessageEmbed.Footer("Gummi", null, null),
				null,
				new ArrayList<>()
			);
		return embed;
	}

	private static void setIcon() {
		
		if(SystemTray.isSupported()) {
			Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/GummiIcon.png"));
			
			try {
				TrayIcon icon = new TrayIcon(image, "Gummi-Dashboard");
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
