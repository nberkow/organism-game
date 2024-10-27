package io.github.organism;

import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;

public class Hexel {

    Integer i, j, k;

    Float x, y;
    double resources;

    GameBoard game_board;
    HashMap<String, Double> assimilation_by_player = new HashMap<>();

    public Hexel(GameBoard gb){
        game_board = gb;
    }

    public Hexel(GameBoard gb, int i, int j, int k){
        game_board = gb;
        this.i = i;
        this.j = j;
        this.k = k;
        calc_xy();
    }

    public void calc_xy(){
        x = (float) (sqrt(3f) * ( k/2F + i));
        y = (3F/2F * k);
    }

    public String toString() {
        return String.format(Locale.US, "%d %d %d\t%f\t%f", i, j, k, x, y);
    }

    public void update_assimilation(String player_name, double delta){
        assimilation_by_player.merge(player_name, delta, Double::sum);
    }

    public Double calculate_spare_capacity(String player_name) {

        /*

        Find the capacity needed to fill hex to a given capacity

        Enemy players count as additional capacity (or negative assimilation)

        */

        double capacity = 1;

        for (String p : assimilation_by_player.keySet()) {
            if (p.equals(player_name)) {
                capacity -= assimilation_by_player.get(p);
            } else {
                capacity += assimilation_by_player.get(p);
            }
        }
        return capacity;
    }

    public double calculate_free_capacity() {
        /*
        find the capacity unclaimed by any player
        */

        double capacity = 1;

        for (String p : assimilation_by_player.keySet()) {
            capacity += assimilation_by_player.get(p);
        }
        return capacity;
    }

    public void increase_assimilation(String player_name, double energy) {

        /*
        Increase the assimilation level of the hex using the energy
        */

        // clear out any tiny claim remnants
        LinkedList<String> to_remove = new LinkedList<>();
        for (String p : assimilation_by_player.keySet()){
            if (assimilation_by_player.get(p) < game_board.MIN_DELTA){
                to_remove.add(p);
            }
        }
        for (String s : to_remove){
            assimilation_by_player.remove(s);
        }

        int s = assimilation_by_player.size();
        if (!assimilation_by_player.containsKey(player_name)){
            assimilation_by_player.put(player_name, 0d);
        }

        if (s > 1){
            double energy_per_opponent = energy / (assimilation_by_player.size() - 1);
            for (String p : assimilation_by_player.keySet()) {
                if (!p.equals(player_name)) {
                    double current_val = assimilation_by_player.get(p);
                    double delta = Math.min(energy_per_opponent, current_val);
                    assimilation_by_player.put(p,current_val - delta);
                    energy -= delta;
                }
            }
        }

        double delta = Math.min(energy, 1 - assimilation_by_player.get(player_name));
        assimilation_by_player.put(player_name, assimilation_by_player.get(player_name) + delta);
    }

    public double calculate_distance(Hexel m) {
        return Math.pow(Math.pow(i - m.i, 2) + Math.pow(j - m.j, 2) + Math.pow(k - m.k, 2), 0.5);
    }
}




