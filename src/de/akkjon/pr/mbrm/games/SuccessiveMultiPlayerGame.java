package de.akkjon.pr.mbrm.games;

public class SuccessiveMultiPlayerGame extends MultiPlayerGame {

    long lastPlayer;
    boolean isStarted = false;

    SuccessiveMultiPlayerGame(long serverId) {
        super(serverId);
    }

    long getNextPlayer() {
        int indexOflastPlayer = players.indexOf(lastPlayer);
        if (++indexOflastPlayer >= players.size()) indexOflastPlayer = 0;
        return this.lastPlayer = players.get(indexOflastPlayer);
    }

    @Override
    void removePlayer(long id) {
        if (players.size() <= 1) {
            channel.delete().complete();
            return;
        }
        if (this.lastPlayer == id) sendMessage();
        players.remove(id);
    }

    void sendMessage() {

    }

}
