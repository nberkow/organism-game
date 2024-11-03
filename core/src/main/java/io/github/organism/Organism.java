package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Organism {
    final int ENERGY_TO_CLAIM_NEUTRAL_VERTEX = 1;
    final int ENERGY_TO_CLAIM_OPPENENT_VERTEX = 1;
    final int ENERGY_TO_BREAK_HEX = 3;
    public int income;
    TriangularGrid territory_hex;
    TriangularGrid territory_vertex;
    Integer [] resources;
    int energy;
    GameBoard game_board;
    Player player;
    Color color;

    public Organism(GameBoard gb) {
        game_board = gb;
        territory_hex = new TriangularGrid(game_board);
        territory_vertex = new TriangularGrid(game_board);
        resources = new Integer[3];
        energy = 0; //game_board.DEFAULT_STARTING_ENERGY;
        income = 1;
    }

    public void update_resources(){
        resources = new Integer []   {0, 0, 0};

        for (GridPosition pos : territory_hex){
            MapHex h = (MapHex) pos.content;
            for (int i = 0; i < h.resources.length; i++) {
                if (h.resources[i] != null){
                    resources[h.resources[i]]++;
                }
            }
        }
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] == 0){
                resources[i] = 1;
            }
        }
    }
    public void update_income(){
        income = 1;
        for (int r=0; r<3; r++){
            income *= Math.min(resources[r], 6);
        }
    }

    public void extract() {

    }

    public void expand() {

        /*
        - get all the external vertexes
        - get all the hexes they are in
        - sort the hexes by value and claim as many as possible
        - sort the unused vertexes by value and claim as many as possible
         */


        energy = 5;
        Set<MapHex> candidate_hex_set = new HashSet<>();
        Set<MapVertex>  = new HashSet<>();

        for (GridPosition pos : territory_hex){
            MapHex territory_hex = (MapHex) pos.content;
            for (int v=0;v<6;v+=2){
                for (MapHex target_hex : territory_hex.vertex_list[v].adjacent_hexes){
                    if (target_hex != territory_hex){
                        candidate_hex_set.add(target_hex);
                    }
                }
                for (MapVertex target_vertex : territory_hex.vertex_list[v].adjacent_vertices){
                    if (target_vertex.player != player){
                        candidate_vertex_set.add(target_vertex);
                    }
                }
            }
        }


        ArrayList<ExpandSortWrapper> candidate_hex_list = new ArrayList<>();
        for (MapHex h : candidate_hex_set){
            ExpandSortWrapper w = new ExpandSortWrapper(h, player);
            w.compute_expand_value();
            candidate_hex_list.add(w);
        }

        candidate_hex_list.sort(Collections.reverseOrder());
        int budget = energy / 2;
        int cost_to_capture;
        ArrayList<ExpandSortWrapper> over_budget = new ArrayList<>();
        ArrayList<ExpandSortWrapper> zero_resource = new ArrayList<>();


        for (ExpandSortWrapper w : candidate_hex_list){
            System.out.println(w);
            if (w.resource_value == 0){
                zero_resource.add(w);
            } else {
                cost_to_capture = 0;
                if (w.hex.player != null && w.hex.player != player){
                    cost_to_capture += ENERGY_TO_BREAK_HEX;
                }
                for (MapVertex v : w.hex.vertex_list){
                    if (v != null) {
                        cost_to_capture += ENERGY_TO_CLAIM_NEUTRAL_VERTEX;
                        if (v.player != null && v.player != player) {
                            cost_to_capture += ENERGY_TO_CLAIM_OPPENENT_VERTEX;
                        }
                    }
                }
                if (cost_to_capture <= budget) {
                    claim_hex(w.hex);
                    budget -= cost_to_capture;
                    energy -= cost_to_capture;
                }
                else {
                    over_budget.add(w);
                }
            }

            if (budget > 0) {
                for (ExpandSortWrapper ovb : over_budget) {
                    int hex_cost = 0;
                    if (ovb.hex.player != null && ovb.hex.player != player) {
                        hex_cost = ENERGY_TO_BREAK_HEX;
                    }
                    for (MapVertex v : ovb.hex.vertex_list){
                        int cost = ENERGY_TO_CLAIM_NEUTRAL_VERTEX + hex_cost;
                        if (v.player != null && v.player != player) {
                            cost += ENERGY_TO_CLAIM_OPPENENT_VERTEX;
                        }
                        if (cost <= budget) {
                            claim_vertex(v);
                            budget -= cost;
                            energy -= cost;
                        }
                    }
                }
            }

            if (budget > 0) {
                for (ExpandSortWrapper z : zero_resource) {
                    int hex_cost = 0;
                    if (z.hex.player != null && z.hex.player != player) {
                        hex_cost = ENERGY_TO_BREAK_HEX;
                    }
                    for (MapVertex v : z.hex.vertex_list){
                        int cost = ENERGY_TO_CLAIM_NEUTRAL_VERTEX + hex_cost;
                        if (v.player != null && v.player != player) {
                            cost += ENERGY_TO_CLAIM_OPPENENT_VERTEX;
                        }
                        if (cost <= budget) {
                            claim_vertex(v);
                            budget -= cost;
                            energy -= cost;
                        }
                    }
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
            priority_by_resource_type[i] = Math.min(6, resources[(i+1)%3]) * Math.min(6, resources[(i+2)%3]);
        }
        return priority_by_resource_type;
    }

    public void explore () {


    }


    public void claim_hex(MapHex h){
        territory_hex.add_pos(h.pos);
        h.player = player;
        for (MapVertex v : h.vertex_list){
            claim_vertex(v);
        }
    }

    public void claim_hex(int i, int j, int k){
        MapHex h = (MapHex) game_board.universe_map.hex_grid.get_pos(i, j, k).content;
        claim_hex(h);
    }

    public void claim_vertex(MapVertex v){

         if (v.player != null){
            v.player.get_organism().territory_vertex.remove_pos(v.pos);
        }
        v.player = player;
        territory_vertex.add_pos(v.pos);

        boolean claim_hex;
        for (MapHex hex : v.adjacent_hexes){
            claim_hex = true;
            for (MapVertex x : hex.vertex_list){
                if (x.player != player){
                    claim_hex = false;
                }
            }

            // break hex
            if (hex.player != null) {
                hex.player.get_organism().territory_hex.remove_pos(hex.pos);
                hex.player = null;
            }
            if (claim_hex) {
                hex.player = player;
                if (!territory_hex.contains_hex(hex.pos)) {
                    territory_hex.add_pos(hex.pos);
                }
            }
        }
    }

    public void make_move(Integer move) {
        if (move != null){
            if (move == 1) {
                expand();
            }
        }

    }
}


