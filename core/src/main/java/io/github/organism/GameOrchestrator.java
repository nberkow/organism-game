package io.github.organism;

import com.badlogic.gdx.math.Vector2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.organism.player.Player;

public class GameOrchestrator {

    private static final float VICTORY_MAP_FILL_THRESHOLD = .9f;

    private static final float MAX_UNPLAYED_TURNS = 3;

    final float VICTORY_THRESHOLD = 1/2f;

    public void updateTerritory(Point tournamentId, float territory) {
        playerTerritory.put(tournamentId, territory);
    }

    public enum TurnPhase { DECISION, EXECUTION, BUFFER }
    private ArrayList<Point> playerTurnOrder;  // Set from gameBoard.allPlayerIds

    public boolean finished;
    GameBoard gameBoard;
    boolean paused = true;
    HashMap<Point, Float> playerTerritory;
    float totalTerritory;
    int resourceExhaustedCountdown = 36;
    boolean show_countdown;
    float unplayedTurns = 0;


    private TurnPhase currentPhase = TurnPhase.DECISION;
    private int currentPlayerIndex = 0;
    private float phaseTimer = 0f;
    private float decisionTimestamp = 0f;  // When current player's decision completed

    // Timing constants (eventually configurable)
    private static final float TURN_DURATION = 1.5f;    // Decision → Execution
    private static final float BUFFER_DURATION = 0.5f;  // Execution → Next player
    private static final int MAX_STALL_CYCLES = 3;      // Crash after this many stalls
    private int stallCycles = 0;

    public GameOrchestrator(GameBoard gb) {
        gameBoard = gb;
        finished = false;
    }

    public void initializeFromGameBoard() {
        playerTurnOrder = gameBoard.allPlayerIds;
        System.out.println("[Orchestrator] playerTurnOrder=" + playerTurnOrder);

        totalTerritory = (float) gameBoard.universeMap.vertexGrid.getUnmaskedVertices();
        playerTerritory = new HashMap<>();
        for (Point p : playerTurnOrder) {
            playerTerritory.put(p, (float) gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices());
        }
    }


    public void update(float delta) {
        if (paused) {
            return;
        }

        phaseTimer += delta;

        Player currentPlayer = getCurrentPlayer();

        //System.out.println("[Orchestrator] phase=" + currentPhase +
        //    " player=" + getCurrentPlayer().getPlayerName() +
        //    " timer=" + phaseTimer);

        switch (currentPhase) {
            case DECISION:
                // Trigger decision at phase start (handled elsewhere on entry)
                // Check if decision is ready
                if (currentPlayer.isDecisionReady()) {
                    decisionTimestamp = phaseTimer;  // Record when decision completed
                    stallCycles = 0;
                }

                // Advance to EXECUTION when time is up AND decision is ready
                if (phaseTimer >= TURN_DURATION) {
                    if (currentPlayer.isDecisionReady()) {
                        currentPhase = TurnPhase.EXECUTION;
                        phaseTimer = 0f;
                    } else {
                        // Stall: wait one buffer cycle
                        stallCycles++;
                        if (stallCycles > MAX_STALL_CYCLES) {
                            throw new RuntimeException(
                                "Player " + currentPlayer.getPlayerName() +
                                    " decision timed out after " + stallCycles + " stall cycles"
                            );
                        }
                        phaseTimer = 0f;  // Reset timer, wait another cycle
                    }
                }
                break;

            case EXECUTION:
                // DECISION phase lasted TURN_DURATION seconds by design
                Vector2 precisePlanchette = currentPlayer.getHud()
                    .getMoveSpaceControl()
                    .getLogicalPlanchettePosition(TURN_DURATION);  // ← 1.5f, not phaseTimer

                currentPlayer.executeMove(precisePlanchette);
                currentPhase = TurnPhase.BUFFER;
                phaseTimer = 0f;
                break;

            case BUFFER:
                // Just wait, visual drift continues
                if (phaseTimer >= BUFFER_DURATION) {
                    // Advance to next player
                    currentPlayerIndex = (currentPlayerIndex + 1) % playerTurnOrder.size();

                    if (currentPlayerIndex == 0){
                        Point winner = testVictoryConditions();
                        if (winner != null) {
                            gameBoard.session.finishThisRound(winner);
                        }
                    }

                    currentPhase = TurnPhase.DECISION;
                    phaseTimer = 0f;

                    // Trigger next player's decision
                    getCurrentPlayer().makeDecision();
                }
                break;
        }
    }

    Player getCurrentPlayer() {
        Point playerId = playerTurnOrder.get(currentPlayerIndex);
        return gameBoard.players.get(playerId);
    }

    // Call this at game start to initialize turn order
    public void startGame() {
        playerTurnOrder = new ArrayList<>(gameBoard.allPlayerIds);
        currentPlayerIndex = 0;
        currentPhase = TurnPhase.DECISION;
        phaseTimer = 0f;
        getCurrentPlayer().makeDecision();  // Trigger first player's decision
    }

    // For debugging/UI
    public TurnPhase getCurrentPhase() { return currentPhase; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public float getPhaseTimer() { return phaseTimer; }


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

        float claimedTerritory = 0;
        for (Point p : gameBoard.players.keySet()) {

            float lastTurnTerritory = playerTerritory.get(p);

            float pTerritory = gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices();
            claimedTerritory += pTerritory;

            // if any player gained territory in the last round reset the counter
            if (pTerritory > lastTurnTerritory) {
                unplayedTurns = 0;
            }

            playerTerritory.put(p, pTerritory);

            if (pTerritory > leader_territory) {
                leader = p;
                leader_territory = pTerritory;
            }

            if (pTerritory / totalTerritory >= VICTORY_THRESHOLD) {
                return p;
            }
        }

        System.out.println("[VictoryCheck] claimedTerritory=" + claimedTerritory +
            " fill_threshold=" + (totalTerritory * VICTORY_MAP_FILL_THRESHOLD) +
            " leader=" + leader);

        if (claimedTerritory > totalTerritory * VICTORY_MAP_FILL_THRESHOLD) {
            System.out.println("[VictoryCheck] MAP FILL WINNER: " + leader);
            return leader;
        }

        if (claimedTerritory > totalTerritory * VICTORY_MAP_FILL_THRESHOLD) {
            return leader;
        }

        if (unplayedTurns >= MAX_UNPLAYED_TURNS){
            return leader;
        }


        if (resourceExhaustedCountdown <= 0) {
            return leader;
        }

        return null;
    }


    public void run(){
        paused = false;
    }

    public void pause() {
        paused = true;
    }


    public void dispose() {
    }

    public void render() {

    }
}
