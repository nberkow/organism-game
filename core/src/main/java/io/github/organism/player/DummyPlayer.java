package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.LinkedList;

import io.github.organism.Organism;

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
     * @return
     */
    @Override
    public int getMostRecentMove() {
        return 0;
    }

    /**
     *
     */
    @Override
    public void transition() {

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
     * @return
     */
    @Override
    public Point getAllyId() {
        return null;
    }

    /**
     * @param p
     */
    @Override
    public void setAllyId(Point p) {

    }

    /**
     *
     */
    @Override
    public void makeMove() {

    }
}
