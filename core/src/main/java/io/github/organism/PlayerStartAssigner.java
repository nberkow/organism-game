package io.github.organism;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.shuffle;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import io.github.organism.map.GridPosition;
import io.github.organism.map.MapHex;
import io.github.organism.map.UniverseMap;
import io.github.organism.player.Player;

public class PlayerStartAssigner {

    GameBoard game_board;
    HashMap<Point, Player> players;
    UniverseMap universe_map;
    Random rng;
    GameConfig cfg;

    PlayerStartAssigner(GameBoard gb){
        game_board = gb;
        players = game_board.players;
        universe_map = game_board.universeMap;
        rng = game_board.rng;
        cfg = game_board.config;
    }

    public void assignStartingHexes(ArrayList<int []> starting_coords) {
        int i = 0;
        for (Point p : players.keySet()) {
            Organism organism = players.get(p).getOrganism();
            int [] coords = starting_coords.get(i);
            if (organism!= null) {
                organism.claim_hex(coords[0], coords[1], coords[2]);
                MapHex hex = (MapHex) universe_map.hexGrid.getPos(coords[0], coords[1], coords[2]).content;
                hex.add_resource(i % 3, 3);
            }
            i ++;
        }
    }

    public ArrayList<int []> randomizeStartingCoords(){
        if (Objects.equals(cfg.layout, "radial")) {
            return radial_starts();
        }
        return  random_starts();
    }

    public ArrayList<int []>  radial_starts(){
        // randomly select a valid hex

        boolean assigned = false;
        int iteration = 0;
        ArrayList<int []> starting_coords = new ArrayList<>();

        while (!assigned && iteration < 1e5) {
            int r = cfg.radius;
            int a = rng.nextInt(r);
            int min_b = max(-r - a, -r);
            int max_b = min(r - a, r);
            int b = rng.nextInt(max_b - min_b) + min_b;

            if (a != 0 || b != 0) {
                int c = -a - b;

                MapHex proposed_hex = (MapHex) game_board.universeMap.hexGrid.getPos(a, b, c).content;
                MapHex mirror_hex = (MapHex) game_board.universeMap.hexGrid.getPos(-a, -b, -c).content;


                if (!proposed_hex.masked && !mirror_hex.masked && proposed_hex.player == null && mirror_hex.player == null) {

                    starting_coords.add(new int [] {a, b, c});
                    starting_coords.add(new int []{b, c, a});
                    starting_coords.add(new int []{c, a, b});

                    if (cfg.humanPlayers + cfg.botPlayers == 6) {
                        starting_coords.add(new int []{-a, -b, -c});
                        starting_coords.add(new int []{-b, -c, -a});
                        starting_coords.add(new int []{-c, -a, -b});
                    }
                    assigned = true;
                }
            }
            iteration += 1;
        }
        return starting_coords;
    }

    public ArrayList<int []>  random_starts(){
        int total_players = cfg.botPlayers + cfg.humanPlayers;
        ArrayList<int []> starting_coords = new ArrayList<>();
        float min_dist = (float) Math.max(cfg.radius / (cfg.humanPlayers + cfg.botPlayers), 2);

        ArrayList<MapHex> map_hexes = new ArrayList<>();
        for (GridPosition pos : game_board.universeMap.hexGrid){
            map_hexes.add((MapHex) pos.content);
        }
        shuffle(map_hexes, rng);

        int i = 0;
        int idx = rng.nextInt(total_players);

        while (starting_coords.size() <= total_players && i < 1e6) {
            boolean usable = true;
            System.out.println(idx);
            MapHex hex = map_hexes.get(idx % map_hexes.size());

            if (hex.masked || hex.player != null) {
                usable = false;
            }

            for (int [] placed_hex : starting_coords) {
                float dist = (float)  Math.pow((
                    Math.pow(placed_hex[0] - hex.pos.i, 2) +
                    Math.pow(placed_hex[1] - hex.pos.j, 2) +
                    Math.pow(placed_hex[2] - hex.pos.k, 2)),
                0.5f);

                if (dist < min_dist) {
                    usable = false;
                    break;
                }
            }
            if (usable) {
                starting_coords.add(new int [] {hex.pos.i, hex.pos.j, hex.pos.k});
            }
            i++;
            idx += 1;
        }
        return starting_coords;
    }
}
