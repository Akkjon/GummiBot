package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandRunnable {

    void run(MessageReceivedEvent event, String[] args);

}
