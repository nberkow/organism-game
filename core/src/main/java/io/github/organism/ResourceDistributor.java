package io.github.organism;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.shuffle;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ResourceDistributor {

    GameConfig cfg;
    ArrayList<MapHex> hexes_to_fill;
    ArrayList<MapHex> centers;
    int n_centers;
    float total_resources;

    GameBoard game_board;

    Random rng;


    ResourceDistributor(GameBoard gb){
        game_board = gb;
        rng = game_board.rng;
        cfg = game_board.config;
        rng.setSeed(cfg.seed);

        hexes_to_fill = new ArrayList<>();
        for (GridPosition pos : game_board.universe_map.hex_grid){
            MapHex hex = (MapHex) pos.content;
            hexes_to_fill.add(hex);
        }

        n_centers =  cfg.radius - 4;
        total_resources = (cfg.resources + 1) * cfg.radius * 3;

        shuffle(hexes_to_fill, rng);

    }

    public void distribute() {

        ArrayList<ArrayList<MapHex>> patches;
        //if (Objects.equals(cfg.layout, "radial")) {
        patches = create_radial_patches();




        //} else {
        //    patches = create_random_patches();
        //}
        fill_patches(patches);
    }
    public ArrayList<MapHex> center_to_patch(MapHex center) {

        // add the center and hexes around it to a list. hexes near the center will be represented more than once
        ArrayList<MapHex> resource_patch_hexes = new ArrayList<>();

        ArrayList<MapHex> hexes_to_add = new ArrayList<>();
        ArrayList<MapHex> current_hexes = new ArrayList<>();
        current_hexes.add(center);

        for (int r = 0; r < cfg.radius; r++) {
            for (MapHex hex : current_hexes) {
                for (MapVertex v : hex.vertex_list) {
                    for (MapHex n : v.adjacent_hexes) {
                        if (n != null && !n.masked) {
                            hexes_to_add.add(n);
                        }
                    }
                }
            }
            resource_patch_hexes.addAll(hexes_to_add);
            current_hexes = hexes_to_add;
            hexes_to_add.clear();
        }

        return resource_patch_hexes;
    }

    public void fill_patches(ArrayList<ArrayList<MapHex>> patches) {

        // add equal resources of each type
        int iterations;
        int added;
        int idx;

        added = 0;
        iterations = 0;
        idx = 0;
        while (added < total_resources * 3 && iterations < 10e5) {
            for (int r = 0; r < 3; r++) {
                ArrayList<MapHex> patch = patches.get(r);

                MapHex hex = patch.get(idx % patch.size());
                if (hex != null && !hex.masked && hex.total_resources < 3) {
                    hex.add_resource(r);
                    added ++;
                } else {
                    iterations++;
                }
            }
            idx++;
        }
    }

    public ArrayList<ArrayList<MapHex>> create_random_patches(){

        // initialize 3 lists of hexes, one per resource
        ArrayList<ArrayList<MapHex>> patches = new ArrayList<>();
        for (int r=0; r<3; r++){
            patches.add(new ArrayList<>());
        }

        // pick centers and create patches
        for (int i=0; i<n_centers;i++){
            for (int j=0; j<3; j++) {
                MapHex hex = hexes_to_fill.get(i * 3 + j);
                patches.get(j).addAll(center_to_patch(hex));
            }
        }

        // shuffle in all the hexes
        for (int i=0; i<n_centers;i++){
            patches.get(i).addAll(hexes_to_fill);
            shuffle(patches.get(i));
        }

        return patches;
    }

    public ArrayList<ArrayList<MapHex>> create_radial_patches(){

        System.out.println("create_radial_patches " + n_centers);
        // initialize 3 lists of hexes, one per resource
        ArrayList<ArrayList<MapHex>> patches = new ArrayList<>();
        for (int r=0; r<3; r++){
            patches.add(new ArrayList<>());
        }

        // randomly create one-fold of the symmetry and rotate it
        ArrayList<MapHex> one_fold_patch_hexes;
        for (int i=0; i<n_centers;i++) {
            System.out.println("i" + i);
            MapHex hex = hexes_to_fill.get(i);

            one_fold_patch_hexes = center_to_patch(hex);
            List<MapHex> random_hexes = hexes_to_fill.subList(n_centers, n_centers + one_fold_patch_hexes.size() * 2);
            one_fold_patch_hexes.addAll(random_hexes); // shuffle in random the hexes
            shuffle(one_fold_patch_hexes, rng);

            for (MapHex ofp : one_fold_patch_hexes){
                int[][] radial_centers = get_symmetrical_hexes(ofp.pos);
                for (int j = 0; j < 3; j++) {
                    System.out.println("j " + j);
                    GridPosition pos = game_board.universe_map.hex_grid.get_pos(
                        radial_centers[j][0], radial_centers[j][1], radial_centers[j][2]
                    );
                    if (pos.content != null){
                        MapHex h = (MapHex) pos.content;
                        if (!h.masked) {
                            patches.get(j).add(h);
                        }
                    }
                }
                System.out.println("end:");
                for (int r=0; r<3; r++){
                    System.out.println("patch " + r + " " + patches.get(r).size());
                }
            }

        }
        return patches;
    }

    public int [] [] get_symmetrical_hexes(GridPosition hex_pos) {
        return new int [] [] {
            {hex_pos.i, hex_pos.j, hex_pos.k},
            {hex_pos.k, hex_pos.i, hex_pos.j},
            {hex_pos.j, hex_pos.k, hex_pos.i}
        };
    }

}
