package io.github.organism;

import io.github.organism.player.BotPlayer;
import io.github.organism.hud.PlayerHud;
import com.badlogic.gdx.graphics.Color;
import java.awt.Point;
import java.util.Random;

public class ModelSpawner {

    private int nextPrimaryIndex;
    private GameBoard gameBoard; // Need reference to create BotPlayers

    public ModelSpawner(int startingIndex, GameBoard gb) {
        this.nextPrimaryIndex = startingIndex;
        this.gameBoard = gb;
    }

    /**
     * Create a new BotPlayer with random weights via SlimeRLAgent
     */
    public BotPlayer createRandomBotPlayer(String name, int gameIndex, Color color) {
        // Create organism with random initialization
        Organism organism = new Organism(gameBoard);
        organism.init_random_weights(); // Or whatever your init method is

        // Create tournament ID
        Point playerId = new Point(nextPrimaryIndex, 0);
        nextPrimaryIndex++;

        // BotPlayer constructor will create SlimeRLAgent.GameRLInterface internally
        PlayerHud hud = new PlayerHud(); // Adjust constructor as needed
        BotPlayer bot = new BotPlayer(gameBoard, name, gameIndex, playerId, organism, hud, color);

        System.out.println("Created random BotPlayer: " + name + " (ID: " + playerId + ")");
        return bot;
    }

    public int getNextPrimaryIndex() {
        return nextPrimaryIndex;
    }

    public void setNextPrimaryIndex(int index) {
        this.nextPrimaryIndex = index;
    }
}
