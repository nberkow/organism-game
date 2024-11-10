package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Organism {
    public int income;
    TriangularGrid territory_hex;
    TriangularGrid territory_vertex;
    Integer [] resources;
    int energy;
    GameBoard game_board;
    Player player;
    Color color;

    ArrayList<MapHex> extract_queue;

    public Organism(GameBoard gb) {
        game_board = gb;
        territory_hex = new TriangularGrid(game_board);
        territory_vertex = new TriangularGrid(game_board);
        extract_queue = new ArrayList<>();
        resources = new Integer[3];
        energy = game_board.DEFAULT_STARTING_ENERGY;
        income = 1;
    }

    public void update_resources(){
        resources = new Integer [] {0, 0, 0};

        for (GridPosition pos : territory_hex){
            MapHex h = (MapHex) pos.content;
            for (int i = 0; i < h.total_resources; i++) {
                if (h.resources[i] != null){
                    resources[h.resources[i]]++;
                }
            }
        }
    }

    public void update_income(){
        income = 1;
        for (int r=0; r<3; r++){
            income *= Math.min(Math.max(resources[r], 1), 6);
        }
    }

    public void extract() {
        /*
        permanently destroy a resource to gain income
         */
        int [] res_priority = get_resource_priority();
        HashMap<Integer, ArrayList<Integer>> res_by_priority = new HashMap<>();
        int i = 0;
        for (int r : res_priority){
            if (!res_by_priority.containsKey(r)){
                res_by_priority.put(r, new ArrayList<>());
            }
            res_by_priority.get(r).add(i);
            i++;
        }

        ArrayList<Integer> res_list = new ArrayList<>(res_by_priority.keySet());
        Collections.sort(res_list);

        int r = 0;
        int q;
        boolean done = false;
        int target_res;
        while (r < res_list.size() && !done){
            q = 0;
            ArrayList<Integer> tied_res = res_by_priority.get(res_list.get(r));
            while (q<tied_res.size() && !done){
                target_res = tied_res.get(q);
                int h = 0;
                MapHex hex;
                while (h < extract_queue.size() && !done) {
                    hex = extract_queue.get(h);
                    int j = hex.total_resources - 1;
                    while (j >= 0 && !done) {
                        if (hex.resources[j] == target_res && hex.total_resources > 0) {
                            done = true;
                            hex.resources[j] = 0;
                            hex.total_resources--;

                            // shift remaining resources up
                            for (int p = j; p < hex.total_resources - 1; p++) {
                                hex.resources[p] = hex.resources[p + 1];
                                hex.resources[p + 1] = 0;
                            }
                        }
                        j--;
                    }
                    h++;
                } q++;
            }
            r++;
        }
        energy += income;
    }


    public void expand() {

        /*
        - get all the external vertexes
        - get all the hexes they are in
        - sort the hexes by value and claim as many as possible
        - sort the unused vertexes by value and claim as many as possible
         */

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

    public void explore () {
        /*
        - find adjacent vertices
        - compute value based on surrounding hexes
        - prioritize
        -- high value hexes
        -- unexplored hexes
         */

        // spend up to half the player's energy per turn
        int budget = (int) Math.ceil(energy / 2d);
        System.out.println(budget);

        // get all neighboring vertexes
        ArrayList<MapVertex> neighboring_vertexes = territory_vertex.get_external_vertex_layer(player);
        if (neighboring_vertexes.isEmpty()) return;

        // Wrap the vertexes and compute their cost and value. Ignore anything out of budget
        ArrayList<ExploreSortWrapper> explore_vertex_priority = new ArrayList<>();
        for (MapVertex v : neighboring_vertexes){

            ExploreSortWrapper w = new ExploreSortWrapper(v, player);
            w.energy_cost = w.compute_cost(w.vertex);
            System.out.println(w.energy_cost);
            if (w.energy_cost <= budget) {
                w.compute_and_store_vertex_properties(w.vertex);
                explore_vertex_priority.add(w);
            }
        }

        boolean vertex_claimed = true; // starts true
        ArrayList<ExploreSortWrapper> to_remove;
        System.out.println(vertex_claimed + " " + explore_vertex_priority.size() + " " + budget);
        while (vertex_claimed && !explore_vertex_priority.isEmpty() && budget > 0) {
            vertex_claimed = false;

            // filter over-budget
            to_remove = new ArrayList<>();
            for (int i = 0; i<explore_vertex_priority.size(); i++){
                if (explore_vertex_priority.get(i).energy_cost > budget) {
                    to_remove.add(explore_vertex_priority.get(i));
                }
            }


            explore_vertex_priority.sort(Collections.reverseOrder());

            // dequeue and claim
            int to_try = Math.min(explore_vertex_priority.size(), 2);
            for (int i=0; i<to_try; i++) {
                ExploreSortWrapper w = explore_vertex_priority.remove(0);
                if (w.energy_cost <= budget) {
                    claim_vertex(w.vertex);
                    vertex_claimed = true;
                    budget -= w.energy_cost;
                    energy -= w.energy_cost;
                    to_remove.add(w);
                    for (MapVertex n : w.vertex.adjacent_vertices) {
                        if (n.player != player) {
                            ExploreSortWrapper x = new ExploreSortWrapper(n, player);
                            x.energy_cost = x.compute_cost(n);
                            x.compute_and_store_vertex_properties(n);
                            explore_vertex_priority.add(x);
                        }
                    }
                }
            }


            explore_vertex_priority.removeAll(to_remove);


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
                h.player.get_organism().territory_hex.remove_pos(h.pos);
            }
            territory_hex.add_pos(h.pos);
            h.player = player;
            extract_queue.add(h);

            for (MapVertex v : h.vertex_list) {
                if (v.player != player) {
                    claim_vertex(v);
                }
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
            if (move == 0) {
                extract();
            }
            if (move == 1) {
                expand();
            }
            if (move == 2) {
                explore();
            }
        }
        update_resources();
        update_income();
    }
}


