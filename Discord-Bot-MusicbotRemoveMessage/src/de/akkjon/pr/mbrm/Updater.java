package de.akkjon.pr.mbrm;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.JsonObject;



public class Updater {
	
	private static final int version = 0;
	private static final String versionUrl = "https://onedrive.live.com/download?cid=B327A7F518EB2758&resid=B327A7F518EB2758%21371414&authkey=AF_cXCt3Fouz7jo";
	
	private static Updater updater = null;
	
	public static Updater getUpdater() {
		return updater;
	}
	
	private Gson gson;
	private String newDownloadUrl = "";
	
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
		String content = "";
	    byte[] dataBuffer = new byte[1024];
	    //int bytesRead;
	    
	    while ((in.read(dataBuffer, 0, 1024)) != -1) {
	        content += new String(dataBuffer);
	    }
	    content = content.trim();
	    JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
	    int versionGot = jsonObject.get("version").getAsInt();
	    System.out.println("Update-Check: active version: " + version + "; up-to-date version: " + versionGot);
	    if(versionGot > version) {
	    	newDownloadUrl = jsonObject.get("downloadUrl").getAsString();
	    	
	    	return true;
	    }
	    
	    return false;
	}
	
	private void update() throws IOException, InterruptedException {
		System.out.println("Updating...");
		shutdownInternals();
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
		Main.jda.shutdown();
	}
	
}
