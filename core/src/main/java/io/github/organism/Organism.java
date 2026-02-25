package io.github.organism;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import io.github.organism.map.GridPosition;
import io.github.organism.map.MapHex;
import io.github.organism.map.MapVertex;
import io.github.organism.map.TriangularGrid;
import io.github.organism.player.Player;

public class Organism {

    public float income;
    public float energy;
    public float expandRate;
    TriangularGrid territoryHex;
    TriangularGrid territoryVertex;
    HashMap<CandidateVertex, Float> candidateVertices;

    public int [] resources;
    Integer [] allyResources;
    GameBoard gameBoard;
    Player player;
    ArrayList<MapHex> extractQueue;
    float resourceUnitValue;
    float resourceSetValue;

    public Organism(GameBoard gb) {
        gameBoard = gb;
        territoryHex = new TriangularGrid(gameBoard);
        territoryVertex = new TriangularGrid(gameBoard);
        extractQueue = new ArrayList<>();
        resources = new int[3];
        allyResources = new Integer[3];
        energy = SettingsManager.DEFAULT_STARTING_ENERGY;
        candidateVertices = new HashMap<>();
        resourceSetValue = gameBoard.config.gameplaySettings.get("resource set value");
        resourceUnitValue = gameBoard.config.gameplaySettings.get("resource unit value");
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
        // Base income
        float baseIncome = 5f;
        
        // Count how many resource types this player leads in
        int leadershipBonuses = 0;
        for (int i = 0; i < 3; i++) {
            if (gameBoard.resourceLeaders[i] == player) {
                leadershipBonuses++;
            }
        }
        
        // Each leadership gives a bonus (you can adjust the bonus amount)
        float bonusPerLeadership = gameBoard.config.gameplaySettings.getOrDefault(
            "resource leadership bonus", 5f
        );
        
        income = baseIncome + (leadershipBonuses * bonusPerLeadership);
    }

    public void updateIncome() {
        // Base income
        float baseIncome = 5f;
        
        // Count how many resource types this player leads in
        int leadershipBonuses = 0;
        for (int i = 0; i < 3; i++) {
            if (gameBoard.resourceLeaders[i] == player) {
                leadershipBonuses++;
            }
        }
        
        // Each leadership gives a bonus (you can adjust the bonus amount)
        float bonusPerLeadership = gameBoard.config.gameplaySettings.getOrDefault(
            "resource leadership bonus", 5f
        );
        
        income = baseIncome + (leadershipBonuses * bonusPerLeadership);
    }

    public void extract(Vector2 planchetteFromCenter) {
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
                    energy += gameBoard.config.gameplaySettings.get("burn resource value");

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

        candidateVertices = new HashMap<>();
        double scoreSum = 0d;
        float baseP = 0.01f;

        // Tally up all the scores and index them
        for (GridPosition pos : territoryVertex) {
            MapVertex source = (MapVertex) pos.content;
            for (MapVertex v : source.adjacentVertices) {
                if (v.getPlayer() == null  && !v.masked) {
                    CandidateVertex cv = new CandidateVertex(source, v);
                    cv.calculatePlanchetteAgreement(planchetteFromCenter);

                    float p = cv.planchetteAgreement + baseP;
                    scoreSum += p;

                    if (!candidateVertices.containsKey(cv)) {
                        candidateVertices.put(cv, p);
                    }
                    else {
                        candidateVertices.put(cv, candidateVertices.get(cv) + p);
                    }
                }
            }
        }

        // budget depends on planchette magnitude
        float energyBudget = Math.min((income + energy)/4 * (1 + planchetteFromCenter.len()), energy);



        int verticesToClaim = (int) (energyBudget / gameBoard.config.gameplaySettings.get("energy to expand"));

        for (int v = 0; v < verticesToClaim; v++) {
            double r = gameBoard.rng.nextDouble() * scoreSum;
            double s = 0d;
            CandidateVertex remove = null;
            for (CandidateVertex cv : candidateVertices.keySet()) {
                s += candidateVertices.get(cv);
                if (s > r) {
                    claimVertex(cv.target);
                    remove = cv;
                    energy -= gameBoard.config.gameplaySettings.get("energy to expand");
                    break;
                }
            }
            if (remove != null) candidateVertices.remove(remove);
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


