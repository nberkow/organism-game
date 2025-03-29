package io.github.organism;

import static java.util.Collections.sort;

import java.awt.Point;
import java.util.ArrayList;

import io.github.organism.map.GridPosition;
import io.github.organism.map.MapHex;
import io.github.organism.map.MapVertex;
import io.github.organism.map.TriangularGrid;
import io.github.organism.player.Player;

public class Organism {

    public final float MAX_ENERGY = 100f;
    public float income;
    TriangularGrid territory_hex;
    TriangularGrid territoryVertex;
    public Integer [] resources;

    Integer [] allyResources;
    public float energy;
    GameBoard gameBoard;
    Player player;

    ArrayList<MapHex> extractQueue;

    public Organism(GameBoard gb) {
        gameBoard = gb;
        territory_hex = new TriangularGrid(gameBoard);
        territoryVertex = new TriangularGrid(gameBoard);
        extractQueue = new ArrayList<>();
        resources = new Integer[3];
        allyResources = new Integer[3];
        energy = GameBoard.DEFAULT_STARTING_ENERGY;
        income = 1;

    }

    public void updateResources(){
        resources = new Integer [] {0, 0, 0};

        for (GridPosition pos : territory_hex){
            MapHex h = (MapHex) pos.content;
            for (int i = 0; i < h.totalResources; i++) {
                if (h.resources[i] != null){
                    resources[h.resources[i]]++;
                }
            }
        }
    }


    public void updateIncome(){
        float newIncome = 1;
        float resourceValue = gameBoard.config.gameplaySettings.get("resource value");

        for (int r=0; r<3; r++){
            int resourceCount = resources[r];
            Point ally_id = player.getAllyId();
            if (ally_id != null) {
                int allyResourceCount = gameBoard.players.get(ally_id).getOrganism().resources[r];
                resourceCount += allyResourceCount;
            }

            if (resourceCount > 0) {
                newIncome *= Math.min(resourceCount, 6f) * resourceValue;
            }
        }

        if (newIncome < resourceValue) {
            newIncome = resourceValue;
            if (player.getAllyId() != null) {
                newIncome = (float) Math.floor(1.5f * newIncome);
            }
        }
        income = Math.min(newIncome, MAX_ENERGY - energy);
    }

    public void extract() {
        /*
        permanently consume a resource to gain income
         */

        // get the most abundant resource
        int target_res = 0;
        for (int i = 1; i < resources.length; i++) {
            if (resources[i] > resources[target_res]) {
                target_res = i;
            }
        }

        // find the hex to update
        int h = 0;
        boolean done = false;
        MapHex hex;
        while (h < extractQueue.size() && !done) {

            hex = extractQueue.get(h);
            int j = hex.totalResources - 1;

            while (j >= 0 && !done) {

                if (hex.resources[j] == target_res && hex.totalResources > 0) {

                    done = true;
                    hex.resources[j] = 0;
                    hex.totalResources--;

                    // shift remaining resources up
                    for (int p = j; p < hex.totalResources - 1; p++) {
                        hex.resources[p] = hex.resources[p + 1];
                        hex.resources[p + 1] = 0;
                    }
                }
                j--;
            }
            h++;
        }
        energy = Math.min(energy + income, MAX_ENERGY);
    }


    public void expand(FloatPair<Float> planchettePolar) {

        float budget = energy / 2;
        FloatPair<Float> planchetteXY = Util.polarToXYFloat(planchettePolar);

        ArrayList<ExpandEdge> expandEdges = territoryVertex.calculateExpandEdges(player);
        gameBoard.expandEdges.put(player.getTournamentId(), expandEdges);

        // get the magnitude of the sum of the planchette vector and the edge
        ArrayList<Float> planchetteAgreement = new ArrayList<>();
        float planchetteAgreementSum = 0f;

        for (ExpandEdge e : expandEdges){
            float a = (float) Math.pow(e.getPlanchetteAgreement(planchetteXY), 2);
            planchetteAgreement.add(a);
            planchetteAgreementSum += a;
        }

        for (int i=0; i<expandEdges.size(); i++) {
            float m = budget * planchetteAgreement.get(i) / planchetteAgreementSum;
            ExpandEdge e = expandEdges.get(i);
            e.percentProgress += Math.min(m, 1-e.percentProgress);
            if (e.percentProgress >= 1) {
                System.out.println("ffff");
                claimVertex(e.target);
            }
        }
    }


    public int [] get_resource_priority(){

        /*
        For each resource type find the income gain based on the other two.
        return the result as an array indexed by resource type
         */

        int [] priority_by_resource_type = new int[3];
        for (int i=0; i<3; i++){
            priority_by_resource_type[i] = Math.max(0, 6 - resources[i]) * Math.min(6, resources[(i+1)%3] + 1) * Math.min(6, resources[(i+2)%3] + 1);
        }
        return priority_by_resource_type;
    }

    public void claim_hex(MapHex h){

        // remove previous player and add self

        if (h.player != player) {
            if (h.player != null) {
                h.player.getOrganism().territory_hex.remove_pos(h.pos);
            }
            territory_hex.addPos(h.pos);
            h.player = player;
            extractQueue.add(h);

            for (MapVertex v : h.vertexList) {
                if (v.player != player) {
                    claimVertex(v);
                }
            }
        }
    }

    public void claim_hex(int i, int j, int k){
        MapHex h = (MapHex) gameBoard.universeMap.hexGrid.getPos(i, j, k).content;
        claim_hex(h);
    }

    public void claimVertex(MapVertex v){

        if (v.player != null){
            v.player.getOrganism().territoryVertex.remove_pos(v.pos);
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
                claim_hex(hex);
            }
        }
    }

    public void dispose() {
        territoryVertex = null;
        territory_hex = null;
    }


}


