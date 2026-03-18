package io.github.organism;
import static io.github.organism.SettingsManager.MAX_ENERGY;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.organism.hud.DebugLogger;
import io.github.organism.map.GridPosition;
import io.github.organism.map.MapHex;
import io.github.organism.map.MapVertex;
import io.github.organism.map.TriangularGrid;
import io.github.organism.player.Player;

public class Organism {

    // BASE_INCOME_PERCENT is now in SettingsManager
    public float income;
    public float energy;
    TriangularGrid territoryHex;
    TriangularGrid territoryVertex;
    HashMap<CandidateVertex, Float> candidateVertices;
    // Separate list for rendering - persists to show animations
    public ArrayList<CandidateVertex> candidatesForRendering;

    public int [] resources;
    Integer [] allyResources;
    GameBoard gameBoard;
    Player player;
    ArrayList<MapHex> extractQueue;

    public Organism(GameBoard gb) {
        gameBoard = gb;
        territoryHex = new TriangularGrid(gameBoard);
        territoryVertex = new TriangularGrid(gameBoard);
        extractQueue = new ArrayList<>();
        resources = new int[3];
        allyResources = new Integer[3];
        // Starting energy is a percentage of max energy
        energy = (SettingsManager.DEFAULT_STARTING_ENERGY / 100f) * SettingsManager.MAX_ENERGY;
        candidateVertices = new HashMap<>();
        candidatesForRendering = new ArrayList<>();
    }

    public void updateResources(){
        resources = new int [] {0, 0, 0};

        for (GridPosition pos : territoryHex){
            MapHex h = (MapHex) pos.content;
            for (int i = 0; i < h.filledResourceSlots; i++) {
                if (h.resources[i] != null){
                    resources[h.resources[i]]++;
                }
            }
        }
    }

    public void updateIncome() {
        // Base income from settings (as percentage of max energy)
        float baseIncome = (SettingsManager.BASE_INCOME_PERCENT / 100f) * SettingsManager.MAX_ENERGY;

        // Count how many resource types this player leads in
        int leadershipBonuses = 0;
        for (int i = 0; i < 3; i++) {
            if (gameBoard.resourceLeaders[i] == player) {
                leadershipBonuses++;
            }
        }

        // Each leadership doubles the income (exponential bonus)
        income = (float) (baseIncome * Math.pow(2, leadershipBonuses));
    }

    public void extract() {
        /*
        get income based on resources

        permanently burn one resource unit to collect
         */

        // Update resource counts and leadership before calculating income
        updateResources();
        gameBoard.updateResourceLeadership();
        updateIncome();

        // Add energy based on calculated income
        energy = Math.min(energy + income, SettingsManager.MAX_ENERGY);

        // Burn one resource after extracting (as per game rules)
        if (countResources() > 0) {
            burnResources();
        }
    }

    private int countResources() {
        int r = 0;
        int i = 0;
        for (int c : resources) {
            r += c;
            i ++;
        }
        return r;
    }

    public int getMostAbundantResource(){
        // get the most abundant resource
        int targetRes = 0;
        for (int i = 0; i < resources.length; i++) {

            if (resources[i] > resources[targetRes]) {
                targetRes = i;
            }
        }
        return targetRes;
    }

    public void burnResources() {
        // find the hex to update
        int targetRes = getMostAbundantResource();
        updateResources();

        int h = 0;
        boolean done = false;
        MapHex hex;
        ArrayList<MapHex> removeFromQueue = new ArrayList<>();

        while (h < extractQueue.size() && !done) {

            hex = extractQueue.get(h);
            int j = hex.filledResourceSlots - 1;

            if (hex.filledResourceSlots == 0) {
                removeFromQueue.add(hex);
            }

            while (j >= 0 && !done) {
                if (hex.resources[j] == targetRes && hex.filledResourceSlots > 0) {
                    done = true;
                    hex.resources[j] = 0;
                    hex.filledResourceSlots--;
                    // Burning resources is just a cost of staying in the game, not an income source

                    // shift remaining resources up
                    for (int p = j; p < hex.filledResourceSlots - 1; p++) {
                        hex.resources[p] = hex.resources[p + 1];
                        hex.resources[p + 1] = 0;
                    }
                }
                j--;
            }
            h++;
        }

        for (MapHex hx : removeFromQueue){
            extractQueue.remove(hx);
        }
    }

