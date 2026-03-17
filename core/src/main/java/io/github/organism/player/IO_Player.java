package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.awt.Point;

import io.github.organism.ActionHistory;
import io.github.organism.GameBoard;
import io.github.organism.Organism;
import io.github.organism.hud.*;

public class IO_Player  implements Player {

    String player_name;


    Point tournamentId; // id in tournament or other large player collection

    Point allyId;
    int gameIndex; // index within game

    Color color;
    Organism organism;

    GameBoard gameBoard;
    private boolean decisionReady;

    int most_recent_move;
    private Vector2 lockedInput;
    ActionHistory moveHistory;

    PlayerHud hud;

    public IO_Player(GameBoard gb, String name, int idx, Point id, Organism org, PlayerHud h, Color c){

        gameBoard = gb;
        player_name = name;
        hud = h;

        gameIndex = idx;
        tournamentId = id;
        organism = org;
        color = c;

        decisionReady = false;
        lockedInput = new Vector2();
    }

    public PlayerHud getHud() {
        return hud;
    }

    /**
     * @return
     */
    @Override
    public GameBoard getGameboard() {
        return gameBoard;
    }

    /**
     * @return
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     * @return
     */
    @Override
    public int getIndex() {
        return gameIndex;
    }

    /**
     * @return
     */



    @Override
    public String getPlayerName() {
        return player_name;
    }


    @Override
    public Organism getOrganism() {
        return organism;
    }

    /**
     * @return
     */
    @Override
    public float[] gatherInputs() {
        return new float[0];
    }


    /**
     *
     */
    @Override
    public void dispose() {
        organism.dispose();
        gameBoard = null;
        moveHistory = null;
    }

    /**
     * @return
     */
    @Override
    public Point getTournamentId() {
        return tournamentId;
    }


    @Override
    public void makeDecision() {
        decisionReady = false;

        // Capture current planchette position (not cursor)
        // Keep the magnitude for energy budget calculation
        if (hud != null) {
            // Get the raw planchette position (not normalized)
            lockedInput.set(hud.getMoveSpaceControl().planchetteFromCenterVector);
        }

        decisionReady = true;
    }

    @Override
    public void executeMove(Vector2 precisePlanchette) {
        // Use the locked input from decision phase
        organism.expand(lockedInput);
        organism.extract(lockedInput);
    }

    @Override
    public boolean isDecisionReady() {
        return decisionReady;
    }

    @Override
    public MoveSpaceControl getMoveSpaceControl() {
        return hud != null ? hud.moveSpaceControl : null;
    }

}
