package de.akkjon.pr.mbrm;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ServerWatcher {
	
	private static List<WeakReference<ServerWatcher>> serverWatchers = new ArrayList<>();
	
	private long serverId;
	private Long[] channels;
	private String prefix = "~";
	
	public ServerWatcher(long serverId) throws AlreadyBoundException {
		for (WeakReference<ServerWatcher> weakReference : serverWatchers) {
			if(weakReference.get().getGuildId()==serverId) {
				throw new AlreadyBoundException("Server Watcher already exists");
			}
		}
		this.serverId = serverId;
		initChannels();
		initCommandListeners();
		new QuoteOfTheDay(serverId);
	}
	
	
	
	public long getGuildId() {
		return this.serverId;
	}
	
	private void initChannels() {
		this.channels = Storage.getChannels(serverId);
	}
	
	private boolean isChannelRegistered(long channelId) {
		for (Long long1 : channels) {
			if(long1==channelId) return true;
		}
		return false;
	}
	
	private void initCommandListeners() {
		Main.jda.addEventListener(new ListenerAdapter() {
			@Override
			public void onMessageReceived(MessageReceivedEvent event) {
				if(event.isFromGuild()) {
					if(event.getGuild().getIdLong() == serverId) {
						long channelId = event.getChannel().getIdLong();
						String content = event.getMessage().getContentRaw();
						if(content.startsWith(prefix)) { 
							content = content.substring(prefix.length());
							String[] args = Arrays.asList(content.split(" ")).stream().filter(e -> e.length()>0).toArray(String[]::new);
							if(args.length==0) {
								return;
							}
							if(args[0].equalsIgnoreCase("addchannel")) {
								if(!isChannelRegistered(channelId)) {
									try {
										ServerWatcher.this.channels = Storage.addChannel(serverId, channelId);
										event.getChannel().sendMessage(Main.getEmbedMessage("Success", "You wanna make me your slave? Sure...")).complete();
									} catch (IOException e) {
										event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I am dumb... I encountered an error.")).complete();
										e.printStackTrace();
									}
									
								} else {
									event.getChannel().sendMessage(Main.getEmbedMessage("Error", "What do you want from me, bitch? I am already working here.")).complete();
								}
							} else if (args[0].equalsIgnoreCase("removechannel")) {
								if(isChannelRegistered(channelId)) {
									try {
										ServerWatcher.this.channels = Storage.removeChannel(serverId, channelId);
										event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Alright, fuck you. Bye")).complete();
									} catch (IOException e) {
										event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I am dumb... I encountered an error.")).complete();
										e.printStackTrace();
									}
									
								} else {
									event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I was never watchning this fookin channel.")).complete();
								}
							} else if (args[0].equalsIgnoreCase("addqotd")) {
								try {
									boolean isAdded = QuoteOfTheDay.addQotd(event.getChannel().getIdLong(), event.getGuild().getIdLong());
									if(isAdded) {
										event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Ok my master.")).complete();
									} else {
										event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I am allready here, don't you see me? OPEN YOUR GOD DAMNED EYES.")).complete();
									}
								} catch(Exception e) {
									event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I fucking hate my life... AN ERROR AGAIN")).complete();
									e.printStackTrace();
								}
							} else if (args[0].equalsIgnoreCase("removeqotd")) {
								try {
									boolean isRemoved = QuoteOfTheDay.addQotd(event.getChannel().getIdLong(), event.getGuild().getIdLong());
									if(isRemoved) {
										event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Doby is free.")).complete();
									} else {
										event.getChannel().sendMessage(Main.getEmbedMessage("Error", "You cannot remove me, IF I AM NOT HERE. Fuck off.")).complete();
									}
								} catch(Exception e) {
									event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "Fuck, shit, ahhh, NOOOOOO. I made a mistake...")).complete();
									e.printStackTrace();
								}
							} else if (args[0].equalsIgnoreCase("truth")) {
								try {
									event.getChannel().sendMessage(Main.getEmbedMessage("Truth", TruthOrDare.getTruth(serverId))).complete();
								} catch (IOException e) {
									event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "Why is life so fucking difficult?")).complete();
									e.printStackTrace();
								}
							} else if (args[0].equalsIgnoreCase("dare")) {
								try {
									event.getChannel().sendMessage(Main.getEmbedMessage("Dare", TruthOrDare.getDare(serverId))).complete();
								} catch (IOException e) {
									event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "Why is life so fucking difficult?")).complete();
									e.printStackTrace();
								}
							} else if (args[0].equalsIgnoreCase("addtruth")) {
								if(args.length > 1) {
									String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
									try {
										boolean isAdded = TruthOrDare.addTruth(newElement, serverId);
										if(isAdded) {
											event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Added \"" + newElement + "\"")).complete();
										} else {
											event.getChannel().sendMessage(Main.getEmbedMessage("Error", "You fucking goblin.")).complete();
										}
									} catch (IOException e) {
										event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "I am fucking retarded... You are as well.")).complete();
										e.printStackTrace();
									}
									
								} else {
									event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I don't think so...")).complete();
								}
							} else if (args[0].equalsIgnoreCase("adddare")) {
								if(args.length > 1) {
									String newElement = String.join(" ", Arrays.asList(args).subList(1, args.length));
									try {
										boolean isAdded = TruthOrDare.addDare(newElement, serverId);
										if(isAdded) {
											event.getChannel().sendMessage(Main.getEmbedMessage("Success", "Added \"" + newElement + "\"")).complete();
										} else {
											event.getChannel().sendMessage(Main.getEmbedMessage("Error", "I fucking hate you. It's already registered...")).complete();
										}
									} catch (IOException e) {
										event.getChannel().sendMessage(Main.getEmbedMessage("Internal error", "You are annoying... I made a mistake because of you.")).complete();
										e.printStackTrace();
									}
									
								} else {
									event.getChannel().sendMessage(Main.getEmbedMessage("Error", "How dare you?")).complete();
								}
							} else {
								event.getChannel().sendMessage(
										"```Channel-Cleaning:\n"
										+ "~addchannel\n"
										+ "~removechannel\n"
										+ "\n"
										+ "Quote of the day\n"
										+ "~addqotd\n"
										+ "~removeqotd\n"
										+ "\n"
										+ "Truth or dare\n"
										+ "~truth\n"
										+ "~dare\n"
										+ "~addtruth\n"
										+ "~adddare```").complete();
							}
						}
						
						if(isChannelRegistered(channelId)) {
							removeMessages(event.getChannel(), event.getMessageId());
						}
					}
				}
			}
		});
	}
	
	private void removeMessages(MessageChannel channel, String messageId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				MessageHistory messageHistory = MessageHistory.getHistoryBefore(channel, messageId).complete();
				List<Message> messages = messageHistory.getRetrievedHistory();
				for(int i = 0; i<messages.size()-1; i++) {
					try {
						messages.get(i).delete().complete();
					} catch (Exception e) {}
				}
			}
		}).start();
	}
	
	
	
	
}
