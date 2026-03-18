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
        
        // Debug: Log organism creation with current settings - ALWAYS log this
        DebugLogger logger = DebugLogger.getInstance();
        logger.log(">>> Organism constructor called <<<");
        logger.log("  SettingsManager.MAX_ENERGY: " + SettingsManager.MAX_ENERGY);
        logger.log("  SettingsManager.DEFAULT_STARTING_ENERGY: " + SettingsManager.DEFAULT_STARTING_ENERGY + "%");
        logger.log("  Calculation: (" + SettingsManager.DEFAULT_STARTING_ENERGY + " / 100) * " + SettingsManager.MAX_ENERGY);
        logger.log("  Result - organism.energy: " + energy);
        if (gameBoard != null && gameBoard.game != null) {
            logger.log("  GameBoard exists: yes");
        } else {
            logger.log("  GameBoard exists: no");
        }
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

        // income can't be higher than the missing part of the energy bar
        float energyHeadspace = MAX_ENERGY - energy;

        // Each leadership doubles the income (exponential bonus)
        income = (float) (Math.min(energyHeadspace, (baseIncome * Math.pow(2, leadershipBonuses))));

        // Debug output - always show for first 5 turns to verify settings
        if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration <= 5) {
            DebugLogger logger = DebugLogger.getInstance();
            logger.log("Turn " + gameBoard.game.arcadeLoop.currentIteration + " - " + player.getPlayerName() + ":");
            logger.log("  BASE_INCOME_PERCENT: " + SettingsManager.BASE_INCOME_PERCENT + "%");
            logger.log("  MAX_ENERGY: " + SettingsManager.MAX_ENERGY);
            logger.log("  baseIncome: " + baseIncome);
            logger.log("  leadership bonuses: " + leadershipBonuses);
            logger.log("  final income: " + income);
            logger.log("  current energy: " + energy);
        }
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
        // Always cap at current MAX_ENERGY setting
        energy = Math.min(energy + income, SettingsManager.MAX_ENERGY);

        // Also ensure energy doesn't exceed max if max was lowered
        energy = Math.min(energy, SettingsManager.MAX_ENERGY);

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

        // Ensure energy doesn't exceed current max (in case max was lowered)
        energy = Math.min(energy, SettingsManager.MAX_ENERGY);

        // Calculate centroid FIRST (needed for vector calculations)
        Vector2 centroid = calculateCentroid();

        // Rebuild candidate list - check for new candidates and remove invalid ones
        // Keep existing CandidateVertex objects to preserve animation state
        HashMap<MapVertex, CandidateVertex> existingCandidates = new HashMap<>();
        for (CandidateVertex cv : candidateVertices.keySet()) {
            existingCandidates.put(cv.target, cv);
        }

        candidateVertices.clear();

        int totalAdjacent = 0;
        int filteredMasked = 0;
        int filteredClaimed = 0;

        for (GridPosition pos : territoryVertex) {
            MapVertex source = (MapVertex) pos.content;
            for (MapVertex v : source.adjacentVertices) {
                totalAdjacent++;
                if (v.getPlayer() != null) {
                    filteredClaimed++;
                } else if (v.masked) {
                    filteredMasked++;
                } else {
                    // Additional check: ensure vertex is truly adjacent (shares a non-masked hex with source)
                    // This prevents claiming across gaps
                    boolean sharesValidHex = false;
                    for (io.github.organism.map.MapHex sourceHex : source.adjacentHexes) {
                        if (!sourceHex.masked) {
                            for (io.github.organism.map.MapHex targetHex : v.adjacentHexes) {
                                if (sourceHex == targetHex && !targetHex.masked) {
                                    sharesValidHex = true;
                                    break;
                                }
                            }
                        }
                        if (sharesValidHex) break;
                    }

                    if (!sharesValidHex) {
                        filteredMasked++;
                    } else {
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
        }

        // Reduced verbosity - only log on turn 1
        if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration == 1) {
            DebugLogger logger = DebugLogger.getInstance();
            logger.log("  " + player.getPlayerName() + " - Candidate filtering: valid=" + candidateVertices.size() + 
                      ", filtered_claimed=" + filteredClaimed + ", filtered_masked=" + filteredMasked);
        }

        // Calculate planchette agreement scores for vertex selection
        // Normalize the planchette direction for agreement calculation
        Vector2 planchetteDirection = planchetteFromCenter.cpy();
        float planchetteMagnitude = planchetteDirection.len();

        // Define neutral zone: within 5% of available radius, all vertices are equal
        float neutralZoneThreshold = 0.05f;
        boolean inNeutralZone = (planchetteMagnitude < neutralZoneThreshold);

        if (planchetteMagnitude > 0.001f) {
            planchetteDirection.nor();
        }

        int candidateCount = 0;
        float maxAgreement = Float.MIN_VALUE;
        float minAgreement = Float.MAX_VALUE;

        for (CandidateVertex cv : candidateVertices.keySet()) {
            if (inNeutralZone) {
                // In neutral zone: set agreement to neutral value for all
                cv.planchetteAgreement = 0.5f;
            } else {
                cv.calculatePlanchetteAgreement(planchetteDirection);
            }
            candidateCount++;
            maxAgreement = Math.max(maxAgreement, cv.planchetteAgreement);
            minAgreement = Math.min(minAgreement, cv.planchetteAgreement);
        }

        // Removed verbose neutral zone logging

        // Transform agreements to positive probabilities
        // Agreement ranges from -1 (opposite direction) to +1 (same direction)
        // Negative agreements should have near-zero probability
        for (CandidateVertex cv : candidateVertices.keySet()) {
            float agreement = cv.planchetteAgreement;
            float probability;

            if (inNeutralZone) {
                // In neutral zone: all vertices get equal probability
                probability = 1.0f;
            } else if (agreement <= 0) {
                // Opposite or perpendicular to planchette: very low probability
                // Use small exponential: e^(agreement) for agreement in [-1, 0]
                // This gives range from ~0.37 (at -1) to 1.0 (at 0)
                probability = (float) Math.exp(agreement) * 0.01f; // Scale down to 0.0037 to 0.01
            } else {
                // Aligned with planchette: exponentially increasing probability
                // e^(agreement * 3) for agreement in [0, 1]
                // This gives range from 1.0 (at 0) to ~20 (at 1)
                probability = (float) Math.exp(agreement * 3.0);
            }

            candidateVertices.put(cv, probability);
        }

        // Suppress detailed candidate debug output - too verbose

        // Store the planchette direction and neutral zone status in each candidate for rendering
        for (CandidateVertex cv : candidateVertices.keySet()) {
            cv.planchetteFromCenter = planchetteDirection;
            cv.inNeutralZone = inNeutralZone;
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

        // Budget calculation: base budget is 50% of (income + energy)
        // Planchette magnitude provides a modest multiplier (1.0x to 1.25x)
        planchetteMagnitude = planchetteFromCenter.len();
        float aggressionMultiplier = 1.0f + (planchetteMagnitude * 0.25f); // 1.0 to 1.25
        float baseBudget = 0.5f * (income + energy);
        float energyBudget = Math.min(energy, baseBudget * aggressionMultiplier);
        int verticesToClaim = (int) (energyBudget / expandCost);

        int attemptedClaims = 0;
        int maxAttempts = verticesToClaim * 3; // Allow retries for invalid vertices
        int successfulClaims = 0;
        int skippedMasked = 0;
        int skippedClaimed = 0;

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
                boolean isMasked = selected.target.masked;
                boolean isClaimed = (selected.target.getPlayer() != null);

                // Check if vertex is still available at claim time
                if (!isMasked && !isClaimed) {
                    claimVertex(selected.target);
                    energy -= expandCost;
                    verticesToClaim--;
                    successfulClaims++;
                } else {
                    if (isMasked) skippedMasked++;
                    if (isClaimed) skippedClaimed++;
                }

                // Remove this candidate (claimed or invalid)
                candidateVertices.remove(selected);
            }

            attemptedClaims++;
        }

        // Only log expansion summary on turn 1
        if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration == 1) {
            DebugLogger logger = DebugLogger.getInstance();
            logger.log("  " + player.getPlayerName() + " expand: claimed=" + successfulClaims + 
                      ", energy_spent=" + (successfulClaims * expandCost));
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

    /**
     * Apply current settings to this organism.
     * Called when settings are changed mid-game to ensure organisms respect new values.
     */
    public void applyCurrentSettings() {
        DebugLogger logger = DebugLogger.getInstance();
        
        float oldEnergy = energy;
        float oldIncome = income;
        
        // Cap energy at new maximum if it was lowered
        energy = Math.min(energy, SettingsManager.MAX_ENERGY);

        // Recalculate income based on current settings
        updateIncome();

        logger.log("  Applied settings to " + (player != null ? player.getPlayerName() : "unknown") + 
                  ": energy " + oldEnergy + " -> " + energy + 
                  " (max=" + SettingsManager.MAX_ENERGY + "), income " + oldIncome + " -> " + income);
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


