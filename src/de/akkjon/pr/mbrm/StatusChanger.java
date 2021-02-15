package de.akkjon.pr.mbrm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Timer;
import java.util.TimerTask;

public class StatusChanger {

    private int activeIndex = 0;
    private JsonArray elements;
    private Timer timer = null;
    private static StatusChanger statusChanger = null;

    public StatusChanger() {
        statusChanger = this;
        loadElements();
        startTimer();

    }

    private void loadElements() {
        Gson gson = new Gson();
        String strElements = Storage.getInternalFile("status.json");
        JsonObject elements = gson.fromJson(strElements, JsonObject.class);
        this.elements = elements.get("status").getAsJsonArray();
    }

    private void startTimer() {
        stopTimer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                timerTask();
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 10000);
    }

    private void stopTimer() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        Main.jda.getPresence().setActivity(null);
    }

    private void timerTask() {
        JsonObject object = elements.get(activeIndex).getAsJsonObject();

        Activity.ActivityType type = Activity.ActivityType.valueOf(object.get("type").getAsString());
        String message = object.get("message").getAsString();

        message = message.replace("[numberServers]", Main.jda.getGuilds().size()+"");

        String url = object.get("url").getAsString();

        Main.jda.getPresence().setActivity(Activity.of(type, message, url));
        activeIndex = (activeIndex+1) % elements.size();
    }

    public static void setStatus() {
        if(statusChanger != null) statusChanger.startTimer();
    }

    public static void removeStatus() {
        if(statusChanger != null) statusChanger.stopTimer();
    }
}
