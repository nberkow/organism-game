package io.github.organism;

import com.badlogic.gdx.graphics.Color;

public class Organism {

    TriangularGrid territory;
    GameBoard game_board;

    Player player;
    Color color;

    double energy_store = 0.5;

    public Organism(GameBoard gb) {
        game_board = gb;
        territory = new TriangularGrid(game_board);
    }

    public void extract() {

    }

    public void expand() {

    }

    public void explore () {


    }

    public void exterminate () {


    }

    public void claim_hex(MapHex h){
        territory.add_pos(h.pos);
        h.player = player;
        for (MapVertex v : h.vertex_list){
            claim_vertex(v);
        }
    }

    public void claim_hex(int i, int j, int k){
        //MapHex h = (MapHex) game_board.universe_map.universe_grid.get_pos(i, j, k).content;
        //claim_hex(h);
    }

    public void claim_vertex(MapVertex v){
        territory.add_pos(v.pos);
        v.player = player;
    }

}


