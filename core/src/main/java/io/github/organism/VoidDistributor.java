package io.github.organism;

import static java.util.Collections.shuffle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class VoidDistributor {

    GameConfig cfg;
    int n_centers;
    int max_size;
    GameBoard game_board;
    Random rng;

    ArrayList<MapHex> map_hexes;

    VoidDistributor(GameBoard gb){
        game_board = gb;
        rng = game_board.rng;
        cfg = game_board.config;
        rng.setSeed(cfg.seed);

        map_hexes = new ArrayList<>();
        for (GridPosition pos : game_board.universe_map.hex_grid){
            MapHex hex = (MapHex) pos.content;
            map_hexes.add(hex);
        }
        shuffle(map_hexes, rng);
        n_centers = (int) ((int) (6 - cfg.vertex_density) * (Math.pow(cfg.radius, 1.5)/7));
        max_size = (int) (cfg.radius/cfg.vertex_density);

    }

    public void distribute() {

        ArrayList<MapHex> void_hexes;
        if (Objects.equals(cfg.layout, "radial")) {
            void_hexes = create_radial_voids();

        } else {
            void_hexes = create_random_voids();
        }
        for (MapHex v : void_hexes){
            v.masked = true;
        }

    }
    public HashSet<MapHex> center_to_void(MapHex center) {

        // add the center and hexes around it to a list. hexes near the center will be represented more than once
        HashSet<MapHex> void_hexes = new HashSet<>();
        MapHex hex = center;
        void_hexes.add(hex);

        int random_index;
        ArrayList<MapHex> adj;
        HashSet<MapHex> adjacent_hexes;

        for (int r = 0; r < max_size; r++) {
            adjacent_hexes = new HashSet<>();
            for (MapVertex v : hex.vertex_list) {
                adjacent_hexes.addAll(v.adjacent_hexes);
            }
            adj = new ArrayList<>(adjacent_hexes);
            random_index = rng.nextInt(adjacent_hexes.size());
            hex = adj.get(random_index);
            void_hexes.add(hex);
        }

        return void_hexes;
    }



    public ArrayList<MapHex> create_random_voids(){

        ArrayList<MapHex> void_hexes = new ArrayList<>();
        MapHex hex;

        for (int i=0; i<n_centers * 3; i++){
            hex = map_hexes.get(i);
            void_hexes.addAll(center_to_void(hex));
        }

        return void_hexes;
    }

    public ArrayList<MapHex> create_radial_voids(){

        ArrayList<MapHex> void_hexes = new ArrayList<>();
        MapHex hex;

        // randomly create one-fold of the symmetry and rotate it
        ArrayList<MapHex> one_fold_patch_hexes;
        for (int i=0; i<n_centers;i++) {

            hex = map_hexes.get(i);
            one_fold_patch_hexes = new ArrayList<>(center_to_void(hex));

            for (MapHex ofp : one_fold_patch_hexes){
                int[][] radial_centers = get_symmetrical_hexes(ofp.pos);
                for (int j = 0; j < 3; j++) {
                    GridPosition pos = game_board.universe_map.hex_grid.get_pos(
                        radial_centers[j][0], radial_centers[j][1], radial_centers[j][2]
                    );
                    if (pos.content != null){
                        MapHex h = (MapHex) pos.content;
                        void_hexes.add(h);
                    }
                }

            }

        }
        return void_hexes;
    }

    public int [] [] get_symmetrical_hexes(GridPosition hex_pos) {
        return new int [] [] {
            {hex_pos.i, hex_pos.j, hex_pos.k},
            {hex_pos.k, hex_pos.i, hex_pos.j},
            {hex_pos.j, hex_pos.k, hex_pos.i}
        };
    }

}
