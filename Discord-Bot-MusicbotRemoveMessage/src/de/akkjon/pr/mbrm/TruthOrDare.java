package de.akkjon.pr.mbrm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TruthOrDare {
	
	private static final Gson gson = new Gson();
	
	public static String getTruth(long serverId) throws IOException {
		JsonArray global = getGlobal("truth");
		JsonArray server = getServer("truth", serverId);
		return getFromLists(global, server);
	}
	
	public static String getDare(long serverId) throws IOException {
		JsonArray global = getGlobal("dare");
		JsonArray server = getServer("dare", serverId);
		return getFromLists(global, server);
	}
	public static boolean addTruth(String element, long serverId) throws IOException {
		return add(element, "truth", serverId);
	}
	public static boolean addDare(String element, long serverId) throws IOException {
		return add(element, "dare", serverId);
	}
	
	public static boolean add(String element, String mode, long serverId) throws IOException {
		String path = Storage.rootFolder + serverId + File.separator + "tod.txt";
		JsonObject object = gson.fromJson(Storage.getFileContent(path, "{}"), JsonObject.class);
		if(!object.has(mode)) {
			object.add(mode, new JsonArray());
		}
		JsonArray array = object.get(mode).getAsJsonArray();
		if(!array.contains(new JsonPrimitive(element))) {
			array.add(element);
			String content = gson.toJson(object);
			
			File file = new File(path);
			if(!file.exists()) {
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
		int value = (int)(Math.random() * max);
		if(value < global.size()) {
			return global.get(value).getAsString();
		} else {
			value = value % global.size();
			return server.get(value).getAsString();
		}
	}
	
	private static JsonArray getGlobal(String mode) {
		String filecontent = Storage.getInternalFile("/de/akkjon/pr/mbrm/resource/tod.json");
		Gson gson = new Gson();
		JsonObject element = gson.fromJson(filecontent, JsonObject.class);
		JsonArray array = element.get(mode).getAsJsonArray();
		return array;
	}
	
	private static JsonArray getServer(String mode, long serverId) throws IOException {
		JsonObject element = getServerInternal(serverId);
		if(element.has(mode)) {
			return element.get(mode).getAsJsonArray();
		}
		return new JsonArray(0);
	}
	
	private static JsonObject getServerInternal(long serverId) throws IOException {
		String fileContent = Storage.getFileContent(Storage.rootFolder + serverId + File.separator + "tod.txt", "{}");
		JsonObject element = gson.fromJson(fileContent, JsonObject.class);
		return element;
	}
}
