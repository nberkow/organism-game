package io.github.organism;

import com.badlogic.gdx.Gdx;

import java.awt.Point;
import java.util.HashMap;

import io.github.organism.player.Player;

public class GameOrchestrator {

    final float VICTORY_THRESHOLD = 2/3f;
    public boolean finished;
    float frameMax;

    int frame = 0;
    GameBoard gameBoard;
    double baseActionTime = 1d;
    double actionTime = baseActionTime;

    double actionClock = 0d;
    boolean paused = true;
    HashMap<Point, Float> playerTerritory;
    HashMap<Point, Integer> currentMoves;
    float totalTerritory;
    int resourceExhaustedCountdown = 36;
    boolean show_countdown;

    public GameOrchestrator(GameBoard gb) {
        gameBoard = gb;
        totalTerritory = (float) gameBoard.universeMap.vertexGrid.getUnmaskedVertices();
        frameMax = totalTerritory * 3;
        playerTerritory = new HashMap<>();
        currentMoves = new HashMap<>();
        for (Point p : gameBoard.players.keySet()) {
            playerTerritory.put(p, (float) gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices());
        }
        finished = false;
    }

    public void updateSpeed(float speed){
        actionTime = baseActionTime / speed;
    }

    public void updateTimersAndFlags() {

        if (frame >= frameMax) {
            return;
        }

        actionClock += Gdx.graphics.getDeltaTime();
        if (actionClock > actionTime){
            frame++;
            gameBoard.session.advanceFrameCount();
            actionClock = actionClock % actionTime;
        }

    }

    public void updatePlayers() {
        for (Point p : gameBoard.players.keySet()) {
            Player player = gameBoard.players.get(p);
            Organism organism = player.getOrganism();
            if (organism != null){
                organism.updateResources();
                gameBoard.session.updateHud(p, organism);
            }
        }
    }

    public Point testVictoryConditions() {

        Point leader = null;
        float leader_territory = 0;

        int remaining_resources = gameBoard.count_resources();

        if (show_countdown) {
            resourceExhaustedCountdown -= 1;
        }

        if (remaining_resources == 0) {
            show_countdown = true;
        }

        for (Point p : gameBoard.players.keySet()) {
            float p_territory = gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices();
            playerTerritory.put(p, p_territory);
            if (p_territory > leader_territory) {
                leader = p;
                leader_territory = p_territory;
            }

            if (p_territory / totalTerritory >= VICTORY_THRESHOLD) {
                return p;
            }
        }

        if (resourceExhaustedCountdown <= 0) {
            return leader;
        }

        if (frame >= frameMax) {
            return leader;
        }

        return null;
    }

    private void makeMoves() {

        for (int i = 0; i< gameBoard.allPlayerIds.size(); i++){
            int p = (i + frame) % 3; // shift the first player each frame
            Player player = gameBoard.players.get(gameBoard.allPlayerIds.get(p));
            Organism organism = player.getOrganism();
            if (organism != null) {
                organism.updateIncome();
            }
            player.makeMove();
        }
    }
    public void run(){
        paused = false;
    }

    public void pause() {
        paused = true;
    }

    public void advanceFrame() {
        updatePlayers();
        updateTimersAndFlags();
        makeMoves();
    }

    public void dispose() {
    }

}