    public void expand(Vector2 planchetteFromCenter) {

        // Update resources and income before expanding
        updateResources();
        gameBoard.updateResourceLeadership();
        updateIncome();

        // Calculate centroid FIRST (needed for vector calculations)
        Vector2 centroid = calculateCentroid();

        // Rebuild candidate list - check for new candidates and remove invalid ones
        // Keep existing CandidateVertex objects to preserve animation state
        HashMap<MapVertex, CandidateVertex> existingCandidates = new HashMap<>();
        for (CandidateVertex cv : candidateVertices.keySet()) {
            existingCandidates.put(cv.target, cv);
        }

        candidateVertices.clear();

        for (GridPosition pos : territoryVertex) {
            MapVertex source = (MapVertex) pos.content;
            for (MapVertex v : source.adjacentVertices) {
                if (v.getPlayer() == null && !v.masked) {
                    // Reuse existing candidate if available (preserves animation state)
                    CandidateVertex cv = existingCandidates.get(v);
                    if (cv == null) {
                        cv = new CandidateVertex(source, v);
                        cv.gameBoard = gameBoard;
                    }
                    // Update vector from current centroid
                    cv.updateVectorFromCentroid(centroid);
                    candidateVertices.put(cv, 0f);
                }
            }
        }

        // Calculate planchette agreement scores for vertex selection
        // Normalize the planchette direction for agreement calculation
        Vector2 planchetteDirection = planchetteFromCenter.cpy();
        if (planchetteDirection.len() > 0.001f) {
            planchetteDirection.nor();
        }

        float baseP = 0.01f;
        int candidateCount = 0;
        float maxAgreement = Float.MIN_VALUE;
        float minAgreement = Float.MAX_VALUE;

        for (CandidateVertex cv : candidateVertices.keySet()) {
            cv.calculatePlanchetteAgreement(planchetteDirection);
            float p = cv.planchetteAgreement + baseP;
            candidateVertices.put(cv, p);

            candidateCount++;
            maxAgreement = Math.max(maxAgreement, cv.planchetteAgreement);
            minAgreement = Math.min(minAgreement, cv.planchetteAgreement);
        }

        // Debug output for first few turns
        if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration <= 2) {
            DebugLogger logger = DebugLogger.getInstance();
            logger.logf("Turn %d Player %s expand: %d candidates, agreement range [%.3f, %.3f], " +
                "planchette: (%.2f, %.2f), magnitude: %.2f, energy: %.1f",
                gameBoard.game.arcadeLoop.currentIteration, player.getPlayerName(),
                candidateCount, minAgreement, maxAgreement,
                planchetteDirection.x, planchetteDirection.y,
                planchetteFromCenter.len(), energy);

            // Show top 3 candidates by agreement
            int shown = 0;
            for (CandidateVertex cv : candidateVertices.keySet()) {
                if (shown < 3) {
                    logger.logf("    Candidate %d: agreement=%.3f, probability=%.3f, pos=(%.1f,%.1f)",
                        shown, cv.planchetteAgreement, candidateVertices.get(cv),
                        cv.target.x, cv.target.y);
                    shown++;
                }
            }
        }

        // Store the planchette direction in each candidate for rendering
        for (CandidateVertex cv : candidateVertices.keySet()) {
            cv.planchetteFromCenter = planchetteDirection;
        }

        // Copy candidates to rendering list BEFORE claiming any
        // This preserves them for animation even after they're claimed
        candidatesForRendering.clear();
        candidatesForRendering.addAll(candidateVertices.keySet());

        // Check if we have energy and candidates
        // Use vertex energy cost from settings
        float expandCost = SettingsManager.VERTEX_ENERGY_COST;

        if (candidateVertices.isEmpty() || energy < expandCost) {
            return;
        }

