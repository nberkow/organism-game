package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.LinkedList;

public class ActionQueueBar {

    GameBoard game_board;
    float y_height;
    float x_width;



    final int MAX_ACTIONS = 20;
    final float RADIUS = 8;
    final float INSET = 0.9f;
    final float MARGIN = 55f;
    float spacing;

    float center_x;

    LinkedList<Integer> action_queue;

    public ActionQueueBar(GameBoard gb, float cx){
        game_board = gb;
        center_x = cx;
        action_queue = new LinkedList<>();
        y_height =  game_board.main.VIRTUAL_HEIGHT / game_board.ACTION_HISTORY_HEIGHT;
        x_width = game_board.main.VIRTUAL_WIDTH / 2f;
        spacing = (x_width - (2*MARGIN)) / (MAX_ACTIONS);


    }

    public void add_action(int action){
        action_queue.add(action);
        if (action_queue.size() > MAX_ACTIONS){
            action_queue.remove();
        }
    }
    public void render(){

        float left_start = MARGIN + center_x / 2;

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);

        // make the first circle larger
        game_board.shape_renderer.setColor(game_board.foreground_color);
        game_board.shape_renderer.circle(left_start + RADIUS, y_height, RADIUS * 1.5f);

        for (int i=1; i<MAX_ACTIONS; i++){
            game_board.shape_renderer.setColor(game_board.foreground_color);
            game_board.shape_renderer.circle(left_start + ((i + 1) * spacing), y_height, RADIUS);
        }
        game_board.shape_renderer.end();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        int i = 0;
        float a = INSET / 2;
        float r = RADIUS * INSET * 1.5f;
        boolean first = true;
        for (int v : action_queue.reversed()){
            game_board.shape_renderer.setColor(game_board.colors[v]);
            game_board.shape_renderer.circle(left_start + ((i + a) * spacing), y_height, r);
            if (first) {
                first = false;
                r = RADIUS * INSET;
                a = 1;
            }
            i += 1;
        }
        game_board.shape_renderer.end();
    }
}
