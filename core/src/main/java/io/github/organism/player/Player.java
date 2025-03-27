
package io.github.organism.player;

import java.awt.Point;

import com.badlogic.gdx.graphics.Color;

import io.github.organism.FloatPair;
import io.github.organism.Organism;

public interface Player {

    public Color getColor();

    public int getIndex();


    String getPlayerName();

    Organism getOrganism();

    public float [] gatherInputs();


    int getMostRecentMove();

    void transition();

    void dispose();

    Point getTournamentId();

    Point getAllyId();

    void setAllyId(Point p);

    void makeMove();
}
