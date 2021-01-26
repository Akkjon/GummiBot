package de.akkjon.pr.mbrm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import com.google.gson.*;

import net.dv8tion.jda.api.entities.*;

public class QuoteOfTheDay {
	
	private static final Gson gson = new Gson();
	
	public static String getQotdPath(long serverId) {
		return Storage.rootFolder + serverId + File.separator + "qotd.txt";
	}

	private List<String> data;

	public static boolean removeQotd(long channelId, long serverId) throws IOException {
		JsonObject obj = getQotdObject(serverId);
		if(obj.has(channelId+"")) {
			obj.remove(channelId+"");
			saveJson(gson.toJson(obj), serverId);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean addQotd(long channelId, long serverId) throws IOException {
		JsonObject obj = getQotdObject(serverId);
		if(!obj.has(channelId+"")) {
			obj.addProperty(channelId+"", -1);
			saveJson(gson.toJson(obj), serverId);
			return true;
		} else {
			return false;
		}
	}
	
	public static JsonObject getQotdObject(long serverId) throws IOException {
		String channels = Storage.getFileContent(getQotdPath(serverId), "{}");
		JsonObject obj = gson.fromJson(channels, JsonObject.class);
		return obj;
	}
	
	public static void saveJson(String json, long serverId) throws IOException {
		File f = new File(getQotdPath(serverId));
		if(!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		FileWriter writer = new FileWriter(f);
		writer.write(json);
		writer.close();
	}
	
	
	
	private final long serverId;
	public QuoteOfTheDay(long serverId) {
		this.serverId = serverId;
		Timer timer = new Timer("QuoteOfTheDay-" + serverId, true);
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				timerTask();
			}
		};
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
		long timeNow = System.currentTimeMillis();
		long timeFirstExec = calendar.getTimeInMillis();
		timer.schedule(task, timeFirstExec-timeNow, 1000 * 60 * 60 * 24);
	}
	
	private void timerTask() {
		try {
			if(this.data.size() == 0) {
				loadData();
			}
			//List<String> data = Handler.getData();
			int index = (int)(Math.random() * data.size());
			this.data.remove(index);
			String msg = data.get(index);
			JsonObject obj = QuoteOfTheDay.getQotdObject(serverId);

			JsonArray array = gson.fromJson(Storage.getInternalFile("/animals.json"), JsonObject.class).get("animals").getAsJsonArray();
			int index2 = (int)(Math.random() * array.size());
			String imageUrl = array.get(index2).getAsString();

			for (Entry<String, JsonElement> entry : obj.entrySet()) {
				
				MessageChannel channel=getMessageChannelById(Long.parseLong(entry.getKey()));
				
				if(channel==null) {
					obj.remove(entry.getKey());
					continue;
				}
				
				if(!entry.getValue().isJsonNull() && (entry.getValue().getAsLong()>=0)) {
					long messageId = entry.getValue().getAsLong();
					try {
						MessageHistory.getHistoryAround(channel, messageId + "").complete().getMessageById(messageId).delete().complete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				MessageEmbed msgEmbed = new MessageEmbed(
						null,
						"Message of the day:",
						msg,
						EmbedType.IMAGE,
						null,
						Main.embedColor,
						null,
						null,
						null,
						null,
						new MessageEmbed.Footer("Gummi", null, null),
						new MessageEmbed.ImageInfo(imageUrl, null, -1, -1),
						null);

				long newMsgId = channel.sendMessage(msgEmbed).complete().getIdLong();
				entry.setValue(new JsonPrimitive(newMsgId));
			}
			QuoteOfTheDay.saveJson(gson.toJson(obj), serverId);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private MessageChannel getMessageChannelById(long channelId) {
		List<GuildChannel> guildCannels = Main.jda.getGuildById(serverId).getChannels();
		for (GuildChannel guildChannel : guildCannels) {
			if(guildChannel.getIdLong() == channelId) {
				return (MessageChannel) guildChannel;
			}
		}
		return null;
	}

	private void loadData() {
		try {
			this.data = Handler.getData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
