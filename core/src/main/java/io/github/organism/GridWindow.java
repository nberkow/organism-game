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
        for (Hexel h : game_board.universal_grid.map_grid) {
            draw_hex(h);
        }
    }

    private void draw_hex(Hexel h){
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game_board.shape_renderer.setColor(game_board.resource_color);
        game_board.shape_renderer.circle(h.x * hex_spacing + center_x, h.y * hex_spacing + center_y, (float) (hex_side_len * Math.pow(h.resources.floatValue(), 0.25f)));
        game_board.shape_renderer.end();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        for (String p : h.assimilation_by_player.keySet()){
            float assimilation = h.assimilation_by_player.get(p).floatValue();
            Color player_color = game_board.players.get(p).color;
            game_board.shape_renderer.setColor(player_color);
            game_board.shape_renderer.circle(h.x * hex_spacing + center_x, h.y * hex_spacing + center_y, (float) (hex_side_len * Math.pow(assimilation, 0.25f)));

        }
        game_board.shape_renderer.end();
    }

    public void dispose() {
    }
}
