package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.LinkedList;

public class ActionQueueBar {

    GameBoard game_board;
    float y_height;
    float x_width;
    PlayerHud hud;

    Player player;

    LinkedList<Integer> action_queue;
    final float RADIUS = 8;
    final float INSET = 0.9f;
    float margin;
    float spacing;
    float x;
    float y;

    public ActionQueueBar(GameBoard gb, PlayerHud ph, Player p){
        game_board = gb;
        hud = ph;
        player = p;
        y = hud.HUD_HEIGHT * 0.315f;
        x = hud.x;
        x_width = hud.HUD_WIDTH * 1.1f;
        margin = hud.BUTTONS_X;

        action_queue = player.get_move_queue();
        spacing = (x_width - (margin)) / (game_board.MAX_QUEUED_ACTIONS);
    }

    public void render(){


        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        float [] action_x_pos = new float [game_board.MAX_QUEUED_ACTIONS];

        float first_x = x + margin;
        if (hud.parity == -1){
            first_x = x + hud.HUD_WIDTH - margin;
        }
        for (int i=0; i<game_board.MAX_QUEUED_ACTIONS-1; i++){
            game_board.shape_renderer.setColor(game_board.game.foreground_color);
            action_x_pos[i] = first_x + ((i) * spacing * hud.parity);
            game_board.shape_renderer.circle(action_x_pos[i], y, RADIUS);
            if (i == 10 || i == 0){
                game_board.shape_renderer.circle(action_x_pos[i], y, RADIUS + 2);
            }
            if (i == 0){
                game_board.shape_renderer.circle(action_x_pos[i], y, RADIUS + 3);
            }

        }
        // make the last circle larger
        action_x_pos[game_board.MAX_QUEUED_ACTIONS-1] = first_x + (hud.parity * ((game_board.MAX_QUEUED_ACTIONS-1) * spacing + RADIUS));
        game_board.shape_renderer.circle(
            action_x_pos[game_board.MAX_QUEUED_ACTIONS-1],
            y,
            RADIUS * 1.5f);
        game_board.shape_renderer.end();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        // end circle
        int i = game_board.MAX_QUEUED_ACTIONS-1;

        boolean first = true;
        float r;
        for (int v : action_queue){
            r = RADIUS - 1;
            if (first){
                r = RADIUS * 1.5f - 1;
                first = false;
            }
            game_board.shape_renderer.setColor(game_board.game.colors[v]);
            game_board.shape_renderer.circle(action_x_pos[i], y, r);

            i -= 1;
        }
        game_board.shape_renderer.end();
    }
}
