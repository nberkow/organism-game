
package io.github.organism.player;

import java.awt.Point;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import io.github.organism.FloatPair;
import io.github.organism.GameBoard;
import io.github.organism.Organism;
import io.github.organism.hud.MoveSpaceControl;
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

    default MoveSpaceControl getMoveSpaceControl() { return null; }

    void dispose();

    Point getTournamentId();

    /**
     * Make a decision for this turn.
     * Bot: runs AI policy, sets cursor.
     * Human: captures input state.
     * Called at start of DECISION phase.
     */
    void makeDecision();

    /**
     * Execute the move using precise planchette position.
     * Called at EXECUTION phase with calculated position.
     * @param precisePlanchette Normalized vector from MoveSpaceControl
     */
    void executeMove(Vector2 precisePlanchette);

    /**
     * Check if decision is complete.
     * @return true when makeDecision() has finished and cursor is set
     */
    boolean isDecisionReady();
    PlayerHud getHud();

    GameBoard getGameboard();
}
