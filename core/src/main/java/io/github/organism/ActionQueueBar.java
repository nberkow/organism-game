package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.LinkedList;

public class ActionQueueBar {

    OrganismGame game;
    float y_height;
    float x_width;
    PlayerHud hud;

    final float RADIUS = 8;
    final float INSET = 0.9f;
    float margin;
    float spacing;
    float x;
    float y;

    public ActionQueueBar(OrganismGame g, PlayerHud ph){
        game = g;
        hud = ph;
        y = hud.HUD_HEIGHT * 0.315f;
        x = hud.x;
        x_width = hud.HUD_WIDTH * 1.1f;
        margin = hud.BUTTONS_X;

        spacing = (x_width - (margin)) / (GameBoard.MAX_QUEUED_ACTIONS);
    }


    public void render(){

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        float [] action_x_pos = new float [GameBoard.MAX_QUEUED_ACTIONS];

        float first_x = x + margin;
        if (hud.parity == -1){
            first_x = x + hud.HUD_WIDTH - margin;
        }
        for (int i=0; i<GameBoard.MAX_QUEUED_ACTIONS-1; i++){
            game.shape_renderer.setColor(game.foreground_color);
            action_x_pos[i] = first_x + ((i) * spacing * hud.parity);
            game.shape_renderer.circle(action_x_pos[i], y, RADIUS);
            if (i == 10 || i == 0){
                game.shape_renderer.circle(action_x_pos[i], y, RADIUS + 2);
            }
            if (i == 0){
                game.shape_renderer.circle(action_x_pos[i], y, RADIUS + 3);
            }

        }
        // make the last circle larger
        action_x_pos[GameBoard.MAX_QUEUED_ACTIONS-1] = first_x + (hud.parity * ((GameBoard.MAX_QUEUED_ACTIONS-1) * spacing + RADIUS));
        game.shape_renderer.circle(
            action_x_pos[GameBoard.MAX_QUEUED_ACTIONS-1],
            y,
            RADIUS * 1.5f);
        game.shape_renderer.end();

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        // end circle
        int i = GameBoard.MAX_QUEUED_ACTIONS-1;

        boolean first = true;
        float r;

        if (hud.player != null) {
            for (int v : hud.player.get_move_queue()) {
                r = RADIUS - 1;
                if (first) {
                    r = RADIUS * 1.5f - 1;
                    first = false;
                }
                game.shape_renderer.setColor(game.action_colors[v]);
                game.shape_renderer.circle(action_x_pos[i], y, r);

                i -= 1;
            }
        }
        game.shape_renderer.end();
    }
}
