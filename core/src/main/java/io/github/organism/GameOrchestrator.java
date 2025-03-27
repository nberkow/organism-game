package io.github.organism;

import com.badlogic.gdx.Gdx;

import java.awt.Point;
import java.util.HashMap;

import io.github.organism.player.Player;

public class GameOrchestrator {

    final float VICTORY_THRESHOLD = 2/3f;
    public boolean finished;
    float turn_max;

    int turn = 0;
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
        turn_max = totalTerritory * 3;
        playerTerritory = new HashMap<>();
        currentMoves = new HashMap<>();
        for (Point p : gameBoard.players.keySet()) {
            playerTerritory.put(p, (float) gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices());
        }
        finished = false;
    }

    public void update_speed(float speed){
        actionTime = baseActionTime / speed;
    }

    public void updateTimersAndFlags() {

        if (turn >= turn_max) {
            return;
        }

        actionClock += Gdx.graphics.getDeltaTime();
        if (actionClock > actionTime){
            turn ++;
            gameBoard.session.advanceTurnCount();
            actionClock = actionClock % actionTime;
        }

    }

    public void updatePlayers() {
        for (Player p : gameBoard.players.values()) {
            Organism organism = p.getOrganism();
            if (organism != null){
                organism.updateResources();
            }
            p.makeMove();
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

        if (turn >= turn_max) {
            return leader;
        }

        return null;
    }

    private void makeMoves(HashMap<Point, DoublePair<Double>> allPlayerMoves) {

        /*
        DEPRECATED for now. May want a shared queue
         */

        // move execution rotates order

        for (int i = 0; i< gameBoard.allPlayerIds.size(); i++){
            int p = (i + turn) % 3;
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

    public HashMap<String, String> get_logger_stats() {
        HashMap<String, String> most_recent_move_stats = new HashMap<>();
        most_recent_move_stats.put("turn", String.valueOf(turn));

        int i = 1;
        for (Point p : gameBoard.allPlayerIds) {
            int move = gameBoard.players.get(p).getMostRecentMove();
            int territory = gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices();
            most_recent_move_stats.put("player" + i + "_move", String.valueOf(move));
            most_recent_move_stats.put("player" + i + "_territory", String.valueOf(territory));
            i++;
        }
        return most_recent_move_stats;
    }


    public void dispose() {
    }
}
