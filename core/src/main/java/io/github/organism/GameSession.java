package io.github.organism;

import com.badlogic.gdx.InputProcessor;

import java.awt.Point;

public interface GameSession {
    public InputProcessor getInputProcessor();

    public void advanceFrameCount();

    Object getScreen();

    void updateHud(Point p, Organism organism);
}
