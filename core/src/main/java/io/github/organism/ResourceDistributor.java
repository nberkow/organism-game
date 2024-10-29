package io.github.organism;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Random;

public class ResourceDistributor {

    final int RESOURCE_TYPES = 3;
    ArrayList<int []> centers;

    double decay = .8d;
    int number;

    GameBoard game_board;

    Random rng;


    ResourceDistributor(GameBoard gb){
        game_board = gb;
        centers = new ArrayList<>();

    }

    public void create_centers(int number){
        rng = game_board.rng;
        int avg_radius = game_board.radius / 2;
        int range = game_board.radius / 4;

        int radius = rng.nextInt(avg_radius - range) + range;

        for (int i=0; i<number; i++) {
            int a = radius - (rng.nextInt(radius) * 2);
            int b = radius - (rng.nextInt(radius) * 2);
            int c = radius - (rng.nextInt(radius) * 2);

            centers.add(new int[]{a, b, c});
            centers.add(new int[]{c, a, b});
            centers.add(new int[]{b, c, a});
        }
    }

    public void create_symmetrical_patches(int total_to_add){
         int added = 0;

        while (added < total_to_add){

            // Select a random hex
            int i = rng.nextInt(2 * game_board.radius) - game_board.radius;
            int min_j = max(-game_board.radius - i, -game_board.radius);
            int max_j = min(game_board.radius - i, game_board.radius);
            int j = rng.nextInt(max_j - min_j) + min_j;
            int k = -i - j;

            GridPosition hex_pos = game_board.universe_map.hex_grid.get_pos(i, j, k);
            if (hex_pos.content != null){
                MapHex map_hex = (MapHex) hex_pos.content;

                if (map_hex.total_resources < 3){

                    int res = choose_resource_type(hex_pos);
                    i = 0;
                    for (int [] p : get_symmetrical_hexes(hex_pos)){
                        MapHex h = (MapHex) game_board.universe_map.hex_grid.get_pos(p[0], p[1], p[2]).content;
                        h.add_resource((res + i) % 3);
                        i = (i + 1) % 3;
                    }
                    added++;

                }
            }
        }
    }

    public int [] [] get_symmetrical_hexes(GridPosition hex_pos) {
        return new int[][]{
            {hex_pos.i, hex_pos.j, hex_pos.k},
            {hex_pos.j, hex_pos.k, hex_pos.i},
            {hex_pos.k, hex_pos.i, hex_pos.j}
        };
    }




    public int choose_resource_type(GridPosition hex_pos){

        float [] distances = get_center_distances(hex_pos);

        float r = rng.nextFloat();
        int res = 0;
        float sum = 0f;
        boolean found = false;

        while (!found){
            sum += distances[res];

            if (r < sum){
                found = true;
            }
        }
        return res;
    }

    public float [] get_center_distances(GridPosition hex_pos){
        float [] distances = new float [3];
        float total = 0f;
        int i = 0;

        for (int [] c : centers) {
            float d = (float) Math.pow((
                Math.pow(c[0] - hex_pos.i, 2) +
                Math.pow(c[1] - hex_pos.j, 2) +
                Math.pow(c[2] - hex_pos.k, 2)
            ), 0.5);
            distances[i] = d;
            total += d;
            i++;
        }

        for (i=0; i<3; i++){
            distances[i] = distances[i]/total;
        }

        return distances;
    }

}
