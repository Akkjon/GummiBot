package de.akkjon.pr.mbrm.games;


public class Dice {
    public static int throwDice(int count) {
        return (int) (Math.random() * count) + 1;
    }
}
