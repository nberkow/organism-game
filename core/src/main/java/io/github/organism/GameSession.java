package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;

import java.awt.Point;

public interface GameSession {
    public InputProcessor getInputProcessor();

    public void advanceFrameCount();

    Screen getScreen();

    void updateHud(Point p, Organism organism);
}
