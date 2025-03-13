package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GridWindow {

    GameBoard game_board;
    Integer dimension;

    GridWindow(GameBoard gb, Integer dim) {
        game_board = gb;
        dimension = dim;
    }

    public void render(){

        if (game_board.game.shapeRenderer == null) {
            return;
        }

        game_board.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : game_board.universe_map.hex_grid) {
            if (pos.content != null) pos.content.render();
        }
        for (GridPosition pos : game_board.universe_map.vertex_grid) {
            if (pos.content != null) pos.content.render();
        }

        game_board.game.shapeRenderer.end();
    }







    public void dispose() {
    }
}
