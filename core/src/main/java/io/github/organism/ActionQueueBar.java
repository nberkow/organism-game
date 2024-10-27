package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.LinkedList;

public class ActionQueueBar {

    GameBoard game_board;
    float y_height;
    float x_width;

    String player_name;

    LinkedList<Integer> action_queue;
    final float RADIUS = 8;
    final float INSET = 0.9f;
    final float MARGIN = 55f;
    float spacing;

    float center_x;

    public ActionQueueBar(GameBoard gb, String name, float cx){
        game_board = gb;
        player_name = name;
        center_x = cx;
        action_queue = game_board.players.get(player_name).get_move_queue();
        y_height =  game_board.main.VIRTUAL_HEIGHT / game_board.ACTION_HISTORY_HEIGHT;
        x_width = game_board.main.VIRTUAL_WIDTH / 2f;
        spacing = (x_width - (2*MARGIN)) / (game_board.MAX_QUEUED_ACTIONS);
    }

    public void render(){

        float left_start = MARGIN + center_x / 2;

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);

        // make the first circle larger
        game_board.shape_renderer.setColor(game_board.foreground_color);
        game_board.shape_renderer.circle(left_start + RADIUS, y_height, RADIUS * 1.5f);

        for (int i=1; i<game_board.MAX_QUEUED_ACTIONS; i++){
            game_board.shape_renderer.setColor(game_board.foreground_color);
            game_board.shape_renderer.circle(left_start + ((i + 1) * spacing), y_height, RADIUS);
        }
        game_board.shape_renderer.end();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        int i = 0;
        float a = INSET / 2;
        float r = RADIUS * INSET * 1.5f;
        boolean first = true;
        for (int v : action_queue){
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
