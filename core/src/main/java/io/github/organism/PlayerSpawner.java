package io.github.organism;

import io.github.organism.player.BotPlayer;
import io.github.organism.hud.PlayerHud;
import com.badlogic.gdx.graphics.Color;
import java.awt.Point;

public class PlayerSpawner {

    private int nextPrimaryIndex;
    private GameBoard gameBoard;

    public PlayerSpawner(int startingIndex, GameBoard gb) {
        this.nextPrimaryIndex = startingIndex;
        this.gameBoard = gb;
    }

    /**
     * Create a new BotPlayer with random weights, fully initialized
     */
    public BotPlayer createRandomBotPlayer(String name, int gameIndex, Color color, GameBoard gameBoard) {
        Point playerId = new Point(nextPrimaryIndex, 0);
        nextPrimaryIndex++;

        // Create organism with random init
        Organism organism = new Organism(gameBoard);

        // Pass null for HUD - BotPlayer should handle null HUD gracefully for tournament mode
        // If it doesn't, we'll fix that specific NPE when it occurs
        PlayerHud hud = null;

        BotPlayer bot = new BotPlayer(gameBoard, name, gameIndex, playerId, organism, hud, color, null);
        System.out.println("Created: " + name + " (ID: " + playerId + ")");
        return bot;
    }

    public int getNextPrimaryIndex() {
        return nextPrimaryIndex;
    }

    public void setNextPrimaryIndex(int index) {
        this.nextPrimaryIndex = index;
    }
}
