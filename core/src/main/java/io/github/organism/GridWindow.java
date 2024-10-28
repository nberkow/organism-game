package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GridWindow {

    GameBoard game_board;
    Integer dimension;

    GridWindow(GameBoard gb, Integer dim) {
        game_board = gb;
        dimension = dim;
    }

    public void render(){

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : game_board.universe_map.hex_grid) {
            pos.content.render();
        }
        for (GridPosition pos : game_board.universe_map.vertex_grid) {
            pos.content.render();
        }

        game_board.shape_renderer.end();
    }







    public void dispose() {
    }
}
