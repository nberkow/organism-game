package io.github.organism;

import static java.util.Collections.sort;

import com.badlogic.gdx.graphics.Color;

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

    Integer [] ally_resources;
    public float energy;
    GameBoard game_board;
    Player player;

    ArrayList<MapHex> extract_queue;

    public Organism(GameBoard gb) {
        game_board = gb;
        territory_hex = new TriangularGrid(game_board);
        territoryVertex = new TriangularGrid(game_board);
        extract_queue = new ArrayList<>();
        resources = new Integer[3];
        ally_resources = new Integer[3];
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
        float resourceValue = game_board.config.gameplaySettings.get("resource value");

        for (int r=0; r<3; r++){
            int resourceCount = resources[r];
            Point ally_id = player.getAllyId();
            if (ally_id != null) {
                int allyResourceCount = game_board.players.get(ally_id).getOrganism().resources[r];
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
        while (h < extract_queue.size() && !done) {

            hex = extract_queue.get(h);
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
        /*
        - claim adjacent hexes
        - prioritize
         - fill gaps
         - direction of planchette
         - hexes with resources
        - claim until out of budget
         */

        float budget = energy / 2;

        ArrayList<MapVertex> externalVertexLayer = territoryVertex.getExternalVertexLayer(player);
        //ArrayList<ExpandSortWrapper> vertexPriority = new ArrayList<>();

        for (MapVertex v : externalVertexLayer) {
            v.calculateDirectionVectors(player);
            v.showVectors = true;
            v.showCircle = true;
            v.circleColor = Color.PURPLE;
        }

        /*
        for (ExpandSortWrapper w : vertexPriority) {

            float cost = w.remove_player_cost + game_board.config.gameplaySettings.get("claim vertex cost");
            if (cost <= budget) {
                claim_vertex(w.vertex);
                budget -= cost;
                energy -= cost;
            }
        }*/
    }

    public void computeAdjacentHexValue(ExpandSortWrapper w) {
        w.total_adjacent_hex_value = 0;
        w.adjacent_hex_completeness = 0;

        int [] resource_priority = player.getOrganism().get_resource_priority();
        for (MapHex hex : w.vertex.adjacentHexes) {
            if (hex.player != player) {

                for (int i = 0; i < hex.totalResources; i++) {
                    w.total_adjacent_hex_value += resource_priority[hex.resources[i]];
                }

                int c = 0;
                for (MapVertex n : hex.vertexList) {
                    if (n.player == player) {
                        c += 1;
                    }
                }
                if (c > w.adjacent_hex_completeness) {
                    w.adjacent_hex_completeness = c;
                }
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
            extract_queue.add(h);

            for (MapVertex v : h.vertexList) {
                if (v.player != player) {
                    claim_vertex(v);
                }
            }
        }
    }

    public void claim_hex(int i, int j, int k){
        MapHex h = (MapHex) game_board.universeMap.hexGrid.getPos(i, j, k).content;
        claim_hex(h);
    }

    public void claim_vertex(MapVertex v){

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


