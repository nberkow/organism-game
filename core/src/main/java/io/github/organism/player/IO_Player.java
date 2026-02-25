package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;

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


    int most_recent_move;
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


    /**
     *
     */
    @Override
    public void makeMove() {
        organism.expand(hud.getPlanchetteVector());
        organism.extract(hud.getPlanchetteVector());
    }

}
