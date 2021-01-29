package de.akkjon.pr.mbrm;

import de.akkjon.pr.mbrm.games.Game;

import java.io.IOException;
import java.util.List;

public class Insult extends Game{

    public Insult(long serverID) {
        super(serverID);
        try {
            loadRemainingList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> remainingList;

    private static final String  fileName = "insults";

    public String getMessage() {
        if(this.remainingList.size()==0) {
            try {
                loadRemainingList();
            } catch (IOException e) {
                e.printStackTrace();
                return Locales.getString("error.internalError5");
            }
        }
        return Game.getFromLists(this.remainingList);
    }

    public static boolean addMessage(String element, long serverId) throws IOException {
        return add(element, "insult", serverId, fileName + ".txt");
    }

    private void loadRemainingList() throws IOException {
        this.remainingList = loadRemaining("insults", fileName);
    }
}
