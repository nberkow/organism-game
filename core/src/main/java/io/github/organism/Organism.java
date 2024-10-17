package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Organism {

    HexSet assimilated_hexes;
    GameBoard game_board;

    String name = "player1";
    Color color = Color.PURPLE;

    double energy_store;

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
            double assimilation = h.assimilation_by_player.get(this.name);
            delta *= assimilation;

            if (delta <= game_board.MIN_DELTA){
                delta = 0;
            }

            h.resources -= delta;
            energy_store += delta;
        }
    }

    public void expand() {
        /*
        Use energy to increase assimilation
        - assimilated hexes increase until 100%
        - if an assimilated or adjacent Hex is partially occupied by another player,
        -- energy decreases the enemy share before filling player's share

        After energy is distributed
        - adjacent hexes increase until ASSIMILATION_THRESHOLD, then become assimilated
        - undiscovered hexes next to assimilated hexes become adjacent
         */

        //FIXME - need to keep adding adjacent each round

        double assimilation_energy = energy_store * game_board.ENERGY_PER_ACTION;
        double total_unassimilated = 0.0d;
        int total_count = 0;
        double per_hex;

        // get all currently assimilated hexes.
        // - a hex is assimilated if it has >0% ownership
        // - an active hex has ownership > ASSIMILATION_THRESHOLD
        // - an active hex extracts resources and assimilates adjacent hexes

        for (Hexel h : assimilated_hexes) {
            System.out.println(h.i + " " + h.j + " " + h.k);
        }
    }

    public void explore(int n) {
        /*
        Use energy to assimilate distant hexes
        - BFS one layer per cycle
        - sort hexes by resources
        - top hex becomes assimilated, starting with assimilation = ASSIMILATION_THRESHOLD
         */
    }

    public void create_assimilated_hex(int i, int j, int k){
        /*
        Choose a hex to instantly assimilate for one player
        This is for setup. potentially unsafe during play
         */

        Hexel target_hex = game_board.grid.hex_grid.get(i).get(j).get(k);
        if (!target_hex.assimilation_by_player.containsKey(this.name)){
            target_hex.assimilation_by_player.put(this.name, game_board.ASSIMILATION_THRESHOLD);
            assimilated_hexes.add_hex(target_hex);
        }
    }
}


