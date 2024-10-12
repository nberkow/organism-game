package io.github.organism;

import static java.lang.Math.min;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class UniversalHexGrid {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, Hexel>>> hex_grid;

    ArrayList<Float> gradient_means;

    int grid_radius;

    UniversalHexGrid(int r){
        hex_grid = new HashMap<>();
        grid_radius = r;
        Random rng = new Random(10);


        int min_j;
        int max_j;
        for (int layer=0; layer<=grid_radius; layer++){

            for (int i=-layer; i<=layer; i++){

                if (!hex_grid.containsKey(i)){
                    hex_grid.put(i, new HashMap<>());
                }

                min_j = max(-layer - i, -layer);
                max_j = min(layer - i, layer);
                for (int j=min_j; j<=max_j; j++) {

                    if (!hex_grid.get(i).containsKey(j)){
                        hex_grid.get(i).put(j, new HashMap<>());
                    }

                    int k = -i - j;

                    Hexel h = new Hexel();
                    h.i = i;
                    h.j = j;
                    h.k = k;
                    h.x = (float) (sqrt(3F) * ( k/2F + i));
                    h.y = (3F/2F * k);
                    h.resources = rng.nextDouble(); //
                    hex_grid.get(i).get(j).put(k, h);

                }
            }
        }
    }

    public HexSet get_adjacent_layer(HexSet hex_set){

        HexSet adjacent_hexes = new HexSet();

        for (Hexel h : hex_set.dump_hex_list()){
            HexSet surrounding = get_surrounding_hexes(h);
            for (Hexel a : surrounding.dump_hex_list()){
                if (!hex_set.contains_hex(a)){
                    if (!adjacent_hexes.contains_hex(h)){
                        adjacent_hexes.add_hex(h);
                    }
                }
            }
        }

        return adjacent_hexes;
    }

    public HexSet get_surrounding_hexes(Hexel h){

        HexSet surrounding = new HexSet();

        surrounding.add_hex(hex_grid.get(h.i - 1).get(h.j + 1).get(h.k));
        surrounding.add_hex(hex_grid.get(h.i - 1).get(h.j).get(h.k + 1));

        surrounding.add_hex(hex_grid.get(h.i).get(h.j - 1).get(h.k + 1));
        surrounding.add_hex(hex_grid.get(h.i).get(h.j + 1).get(h.k - 1));

        surrounding.add_hex(hex_grid.get(h.i + 1).get(h.j - 1).get(h.k));
        surrounding.add_hex(hex_grid.get(h.i + 1).get(h.j).get(h.k - 1));

        return surrounding;
    }
}
