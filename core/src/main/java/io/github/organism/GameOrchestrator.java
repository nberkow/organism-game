package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

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

    int blinkingPlayerIdx = 0;
    float blinkPeriod = 1f/3;
    float blinkTime = 0;

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

            actionClock = actionClock % actionTime;
        }

    }

    public void updatePlayers() {
        for (Point playerId : gameBoard.players.keySet()) {
            Player player = gameBoard.players.get(playerId);
            Organism organism = player.getOrganism();
            if (organism != null){
                organism.updateResources();
                gameBoard.session.updateHud(playerId, organism);
            }
        }
    }

    public Point testVictoryConditions() {

        Point leader = null;
        float leader_territory = 0;

        int remaining_resources = gameBoard.countResources();

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
        for (int i = 0; i < gameBoard.allPlayerIds.size(); i++){
            Player player = gameBoard.players.get(gameBoard.allPlayerIds.get(i));
            if (player != null) {
                player.makeMove();
            }
        }
    }

    public void run(){
        paused = false;
    }

    public void pause() {
        paused = true;
    }

    public void update(float timeDelta) {

        blinkTime += timeDelta;
        if (blinkTime > blinkPeriod){
            blinkingPlayerIdx = (blinkingPlayerIdx + 1) % 3;
            blinkTime = blinkTime % blinkPeriod;
        }

        updatePlayers();
        
        if (!paused) {
            actionClock += timeDelta;
            if (actionClock >= actionTime) {
                actionClock = actionClock % actionTime;
                frame++;
                makeMoves();
            }
        }
    }

    public void dispose() {
    }

    public Player getBlinkingPlayer() {
        Point blinkingPlayerId = gameBoard.allPlayerIds.get(blinkingPlayerIdx);
        return (gameBoard.players.get(blinkingPlayerId));
    }

    public void render() {

    }
}
