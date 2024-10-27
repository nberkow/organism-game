package io.github.organism;

import static java.lang.Math.min;
import static java.lang.Math.max;

import java.util.ArrayList;

public class UniversalHexGrid {
    HexSet map_grid;
    GameBoard game_board;

    int grid_radius;

    UniversalHexGrid(GameBoard gb, int r){
        game_board = gb;
        map_grid = new HexSet();
        grid_radius = r;

        int min_j;
        int max_j;
        for (int layer=0; layer<=grid_radius; layer++){

            for (int i=-layer; i<=layer; i++){
                min_j = max(-layer - i, -layer);
                max_j = min(layer - i, layer);
                for (int j=min_j; j<=max_j; j++) {
                    int k = -i - j;

                    Hexel h = new Hexel(game_board, i, j, k);
                    h.resources = game_board.gradient_set.get_gradient_score(h);
                    map_grid.add_hex(h);

                }
            }
        }
    }

    public ArrayList<Hexel> get_surrounding_hexes(Hexel h){

        ArrayList<Hexel> surrounding = new ArrayList<>();
        int [] [] coords = {
            {h.i - 1, h.j + 1, h.k},
            {h.i - 1, h.j, h.k + 1},
            {h.i, h.j - 1, h.k + 1},
            {h.i, h.j + 1, h.k - 1},
            {h.i + 1, h.j - 1, h.k},
            {h.i + 1, h.j, h.k - 1}
        };

        for (int[] coord : coords) {
            Hexel g = map_grid.get_hex(coord[0], coord[1], coord[2]);
            if (g != null){
                surrounding.add(g);
            }
        }

        return surrounding;
    }
}
