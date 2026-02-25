package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.LinkedList;

import io.github.organism.GameBoard;
import io.github.organism.Organism;
import io.github.organism.hud.PlayerHud;

public class DummyPlayer implements Player {

    /**
     * @return
     */
    @Override
    public Color getColor() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public int getIndex() {
        return 0;
    }

    /**
     * @return
     */

    /**
     * @return
     */
    @Override
    public String getPlayerName() {
        return "";
    }

    /**
     * @return
     */
    @Override
    public Organism getOrganism() {
        return null;
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

    }

    /**
     * @return
     */
    @Override
    public Point getTournamentId() {
        return null;
    }


    /**
     *
     */
    @Override
    public void makeMove() {

    }

    /**
     * @return
     */
    @Override
    public PlayerHud getHud() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GameBoard getGameboard() {
        return null;
    }
}
