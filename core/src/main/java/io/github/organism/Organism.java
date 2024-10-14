package io.github.organism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Organism {

    HexSet assimilated_hexes;
    HexSet adjacent_hexes;
    GameBoard game_board;

    String name = "player1";

    double energy_store;
    public final double ASSIMILATION_THRESHOLD = 0.37d;

    // percent of total energy transferred per action (transfers faster when more full);
    public final double ENERGY_PER_ACTION = .05d;

    public final double MIN_DELTA = 10e-5f;


    public Organism(GameBoard gb) {
        game_board = gb;
        assimilated_hexes = new HexSet();
        adjacent_hexes = new HexSet();
    }

    public void extract() {
        /*
        Collect Energy from all assimilated hexes
        */

        for (Hexel h : assimilated_hexes.dump_hex_list()) {

            double delta = h.resources * ENERGY_PER_ACTION;
            double assimilation = h.assimilation_by_player.get(this.name);
            delta *= assimilation;

            if (delta <= MIN_DELTA){
                delta = 0;
            }

            h.resources -= delta;
            energy_store += delta;
        }
    }

    public void expand(int n) {
        /*
        Use energy to increase assimilation
        - assimilated hexes increase until 100%
        - adjacent hexes increase until ASSIMILATION_THRESHOLD, then become assimilated
        - undiscovered hexes next to assimilated hexes become adjacent
         */

    }

    public void explore(int n) {
        /*
        Use energy to assimilate distant hexes
        - BFS one layer per cycle
        - sort hexes by resources
        - top hex becomes assimilated, starting with assimilation = ASSIMILATION_THRESHOLD
         */
    }

    public void assimilate_hex(int i, int j, int k){
        Hexel target_hex = game_board.grid.hex_grid.get(i).get(j).get(k);
        if (!target_hex.assimilation_by_player.containsKey(this.name)){
            target_hex.assimilation_by_player.put(this.name, ASSIMILATION_THRESHOLD);
        }

        assimilated_hexes.add_hex(target_hex);
        if (adjacent_hexes.contains_hex(target_hex)) {
            adjacent_hexes.remove_hex(target_hex);
        }
    }

    public void add_adjacent_hex(int i, int j, int k){
        Hexel target_hex = game_board.grid.hex_grid.get(i).get(j).get(k);
        adjacent_hexes.add_hex(target_hex);
    }

}


