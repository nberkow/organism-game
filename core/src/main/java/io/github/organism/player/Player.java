
package io.github.organism.player;

import java.awt.Point;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;

import io.github.organism.FloatPair;
import io.github.organism.GameBoard;
import io.github.organism.Organism;
import io.github.organism.hud.PlayerHud;

public interface Player {

    public Color getColor();

    public int getIndex();


    String getPlayerName();

    Organism getOrganism();
    
    default int[] getResourceCounts() {
        Organism organism = getOrganism();
        return organism != null ? organism.resources : new int[]{0, 0, 0};
    }

    public float [] gatherInputs();

    void dispose();

    Point getTournamentId();

    void makeMove();

    PlayerHud getHud();

    GameBoard getGameboard();
}
