package io.github.organism;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class UniverseMap {

    TriangularGrid hex_grid;
    TriangularGrid vertex_grid;

    GameBoard game_board;

    int grid_radius;

    final int [][] VERTEX_POS = {
        { 1, 0, 0},
        { 0, 0, -1},
        { 0, 1, 0},
        { -1, 0, 0},
        { 0, 0, 1},
        { 0, -1, 0},





    };

    public UniverseMap(GameBoard gb, int r) {
        game_board = gb;
        grid_radius = r;
        hex_grid = new TriangularGrid(game_board);
        vertex_grid = new TriangularGrid(game_board);
        build_map();
    }

    private void build_map() {

        int min_j;
        int max_j;
        for (int layer=0; layer<=grid_radius; layer++){

            for (int i=-layer; i<=layer; i++){
                min_j = max(-layer - i, -layer);
                max_j = min(layer - i, layer);
                for (int j=min_j; j<=max_j; j++) {
                    int k = -i - j;

                    GridPosition hex_pos = new GridPosition(i, j, k, hex_grid);
                    hex_grid.add_pos(hex_pos);
                    MapHex hex = new MapHex(hex_pos);
                    hex_pos.content = hex;
                    add_vertices(hex_pos, hex);

                }
            }
        }
    }

    private void add_vertices(GridPosition hex_pos, MapHex hex){

        for (int n=0; n<6; n++){
            int [] p = VERTEX_POS[n];
            int a = hex_pos.i + p[0];
            int b = hex_pos.j + p[1];
            int c = hex_pos.k + p[2];

            GridPosition vertex_pos;
            MapVertex vertex;

            if (vertex_grid.contains_position(a, b, c)){
                vertex_pos = vertex_grid.get_pos(a, b, c);
                vertex = (MapVertex) vertex_pos.content;
            }
            else {
                vertex_pos = new GridPosition(a, b, c, vertex_grid);
                vertex = new MapVertex(vertex_pos);
                vertex_pos.content = vertex;
                vertex_grid.add_pos(vertex_pos);
            }

            hex.vertex_list[n] = vertex;
            vertex.adjacent_hexes.add(hex);
        }
    }
}


