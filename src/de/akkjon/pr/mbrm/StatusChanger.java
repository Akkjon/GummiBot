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

    public StatusChanger() {

        loadElements();
        initTimer();

    }

    private void loadElements() {
        Gson gson = new Gson();
        String strElements = Storage.getInternalFile("status.json");
        JsonObject elements = gson.fromJson(strElements, JsonObject.class);
        this.elements = elements.get("status").getAsJsonArray();
    }

    private void initTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                timerTask();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 5000);
    }

    private void timerTask() {
        JsonObject object = elements.get(activeIndex).getAsJsonObject();

        Activity.ActivityType type = Activity.ActivityType.valueOf(object.get("type").getAsString());
        String message = object.get("message").getAsString();
        String url = object.get("url").getAsString();

        Main.jda.getPresence().setActivity(Activity.of(type, message, url));
        activeIndex = (activeIndex+1) % elements.size();
    }
}
