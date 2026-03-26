package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.awt.Point;

import io.github.organism.GameBoard;
import io.github.organism.Organism;
import io.github.organism.hud.MoveSpaceControl;
import io.github.organism.hud.PlayerHud;

public class DummyPlayer implements Player {

    @Override
    public Color getColor() {
        return null;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public String getPlayerName() {
        return "";
    }

    @Override
    public Organism getOrganism() {
        return null;
    }

    @Override
    public float[] gatherInputs() {
        return new float[0];
    }

    @Override
    public void dispose() {
        // Stub implementation
    }

    @Override
    public Point getTournamentId() {
        return null;
    }

    @Override
    public void makeDecision() {
        // Stub implementation
    }

    @Override
    public void executeMove(Vector2 precisePlanchette) {
        // Stub implementation
    }

    @Override
    public boolean isDecisionReady() {
        return false;
    }

    @Override
    public PlayerHud getHud() {
        return null;
    }

    @Override
    public GameBoard getGameBoard() {
        return null;
    }

    @Override
    public MoveSpaceControl getMoveSpaceControl() {
        return null;
    }
}