        // Budget depends on planchette magnitude (how far from center)
        float planchetteMagnitude = planchetteFromCenter.len();
        float energyBudget = Math.min(energy, energy * (0.5f + planchetteMagnitude * 0.5f));
        int verticesToClaim = (int) (energyBudget / expandCost);


        int attemptedClaims = 0;
        int maxAttempts = verticesToClaim * 3; // Allow retries for invalid vertices

        while (attemptedClaims < maxAttempts && verticesToClaim > 0 && !candidateVertices.isEmpty()) {
            // Recalculate score sum from current candidates
            double scoreSum = 0d;
            for (float score : candidateVertices.values()) {
                scoreSum += score;
            }

            if (scoreSum <= 0) {
                break;
            }


            double r = gameBoard.rng.nextDouble() * scoreSum;
            double s = 0d;
            CandidateVertex selected = null;

            for (CandidateVertex cv : candidateVertices.keySet()) {
                s += candidateVertices.get(cv);
                if (s > r) {
                    selected = cv;
                    break;
                }
            }

            if (selected != null) {
                // Check if vertex is still available at claim time
                if (selected.target.getPlayer() == null && !selected.target.masked) {
                    claimVertex(selected.target);
                    energy -= expandCost;
                    verticesToClaim--;

                    // Debug output for first few turns
                    if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration <= 2) {
                        DebugLogger.getInstance().logf("  -> Claimed vertex at (%.1f, %.1f), agreement: %.3f",
                            selected.target.x, selected.target.y, selected.planchetteAgreement);
                    }
                } else {
                    // Debug: vertex was already claimed
                    if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration <= 2) {
                        DebugLogger.getInstance().log("  -> Skipped vertex (already claimed or masked)");
                    }
                }
                // Remove this candidate (claimed or invalid)
                candidateVertices.remove(selected);
            }

            attemptedClaims++;

        }
    }

    public void claimHex(MapHex h){

        // remove previous player and add self

        if (h.player != player) {
            if (h.player != null) {
                h.player.getOrganism().territoryHex.removePos(h.pos);
            }
            territoryHex.addPos(h.pos);
            h.player = player;
            extractQueue.add(h);

            for (MapVertex v : h.vertexList) {
                if (v.player != player) {
                    claimVertex(v);
                }
            }
        }
        updateResources();
    }

    public void claimHex(int i, int j, int k){
        MapHex h = (MapHex) gameBoard.universeMap.hexGrid.getPos(i, j, k).content;
        claimHex(h);
    }

    public void claimVertex(MapVertex v){

        // Double-check vertex is available before claiming
        if (v.player != null) {
            return; // Already claimed by someone
        }

        if (territoryVertex.contains_position(v.pos.i, v.pos.j, v.pos.k)) {
            return; // Already in our territory
        }

        v.player = player;
        territoryVertex.addPos(v.pos);

        // check if claiming this vertex completed a hex
        boolean completes_hex;
        for (MapHex hex : v.adjacentHexes){
            completes_hex = true;
            for (MapVertex x : hex.vertexList){
                if (x.player != player) {
                    completes_hex = false;
                    break;
                }
            }

            if (completes_hex) {
                claimHex(hex);
            }
        }
    }

    /**
     * Calculate the centroid (average position) of all owned vertices.
     * This represents the center of mass of the organism's territory.
     */
    private Vector2 calculateCentroid() {
        if (territoryVertex.isEmpty()) {
            return new Vector2(0, 0);
        }

        float sumX = 0;
        float sumY = 0;
        int count = 0;

        for (GridPosition pos : territoryVertex) {
            MapVertex v = (MapVertex) pos.content;
            sumX += v.x;
            sumY += v.y;
            count++;
        }

        return new Vector2(sumX / count, sumY / count);
    }

    public void dispose() {
        territoryVertex = null;
        territoryHex = null;
    }

    public Player getPlayer() {
        return player;
    }

    public TriangularGrid getTerritoryVertex() {
        return territoryVertex;
    }

    public TriangularGrid getTerritoryHex() {
        return territoryHex;
    }
}


