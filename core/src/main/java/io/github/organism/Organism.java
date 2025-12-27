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

    public void updateIncome(){
        float newIncome = 0;
        //System.out.println(":::::::");
        int sets = Integer.MAX_VALUE;
        //System.out.println("count sets");
        for (int r : resources){
            if (r < sets){
                sets = r;
                //System.out.println(r + "\t" + sets);
            }
        }
        //System.out.println("-- UPDATE INCOME ---");
        for (int r : resources){

            if (r == sets){

                newIncome += resourceSetValue * r;
                //System.out.println("sets:\t" + r + "\t" + resourceSetValue);
            } else {
                newIncome += resourceUnitValue * (r - sets);
                //System.out.println("indv:\t" + r + "\t" + resourceUnitValue);
            }
        }
        //System.out.println("-----");
        //System.out.println("sets: " + sets);
        //System.out.println("newIncome: " + newIncome);

        income = Math.min(newIncome, SettingsManager.MAX_ENERGY - energy);
        //System.out.println("max energy:\t" + SettingsManager.MAX_ENERGY );
        //System.out.println("energy:\t" + energy);
        //System.out.println("income:\t" + income);
    }

    public void extract(Vector2 planchetteFromCenter) {
        /*
        get income based on resources

        permanently burn one resource unit to collect
         */

        //FIXME temporarily setting constant income
        income = 5;
        energy = Math.min(energy + income, SettingsManager.MAX_ENERGY);
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

        candidateVertices = new HashMap<>();
        double scoreSum = 0d;

        // Tally up all the scores and index them
        for (GridPosition pos : territoryVertex) {
            MapVertex source = (MapVertex) pos.content;
            for (MapVertex v : source.adjacentVertices) {
                if (v.getPlayer() == null  && !v.masked) {
                    CandidateVertex cv = new CandidateVertex(source, v);
                    cv.calculatePlanchetteAgreement(planchetteFromCenter);

                    if (!candidateVertices.containsKey(cv)) {
                        candidateVertices.put(cv, cv.planchetteAgreement);
                    }
                    else {
                        candidateVertices.put(cv, candidateVertices.get(cv) + cv.planchetteAgreement);
                    }
                }
            }
        }

        // budget depends on planchette magnitude
        float energyBudget = Math.min(income * (1 + planchetteFromCenter.len()), energy);
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

    private void burnVertex(MapVertex v) {

        for (MapHex hex : v.adjacentHexes) {
            if (hex.player == player) {
                releaseHex(hex);
            }
        }

        v.player = null;
        territoryVertex.removePos(v.pos);
    }

    private void releaseHex(MapHex h) {
        h.player = null;
        territoryHex.removePos(h.pos);
        extractQueue.remove(h);
        updateResources();
    }

    public void dispose() {
        territoryVertex = null;
        territoryHex = null;
    }
}


