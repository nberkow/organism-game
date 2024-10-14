package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GridWindow {


    GameBoard game_board;
    Integer dimension;

    Float hex_side_len;
    Float hex_spacing;

    Float center_x;
    Float center_y;

    GridWindow(GameBoard gb, Integer dim) {
        game_board = gb;
        dimension = dim;
        hex_side_len = 13.0F; // starting default
        hex_spacing = 25F; // starting default
        center_x = game_board.main.VIRTUAL_WIDTH / 2f;
        center_y = game_board.main.VIRTUAL_HEIGHT / game_board.GRID_WINDOW_HEIGHT;
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
        Color c  = new Color(0f, 0.6f, 0f, 1f);
        game_board.shape_renderer.setColor(c);
        game_board.shape_renderer.circle(h.x * hex_spacing + center_x, h.y * hex_spacing + center_y, (float) (hex_side_len * Math.pow(h.resources.floatValue(), 0.25f)));
    }

    public void dispose() {
    }
}
