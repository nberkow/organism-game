package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.HashMap;

public interface GameSession {
    public InputProcessor getInputProcessor();

    public void advanceFrameCount();

    Screen getScreen();

    void updateHud(Point p, Organism organism);

    void finishThisRound(Point winner);

    String getPlayerName(Point p);

    Color getPlayerColor(Point p);

    HashMap<Point, String> getPlayerNames();

    Point getWinRecord(Point p);

    void togglePause();

    GameBoard getGameBoard();
}
