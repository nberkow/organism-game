package io.github.organism.map;

import static java.lang.Math.max;
import static java.lang.Math.min;

import io.github.organism.GameBoard;

public class UniverseMap {

    public TriangularGrid hexGrid;
    public TriangularGrid vertexGrid;
    public GameBoard game_board;
    int gridRadius;

    final int [][] VERTEX_POS = {
        { 1, 0,  0},
        { 0, 0, -1},
        { 0, 1, 0},
        { -1, 0, 0},
        { 0, 0, 1},
        { 0, -1, 0},
    };

    public UniverseMap(GameBoard gb, int r) {
        game_board = gb;
        gridRadius = r;
        hexGrid = new TriangularGrid(game_board);
        vertexGrid = new TriangularGrid(game_board);
        buildMap();
    }

    private void buildMap() {

        int min_j;
        int max_j;

        int layer = gridRadius;
        for (int i=-layer; i<=layer; i++){
            min_j = max(-layer - i, -layer);
            max_j = min(layer - i, layer);
            for (int j=min_j; j<=max_j; j++) {
                int k = -i -j;

                GridPosition hex_pos = new GridPosition(i, j, k, hexGrid);
                hexGrid.addPos(hex_pos);
                MapHex hex = new MapHex(hex_pos);
                hex_pos.content = hex;
                addVertices(hex_pos, hex);

            }
        }

    }

    private void addVertices(GridPosition hex_pos, MapHex hex){

        for (int n=0; n<6; n++){
            int [] p = VERTEX_POS[n];
            int a = hex_pos.i + p[0];
            int b = hex_pos.j + p[1];
            int c = hex_pos.k + p[2];

            GridPosition vertex_pos;
            MapVertex vertex;

            if (vertexGrid.contains_position(a, b, c)){
                vertex_pos = vertexGrid.getPos(a, b, c);
                vertex = (MapVertex) vertex_pos.content;
            }

            else {
                vertex_pos = new GridPosition(a, b, c, vertexGrid);
                vertex = new MapVertex(vertex_pos);
                vertex_pos.content = vertex;
                vertexGrid.addPos(vertex_pos);
            }

            hex.vertexList[n] = vertex;
            vertex.adjacentHexes.add(hex);
        }

        // connect the vertices explicitly
        for (int v=0; v<6; v++){
            MapVertex center = hex.vertexList[v];
            MapVertex left = hex.vertexList[(v+1) % 6];
            MapVertex right = hex.vertexList[(v+5) % 6];

            center.adjacentVertices.add(left);
            center.adjacentVertices.add(right);

            left.adjacentVertices.add(center);
            right.adjacentVertices.add(center);

        }
    }

    public void dispose() {
        hexGrid.dispose();
        vertexGrid.dispose();
    }
}


