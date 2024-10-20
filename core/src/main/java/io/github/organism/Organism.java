package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Organism {

    HexSet assimilated_hexes;
    GameBoard game_board;

    String name = "player1";
    Color color = Color.PURPLE;

    double energy_store = 0.5;

    public Organism(GameBoard gb) {
        game_board = gb;
        assimilated_hexes = new HexSet();
    }

    public void extract() {
        /*
        Collect Energy from all assimilated hexes
        */

        for (Hexel h : assimilated_hexes.dump_hex_list()) {

            double delta = h.resources * game_board.ENERGY_PER_ACTION;
            double assimilation = h.assimilation_by_player.get(name);
            delta *= assimilation;
            delta = Math.min(delta, game_board.energy_bar.MARGIN - energy_store);

            if (delta <= game_board.MIN_DELTA){
                delta = 0;
            }

            h.resources -= delta;
            energy_store += delta;
        }
    }

    public void expand() { // Think about recursion
        /*
        Use energy to increase assimilation
        - assimilated hexes increase until 100%
        - when a hex reach 37%, the hexes around it are assimilated
        - if an assimilated or adjacent Hex is partially occupied by another player,
          energy decreases the enemy share before filling player's share
        */

        double assimilation_energy = energy_store * game_board.ENERGY_PER_ACTION;
        energy_store -= assimilation_energy;
        LinkedList<Hexel> hex_queue = assimilated_hexes.dump_hex_list();

        while (assimilation_energy > 0 && !hex_queue.isEmpty()){

            double energy_budget_per_hex = assimilation_energy / hex_queue.size();
            ArrayList<Hexel> to_add = new ArrayList<>();
            ArrayList<Hexel> to_remove = new ArrayList<>();

            for (Hexel h : hex_queue){

                double c = h.calculate_spare_capacity(name);
                double delta = Math.min(c, energy_budget_per_hex);
                h.increase_assimilation(name, delta);
                assimilation_energy -= delta;

                if (h.assimilation_by_player.get(name) >= game_board.ASSIMILATION_THRESHOLD){
                    ArrayList<Hexel> neighbors = game_board.universal_grid.get_surrounding_hexes(h);
                    for (Hexel n : neighbors){
                        if (n != null && !assimilated_hexes.contains_hex(n)){
                            to_add.add(n);
                        }
                    }
                }

                if (h.calculate_spare_capacity(name) == 0){
                    to_remove.add(h);
                }
            }

            for (Hexel h : to_remove){
                hex_queue.remove(h);
            }

            for (Hexel h : to_add){
                assimilated_hexes.add_hex(h);
                h.assimilation_by_player.put(name, 0d);
            }
        }
    }

    public void explore () {

        double energy_to_spend = energy_store;
        ArrayList<Hexel> hexes_to_claim = choose_explore_hexes(energy_to_spend);
        for (Hexel h : hexes_to_claim) {
            h.assimilation_by_player.put(name, 0d);
            assimilated_hexes.add_hex(h);
        }
        energy_store = 0;
    }

    public ArrayList<Hexel> choose_explore_hexes(Double energy_spend) {

        /*
        find a hex to colonize based
        - search hexes within some min and max radius
        - value depends on free capacity and resource level. (free = % not claimed by opponent)
        - all energy is spent to claim a hex. the more energy spent the better the hex
         */

        double min_radius = Math.pow(assimilated_hexes.size, 0.5d);
        double max_radius = min_radius * 2;
        HashMap<Hexel, Double> minimum_distances = new HashMap<>();

        // calculate all the relevant pairwise distances and pick the shortest
        LinkedList<Hexel> outer_perimeter = find_outer_hex_layer();
        for (Hexel m : game_board.universal_grid.map_grid) {
            for (Hexel h : outer_perimeter) {
                double dist = h.calculate_distance(m);
                if (!minimum_distances.containsKey(m)) {
                    minimum_distances.put(m, dist);
                } else {
                    Double current_val = minimum_distances.get(m);
                    if (dist < current_val) {
                        minimum_distances.put(m, dist);
                    }
                }
            }
        }

        //ArrayList<Hexel> hexes_in_range = new ArrayList<>();
        //<Double> scores = new ArrayList<>();

        List<Pair<Hexel, Double>> hexScorePairs = new ArrayList<>();

        for (Hexel h : minimum_distances.keySet()) {
            Double dist = minimum_distances.get(h);
            if (dist >= min_radius && dist <= max_radius) {
                double score = h.calculate_free_capacity() * h.resources;
                hexScorePairs.add(new Pair<Hexel, Double>(h, score));
            }
        }

        // Sort the pairs by the score in descending order
        hexScorePairs.sort(Comparator.comparing((Pair<Hexel, Double> pair) -> pair.getValue()).reversed());

        ArrayList<Hexel> chosen_hexes = new ArrayList<>();
        double threshold = energy_spend;
        for (Pair<Hexel, Double> p : hexScorePairs){
            if (p.getValue() < threshold) {
                chosen_hexes.add(p.getKey());
                threshold /= 2;
            }
        }

        return(chosen_hexes);
    }

    private LinkedList<Hexel> find_outer_hex_layer() {
        LinkedList<Hexel> outer_perimeter = new LinkedList<>();

        for (Hexel h : game_board.universal_grid.map_grid){

            for (Hexel neighbor : game_board.universal_grid.get_surrounding_hexes(h)){
                int i = 0;
                boolean is_internal = true;
                while (i < 6 && is_internal){
                    if (neighbor.assimilation_by_player.containsKey(name)) {
                        is_internal = false;
                    }
                    i+=1;
                }
                if (!is_internal) {
                    outer_perimeter.add(h);
                }
            }
        }
        return outer_perimeter;
    }

    public void create_assimilated_hex(int i, int j, int k){
        /*
        Choose a hex to instantly assimilate for one player

        This is for setup. potentially unsafe during play
         */

        Hexel target_hex = game_board.universal_grid.map_grid.get_hex(i, j, k);
        if (!target_hex.assimilation_by_player.containsKey(name)){
            target_hex.assimilation_by_player.put(name, 0.5);
        }
        assimilated_hexes.add_hex(target_hex);
    }


}


