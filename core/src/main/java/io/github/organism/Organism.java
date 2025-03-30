package io.github.organism;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Comparator;

import io.github.organism.map.GridPosition;
import io.github.organism.map.MapHex;
import io.github.organism.map.MapVertex;
import io.github.organism.map.TriangularGrid;
import io.github.organism.player.Player;

public class Organism {

    public float income;
    public float energy;
    public float budget;
    public float spend;
    TriangularGrid territoryHex;
    TriangularGrid territoryVertex;

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
        energy = SettingsManager.DEFAULT_STARTING_ENERGY;

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

    public void updateBudget(FloatPair<Float> planchettePolar){
        Float r = gameBoard.config.gameplaySettings.get("income budget ratio");
        float b = budget +
            income * (1 - r) +
            (planchettePolar.a * r);
        budget = Math.min(b, energy);
    }

    public void updateIncome(){
        float newIncome = 0;
        float resourceSetValue = gameBoard.config.gameplaySettings.get("resource set value");
        float resourceUnitValue = gameBoard.config.gameplaySettings.get("resource unit value");

        int sets = Integer.MAX_VALUE;
        for (int r : resources){
            if (r < sets){
                sets = r;
            }
        }

        for (int r : resources){
            if (r == sets){
                newIncome += resourceSetValue * r;
            } else {
                newIncome += resourceUnitValue * (r - sets);
            }
        }

        income = Math.min(newIncome, SettingsManager.MAX_ENERGY - energy);
    }

    public void extract(FloatPair<Float> planchetteXY) {
        /*
        passively get income base on trios. at low energy burn assets
         */
        updateResources();
        energy = Math.min(energy + income, SettingsManager.MAX_ENERGY);
        int totalResources = countResources();

        if (spend > income) {
            while  (energy < SettingsManager.MAX_ENERGY / 6 & totalResources > 0) {
                burnResources();
                totalResources = countResources();
            }

            while  (energy < SettingsManager.MAX_ENERGY / 6 & territoryVertex.size() > 0) {
                burnVertexes(planchetteXY);
            }
        }
    }

    private void burnVertexes(FloatPair<Float> planchetteXY) {
        ArrayList<ExpandEdge> expandEdges = territoryVertex.calculateExpandEdges(player);
        for (ExpandEdge e : expandEdges){
            e.calculatePlanchetteAgreement(planchetteXY);
        }
        expandEdges.sort(Comparator.naturalOrder());
        burnVertex(expandEdges.get(0).source);
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


    public void expand(FloatPair<Float> planchettePolar) {

        updateResources();
        updateBudget(planchettePolar);

        FloatPair<Float> planchetteXY = Util.polarToXYFloat(planchettePolar);

        ArrayList<ExpandEdge> expandEdges = territoryVertex.calculateExpandEdges(player);
        gameBoard.expandEdges.put(player.getTournamentId(), expandEdges);

        // get the magnitude of the sum of the planchette vector and the edge
        for (ExpandEdge e : expandEdges){
            e.calculatePlanchetteAgreement(planchetteXY);
        }
        expandEdges.sort(Comparator.reverseOrder());
        int n = expandEdges.size() / 3;

        float planchetteAgreementSum = 0f;
        for (int i=0; i<n; i++) {
            planchetteAgreementSum += (float) expandEdges.get(i).getPlanchetteAgreement();
        }

        float sp = 0;
        for (int i=0; i<n; i++) {
            float m = (float) (budget * expandEdges.get(i).getPlanchetteAgreement() / planchetteAgreementSum);
            ExpandEdge e = expandEdges.get(i);
            e.percentProgress += Math.min(m, 1-e.percentProgress);
            if (e.percentProgress >= 1) {
                claimVertex(e.target);
            }
            sp += m;
        }
        spend = sp;
        energy -= sp;
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

        // remove the player currently claiming this vertex
        if (v.player != null){
            v.player.getOrganism().territoryVertex.removePos(v.pos);
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


