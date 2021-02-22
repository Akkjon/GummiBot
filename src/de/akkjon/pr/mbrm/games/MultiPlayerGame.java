package de.akkjon.pr.mbrm.games;

import java.util.ArrayList;

public class MultiPlayerGame extends Game{

    final ArrayList<Long> players = new ArrayList<>();



    protected MultiPlayerGame(long serverId) {
        super(serverId);
    }

    void addPlayer(long id) {
        if (!players.contains(id)) players.add(id);
    }

    void removePlayer(long id) {
        if (players.size() <= 1) {
            channel.delete().complete();
            return;
        }
        players.remove(id);
    }

}
