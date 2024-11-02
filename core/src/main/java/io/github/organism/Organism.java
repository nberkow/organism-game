package io.github.organism;

import com.badlogic.gdx.graphics.Color;

public class Organism {

    public int income;
    TriangularGrid territory;
    int [] resources;
    int energy;
    GameBoard game_board;
    Player player;
    Color color;

    public Organism(GameBoard gb) {
        game_board = gb;
        territory = new TriangularGrid(game_board);
        resources = new int[3];
        energy = game_board.DEFAULT_STARTING_ENERGY;
        income = 1;
    }

    public void update_resources(){
        resources = new int []   {1, 1, 1};

        for (GridPosition pos : territory){
            if (pos.content instanceof MapHex) {
                MapHex h = (MapHex) pos.content;
                for (int i = 0; i < h.resources.length; i++) {
                    resources[h.resources[i]]++;
                }
            }
        }
    }
    public void update_income(){
        income = 1;
        for (int r=0; r<3; r++){
            income *= (resources[r]);
        }
    }

    public void extract() {

    }

    public void expand() {

    }

    public void explore () {


    }


    public void claim_hex(MapHex h){
        territory.add_pos(h.pos);
        h.player = player;
        for (MapVertex v : h.vertex_list){
            claim_vertex(v);
        }
    }

    public void claim_hex(int i, int j, int k){
        MapHex h = (MapHex) game_board.universe_map.hex_grid.get_pos(i, j, k).content;
        claim_hex(h);
    }

    public void claim_vertex(MapVertex v){
        territory.add_pos(v.pos);
        v.player = player;
    }

}


