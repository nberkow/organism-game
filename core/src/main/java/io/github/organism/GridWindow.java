package io.github.organism;

import static java.lang.Math.sqrt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public class GridWindow {


    GameBoard game_board;
    Integer dimension;

    Float hex_side_len;
    Float hex_spacing;

    Float center_x;
    Float centr_y;



    GridWindow(GameBoard gb, Integer dim) {
        game_board = gb;
        dimension = dim;
        hex_side_len = 7.0F; // starting default
        hex_spacing = 12F; // starting default
    }

    public void render(){

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i : game_board.grid.hex_grid.keySet()){
            for (int j : game_board.grid.hex_grid.get(i).keySet()){
                for (int k : game_board.grid.hex_grid.get(i).get(j).keySet()){
                    Hexel h = game_board.grid.hex_grid.get(i).get(j).get(k);
                    draw_hex(h);
                }
            }
        }
        game_board.shape_renderer.end();
    }

    private void draw_hex(Hexel h){

        Color c  = new Color(h.resources.floatValue(), 1f, 1f - h.resources.floatValue(), .01f);
        if (game_board.test_organism.assimilated_hexes.contains_hex(h)){
            c = Color.RED;
        }

        game_board.shape_renderer.setColor(c);
        game_board.shape_renderer.circle(h.x * hex_spacing + 200, h.y * hex_spacing + 200, hex_side_len);
    }

    public void dispose() {
    }
}
