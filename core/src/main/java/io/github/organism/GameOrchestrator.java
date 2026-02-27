package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.organism.player.Player;

public class GameOrchestrator {

    public enum TurnPhase { DECISION, EXECUTION, BUFFER }
    private ArrayList<Point> playerTurnOrder;  // Set from gameBoard.allPlayerIds
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
        playerTurnOrder = gameBoard.allPlayerIds;
        totalTerritory = (float) gameBoard.universeMap.vertexGrid.getUnmaskedVertices();
        frameMax = totalTerritory * 3;
        playerTerritory = new HashMap<>();
        currentMoves = new HashMap<>();
        for (Point p : gameBoard.players.keySet()) {
            playerTerritory.put(p, (float) gameBoard.players.get(p).getOrganism().territoryVertex.getUnmaskedVertices());
        }
        finished = false;


    }

    public void update(float delta) {
        phaseTimer += delta;

        Player currentPlayer = getCurrentPlayer();

        System.out.println("[Orchestrator] phase=" + currentPhase +
            " player=" + getCurrentPlayer().getPlayerName() +
            " timer=" + phaseTimer);

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
                // Calculate precise planchette position and execute
                float elapsedSinceDecision = phaseTimer;  // Time since DECISION phase ended
                Vector2 precisePlanchette = currentPlayer.getHud()
                    .getMoveSpaceControl()
                    .getLogicalPlanchettePosition(elapsedSinceDecision);

                currentPlayer.executeMove(precisePlanchette);

                // Immediately advance to BUFFER
                currentPhase = TurnPhase.BUFFER;
                phaseTimer = 0f;
                break;

            case BUFFER:
                // Just wait, visual drift continues
                if (phaseTimer >= BUFFER_DURATION) {
                    // Advance to next player
                    currentPlayerIndex = (currentPlayerIndex + 1) % playerTurnOrder.size();
                    currentPhase = TurnPhase.DECISION;
                    phaseTimer = 0f;

                    // Trigger next player's decision
                    getCurrentPlayer().makeDecision();
                }
                break;
        }
    }

    private Player getCurrentPlayer() {
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


    public void run(){
        paused = false;
    }

    public void pause() {
        paused = true;
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
