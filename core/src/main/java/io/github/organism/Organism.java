package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Organism {
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


        energy = 6;
        // claim the best neighboring hexes
        int budget = (int) Math.ceil(energy / 2d);
        ArrayList<MapHex> neighboring_hexes = territory_hex.get_external_hex_layer(player);
        ArrayList<ExpandSortWrapper> hex_by_value = new ArrayList<>();
        for (MapHex neighbor : neighboring_hexes) {

            ExpandSortWrapper w = new ExpandSortWrapper(neighbor, player);
            w.compute_value();
            w.compute_cost();
            hex_by_value.add(w);
        }
        hex_by_value.sort(Collections.reverseOrder());

        for (ExpandSortWrapper w : hex_by_value){
            if (w.energy_cost <= budget) {
                claim_hex((MapHex) w.map_element);
                energy -= w.energy_cost;
                budget -= w.energy_cost;
            }
        }

        // use remaining energy budget to claim vertexes in best unclaimed hexes
        ArrayList<MapVertex> neighboring_vertexes = territory_vertex.get_external_vertex_layer(player);
        ArrayList<ExpandSortWrapper> vertex_by_value = new ArrayList<>();
        for (MapVertex neighbor : neighboring_vertexes) {
            ExpandSortWrapper w = new ExpandSortWrapper(neighbor, player);
            w.compute_value();
            w.compute_cost();
            System.out.println(w.energy_cost);
            vertex_by_value.add(w);
        }
        vertex_by_value.sort(Collections.reverseOrder());
        for (ExpandSortWrapper w : vertex_by_value){
            if (w.energy_cost <= budget) {
                claim_vertex((MapVertex) w.map_element);
                energy -= w.energy_cost;
                budget -= w.energy_cost;
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

        // remove previous player and add self
        if (h.player != null) {
            h.player.get_organism().territory_hex.remove_pos(h.pos);
        }
        territory_hex.add_pos(h.pos);
        h.player = player;

        for (MapVertex v : h.vertex_list){
            if (v.player != player) {
                claim_vertex(v);
            }
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

        // check if claiming this vertex completed a hex
        boolean completes_hex;
        for (MapHex hex : v.adjacent_hexes){
            completes_hex = true;
            for (MapVertex x : hex.vertex_list){
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

    public void make_move(Integer move) {
        if (move != null){
            if (move == 1) {
                expand();
            }
        }
    }
}


