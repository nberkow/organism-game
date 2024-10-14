package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.LinkedList;

public class ActionHistoryBar {

    GameBoard game_board;
    float y_height;
    float x_width;

    final int MAX_ACTIONS = 20;
    final float RADIUS = 10;
    final float MARGIN = 55f;
    float spacing;

    LinkedList<Integer> visible_history;

    public ActionHistoryBar(GameBoard gb){
        game_board = gb;
        visible_history = new LinkedList<>();
        y_height =  game_board.main.VIRTUAL_HEIGHT / game_board.ACTION_HISTORY_HEIGHT;
        x_width = game_board.main.VIRTUAL_WIDTH;
        spacing = (x_width - (2*MARGIN)) / (MAX_ACTIONS - 1);

    }

    public void add_action(int action){
        visible_history.add(action);
        if (visible_history.size() > MAX_ACTIONS){
            visible_history.remove();
        }
    }
    public void render(){

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i=0; i<MAX_ACTIONS; i++){
            game_board.shape_renderer.setColor(game_board.foreground_color);
            game_board.shape_renderer.circle(MARGIN + (i * spacing), y_height, RADIUS);
        }
        game_board.shape_renderer.end();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        int i = 0;
        for (int v : visible_history.reversed()){
            game_board.shape_renderer.setColor(game_board.colors[v]);
            game_board.shape_renderer.circle(MARGIN + (i * spacing), y_height, RADIUS * .9f);
            i += 1;
        }
        game_board.shape_renderer.end();
    }
}
