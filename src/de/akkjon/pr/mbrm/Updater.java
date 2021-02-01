package de.akkjon.pr.mbrm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;



public class Updater {
	
	private static double version = 1;
	private static final String versionUrl = "https://onedrive.live.com/download?cid=B327A7F518EB2758&resid=B327A7F518EB2758%21371414&authkey=AF_cXCt3Fouz7jo";

	private static final String versionFilePath = Storage.jarFolder + File.separator + "version.txt";

	static {
		try {
			String versionFile = Storage.getFileContent(versionFilePath, version + "");
			version = Double.parseDouble(versionFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double getVersion() {
		return version;
	}

	private static Updater updater = null;
	
	public static Updater getUpdater() {
		return updater;
	}
	
	private Gson gson;
	private String newDownloadUrl = "";
	private double newVersion = -1;

	public Updater() {
		if(updater != null) {
			return;
		}
		updater = this;
		gson = new Gson();

		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				updateRoutine();
			}
		};
	
		Timer timer = new Timer("Update-Check", true);
		Calendar cal = Calendar.getInstance();
		long now = System.currentTimeMillis();
		if(cal.get(Calendar.MINUTE) >= 30) {
			cal.add(Calendar.MINUTE, 30);
		}
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), 30, 0);
		timer.schedule(task, cal.getTimeInMillis() - now, 1000*60*60); // Every hour
	}
	
	public void updateRoutine() {
		new Thread(() -> {
			boolean newVersionAvail = false;
			try {
				newVersionAvail = isNewVersionAvail();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(newVersionAvail) {
				try {
					update();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	private boolean isNewVersionAvail() throws IOException {
		System.out.println("Checking for updates...");
		BufferedInputStream in = new BufferedInputStream(new URL(versionUrl).openStream());
		StringBuilder content = new StringBuilder();
	    byte[] dataBuffer = new byte[1024];
	    //int bytesRead;
	    
	    while ((in.read(dataBuffer, 0, 1024)) != -1) {
	        content.append(new String(dataBuffer));
	    }
	    JsonObject jsonObject = gson.fromJson(content.toString().trim(), JsonObject.class);
	    newVersion = jsonObject.get("version").getAsDouble();
	    System.out.println("Update-Check: active version: " + version + "; up-to-date version: " + newVersion);
	    if(newVersion > version) {
	    	newDownloadUrl = jsonObject.get("downloadUrl").getAsString();
	    	
	    	return true;
	    }
	    
	    return false;
	}
	
	private void update() throws IOException, InterruptedException {
		System.out.println("Updating...");
		shutdownInternals();

		File file = new File(versionFilePath);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		FileWriter writer = new FileWriter(file);
		writer.write(newVersion + "");
		writer.close();

		BufferedInputStream in = new BufferedInputStream(new URL(newDownloadUrl).openStream());
		String filePath = Storage.getJarName();
		FileOutputStream fileOutputStream = new FileOutputStream(filePath);
	    byte[] dataBuffer = new byte[1024];
	    int bytesRead;
	    
	    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
	        fileOutputStream.write(dataBuffer, 0, bytesRead);
	    }
	    fileOutputStream.close();
	    
	    try {
	    	
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", filePath);
			pb.start();
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void shutdownInternals() throws InterruptedException {
		Handler.stopWebServer();
		Main.jda.shutdownNow();
	}
	
}
