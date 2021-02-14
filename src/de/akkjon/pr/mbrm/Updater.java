package de.akkjon.pr.mbrm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Updater {

    private static double version = 1;
    private static final String versionUrl = "https://api.github.com/repos/Akkjon/Gummibot/releases";

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

    private static final Gson gson = new Gson();
    private String newDownloadUrl = "";
    private double newVersion = -1;

    public Updater() {
        if (updater != null) {
            return;
        }
        updater = this;

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                updateRoutine();
            }
        };

        Timer timer = new Timer("Update-Check", true);
        Calendar cal = Calendar.getInstance();
        long now = System.currentTimeMillis();
        if (cal.get(Calendar.MINUTE) >= 30) {
            cal.add(Calendar.MINUTE, 30);
        }
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), 30, 0);
        timer.schedule(task, cal.getTimeInMillis() - now, 1000 * 60 * 60); // Every hour
    }

    public void updateRoutine() {
        if (!Main.isEnabled) return;
        new Thread(() -> {
            boolean newVersionAvail = false;
            try {
                newVersionAvail = isNewVersionAvail();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (newVersionAvail) {
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

        HTTPSConnection connection = new HTTPSConnection(versionUrl);
        if (connection.isConnectionSuccess()) {
            if (connection.isResponseSuccess()) {
                JsonArray jsonArray = gson.fromJson(connection.getResponse(), JsonArray.class);
                JsonObject lastRelease = jsonArray.get(0).getAsJsonObject();
                double localNewestVersion = lastRelease.get("tag_name").getAsDouble();
                System.out.println("Found version " + localNewestVersion);

                JsonArray assets = lastRelease.get("assets").getAsJsonArray();
                if (assets.size() > 0) {
                    this.newVersion = localNewestVersion;

                    if (newVersion > version) {
                        this.newDownloadUrl = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                        return true;
                    }
                } else {
                    System.err.println("Updater: Release " + localNewestVersion + " cannot be analyzed, as there is not asset uploaded.");
                }
            } else {
                System.err.println("Updater: Response by " + versionUrl + " returned an error");
            }
        } else {
            System.err.println("Updater: Connection to " + versionUrl + " returned an error");
        }
        return false;
    }

    private void update() throws IOException, InterruptedException {
        System.out.println("Updating... (" + newVersion + ")");
        ServerWatcher.logError("Updating now... (" + newVersion + ")");
        String filePath = Storage.getJarName();
        if (filePath == null) {
            System.out.println("Error: file does not end with .jar");
            return;
        }
        shutdownInternals();

        File file = new File(versionFilePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        FileWriter writer = new FileWriter(file);
        writer.write(newVersion + "");
        writer.close();

        BufferedInputStream in = new BufferedInputStream(new URL(newDownloadUrl).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        byte[] dataBuffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        fileOutputStream.close();

        try {

            ProcessBuilder pb = new ProcessBuilder("java", "-jar", filePath, Double.toString(getVersion()));
            pb.start();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdownInternals() throws InterruptedException {
        Handler.stopWebServer();
        Main.jda.shutdownNow();
    }

    public static void sendChangelog() {

        String changelog = Storage.getInternalFile("changelog.json");
        JsonObject jsonObject = gson.fromJson(changelog, JsonObject.class);
        boolean print = false;
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            double version = Double.parseDouble(entry.getKey());
            if (!print) {
                if (version > Main.getVersionPrior()) {
                    print = true;
                }
            }
            if (print) {
                out.append("\n" + version);
                for (JsonElement change : entry.getValue().getAsJsonArray()) {
                    out.append("\n- " + change.getAsString());
                }
            }
        }
        if (print) {
            ServerWatcher.sendChangelog(out.substring(1));
        }
    }

}
