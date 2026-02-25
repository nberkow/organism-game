package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import java.awt.Point;

import io.github.organism.GameBoard;
import io.github.organism.Organism;
import io.github.organism.hud.PlayerHud;
import io.github.organism.learning.DiscountedRewardTracker;
import io.github.organism.learning.PerformanceReporter;
import io.github.organism.learning.ResourceRewardData;
import io.github.organism.learning.SlimeRLAgent;
import io.github.organism.map.GridPosition;
import io.github.organism.map.MapHex;
import io.github.organism.map.MapVertex;

public class BotPlayer implements Player {
    public GameBoard gameBoard;
    public int gameIndex; // index within a single game
    public Point tournamentId; // id in tournament or other large player collection

    public Color color;
    public String playerName;
    public SlimeRLAgent.GameRLInterface modelInterface;

    public Organism organism;

    public PlayerHud hud;

    private ResourceRewardData previousRewardData;
    private DiscountedRewardTracker rewardTracker;
    private PerformanceReporter reporter;

    int currentTurn;

    public BotPlayer(GameBoard gb, String name, int idx, Point id, Organism org, PlayerHud h, Color c){

        gameBoard = gb;
        color = c;
        playerName = name;
        tournamentId = id;
        gameIndex = idx;
        organism = org;
        modelInterface = new SlimeRLAgent.GameRLInterface(this,9);
        hud = h;

        this.rewardTracker = new DiscountedRewardTracker(0.95f); // gamma = 0.95
        this.reporter = new PerformanceReporter("logs");
        currentTurn = 0;
        
        System.out.println("BotPlayer created: " + name + " (ID: " + id + ")");
    }

    public float [] gatherInputs(){

        int r = gameBoard.universeMap.hexGrid.getRadius();
        Vector2 cursor = hud.getBotInputVector();
        MapHex hex = findHexAtCursor(cursor, r);

        float [] inputs = new float[18];
        inputs[0] = (float) hex.pos.i / r;
        inputs[1] = (float) hex.pos.j / r;
        inputs[2] = (float) hex.pos.k / r;

        for (int i=0; i<hex.filledResourceSlots; i++) {
            inputs[3 + i] = 1;
        }

        int i = 6;
        int m = 0;
        int n = 0;

        for (MapVertex v : hex.vertexList) {
            if (v.player != null) {
                if (v.player == this){
                    n = 0;
                }
                else {
                    n = 1;
                }
            }
            inputs[i + m + n]  = 1;
            m += 2;
        }

        return inputs;
    }

    MapHex findHexAtCursor(Vector2 cursor, int mapRadius) {
        MapHex closest = null;
        float minDist = Float.MAX_VALUE;

        for (GridPosition pos : gameBoard.universeMap.hexGrid) {
            MapHex hex = (MapHex) pos.content;
            float i = (float) pos.i / mapRadius;
            float j = (float) pos.j / mapRadius;
            float k = (float) pos.k / mapRadius;

            Vector2 hexCenter = new Vector2(
                (float) ((j * Math.pow(3f, 0.5f) / 2f) - (k * Math.pow(3f, 0.5f) / 2f)),
                i - j / 2f - k / 2f
            );

            float dist = hexCenter.dst(cursor);
            if (dist < minDist) {
                minDist = dist;
                closest = hex;
            }
        }
        return closest;
    }

    public PlayerHud getHud() {
        return hud;
    }

    /**
     * @return
     */
    @Override
    public GameBoard getGameboard() {
        return gameBoard;
    }

    /**
     * @return
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     * @return
     */
    @Override
    public int getIndex() {
        return gameIndex;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    public Organism getOrganism(){
        return organism;
    }

    @Override
    public void dispose() {
        gameBoard = null;
        modelInterface.dispose();
        organism.dispose();
    }

    /**
     * @return
     */
    @Override
    public Point getTournamentId() {
        return tournamentId;
    }

    /**
     *
     */
    @Override

    public void makeMove() {

        currentTurn++;

        // 1. Capture state before move
        ResourceRewardData before = new ResourceRewardData(this);

        // 2. Get and execute move
        Vector2 newCursor = modelInterface.nextMove();
        hud.setBotCursorVector(newCursor);
        organism.expand(hud.getPlanchetteVector());
        organism.extract(hud.getPlanchetteVector());

        // 3. Capture state after move
        ResourceRewardData after = new ResourceRewardData(this);

        // 4. Calculate discounted reward
        float immediateReward = ResourceRewardData.calculateDeltaReward(before, after, 1.0f);
        float discountedReturn = rewardTracker.getDiscountedReturn(currentTurn, 10);
        float totalReward = immediateReward + 0.3f * discountedReturn; // Blend

        // 5. Record for learning
        rewardTracker.addReward(totalReward, currentTurn);
        modelInterface.recordReward(totalReward);

        // 6. Report performance
        //if (reporter != null) {
        //    float avgReward = calculateAverageReward(); // You'd implement this
        //    reporter.recordTurn(this, avgReward);
        //}

        // 7. Update previous data
        previousRewardData = after;
    }

    // Initialize reporter when game starts
    public void initGame(int totalVertices) {
        if (reporter != null) {
            reporter.initGame(totalVertices);
        }
    }
}
