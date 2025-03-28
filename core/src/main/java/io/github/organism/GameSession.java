package io.github.organism;

import com.badlogic.gdx.InputProcessor;

public interface GameSession {
    public InputProcessor getInputProcessor();

    public void advanceTurnCount();

    Object getScreen();
}
