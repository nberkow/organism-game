package io.github.organism;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class PlayerStartAssigner {

    GameBoard game_board;
    HashMap<String, Player> players;
    UniverseMap universe_map;
    Random rng;
    GameConfig cfg;

    PlayerStartAssigner(GameBoard gb){
        game_board = gb;
        players = game_board.players;
        universe_map = game_board.universe_map;
        rng = game_board.rng;
        cfg = game_board.config;
    }

    public void assign_starting_hexes(ArrayList<int[]> starting_coords) {
        int i = 0;
        for (String player_name : players.keySet()) {
            Organism organism = players.get(player_name).get_organism();
            int [] coords = starting_coords.get(i);
            organism.claim_hex(coords[0], coords[1], coords[2]);
            MapHex hex = (MapHex) universe_map.hex_grid.get_pos(coords[0], coords[1], coords[2]).content;
            hex.add_resource(i%3, 3);
            i ++;
        }
    }

    public ArrayList<int[]> randomize_starting_coords(){
        ArrayList<int []> starting_coords;
        if (Objects.equals(cfg.layout, "radial")) {
            return radial_starts();
        }
        return  random_starts();
    }

    public ArrayList<int[]>  radial_starts(){
        // randomly select a valid hex
        boolean assigned = false;
        int iteration = 0;
        ArrayList<int[]> starting_coords = new ArrayList<>();

        while (!assigned && iteration < 1e5) {
            int r = cfg.radius / 2;
            int a = rng.nextInt(r + 1) - r / 2;
            int min_b = max(-r - a, -r);
            int max_b = min(r - a, r);
            int b = rng.nextInt(max_b - min_b) + min_b;

            if (a != 0 || b != 0) {
                int c = -a - b;

                MapHex proposed_hex = (MapHex) game_board.universe_map.hex_grid.get_pos(a * 2, b * 2, c * 2).content;
                MapHex mirror_hex = (MapHex) game_board.universe_map.hex_grid.get_pos(-a * 2, -b * 2, -c * 2).content;


                if (!proposed_hex.masked && !mirror_hex.masked && proposed_hex.player == null && mirror_hex.player == null) {

                    starting_coords.add(new int[]{a * 2, b * 2, c * 2});
                    starting_coords.add(new int[]{b * 2, c * 2, a * 2});
                    starting_coords.add(new int[]{c * 2, a * 2, b * 2});

                    if (cfg.human_players + cfg.bot_players == 6) {
                        starting_coords.add(new int[]{-a * 2, -b * 2, -c * 2});
                        starting_coords.add(new int[]{-b * 2, -c * 2, -a * 2});
                        starting_coords.add(new int[]{-c * 2, -a * 2, -b * 2});
                    }
                    assigned = true;
                }
            }
            iteration += 1;
        }
        return starting_coords;
    }

    public ArrayList<int []>  random_starts(){
        ArrayList<int []> starting_coords = new ArrayList<>();
        float min_dist = (float) cfg.radius / 3;

        int i = 0;
        while (starting_coords.size() < cfg.bot_players + cfg.human_players && i < 1e6) {
            boolean usable = true;

            int r = cfg.radius / 2;
            int a = rng.nextInt(r + 1) - r / 2;
            int min_b = max(-r - a, -r);
            int max_b = min(r - a, r);
            int b = rng.nextInt(max_b - min_b) + min_b;
            int c = -a - b;

            MapHex hex = (MapHex) game_board.universe_map.hex_grid.get_pos(a, b, c).content;
            if (hex.masked) {
                usable = false;
            }

            for (int[] placed_hex : starting_coords) {
                float dist = (float)  Math.pow((
                    Math.pow(placed_hex[0] - a, 2) +
                    Math.pow(placed_hex[1] - b, 2) +
                    Math.pow(placed_hex[2] - c, 2)),
                0.5f);
                if (dist < min_dist) {

                    usable = false;
                }
            }
            if (usable) {
                starting_coords.add(new int[]{a, b, c});
            }
            i++;
        }
        return starting_coords;
    }
}
