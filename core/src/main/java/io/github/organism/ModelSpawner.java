package io.github.organism;

import io.github.organism.player.BotPlayer;
import io.github.organism.hud.PlayerHud;
import com.badlogic.gdx.graphics.Color;
import java.awt.Point;

public class ModelSpawner {

    private int nextPrimaryIndex;

    public ModelSpawner(int startingIndex) {
        this.nextPrimaryIndex = startingIndex;
    }

    /**
     * Create a new BotPlayer with random weights via SlimeRLAgent
     * Note: This creates a bot without a GameBoard - it will need to be assigned later
     */
    public BotPlayer createRandomBotPlayer(String name, int gameIndex, Color color) {
        // Create tournament ID
        Point playerId = new Point(nextPrimaryIndex, 0);
        nextPrimaryIndex++;

        // Create a placeholder bot - will be properly initialized when added to a game
        // The actual organism and gameBoard will be set when the bot joins a game
        BotPlayer bot = new BotPlayer(null, name, gameIndex, playerId, null, null, color);

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
