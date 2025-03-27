package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.map.GridPosition;

public class GridWindow {

    GameBoard gameBoard;
    Integer dimension;

    GridWindow(GameBoard gb, Integer dim) {
        gameBoard = gb;
        dimension = dim;
    }

    public void render(){

        if (gameBoard.game.shapeRenderer == null) {
            return;
        }

        gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : gameBoard.universeMap.hexGrid) {
            if (pos.content != null) pos.content.render();
        }
        for (GridPosition pos : gameBoard.universeMap.vertexGrid) {
            if (pos.content != null) pos.content.render();
        }

        gameBoard.game.shapeRenderer.end();
    }


    public void dispose() {
    }
}
