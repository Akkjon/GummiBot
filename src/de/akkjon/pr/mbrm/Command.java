package de.akkjon.pr.mbrm;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;

public class Command {

    private final boolean adminRequired;
    private final List<String> labels;
    private final CommandRunnable run;

    public Command(String[] labels, boolean adminRequired, CommandRunnable run) {
        this.adminRequired = adminRequired;
        this.labels = Arrays.asList(labels);
        this.run = run;
    }

    public boolean isAdminRequired() {
        return adminRequired;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void run(MessageReceivedEvent event, String[] args, ServerWatcher serverWatcher) {
        if(event.getMember() == null) return;
        if (isAdminRequired()) {
            boolean isAdmin = Commands.isAdmin(event);
            if (!isAdmin) {
                event.getChannel().sendMessage(
                        Main.getEmbedMessage("Nö!", "Darfst du nicht!")).complete();
                return;
            }
        }
        run.run(event, args, serverWatcher);
    }
}
