package io.github.organism;

import static java.lang.Math.pow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class GameplayButtons {
    /*
    A temp class for input handling.
     */

    public class StepButton {
        float x;
        float y;
        float radius;
        Color c;
        int value;

        int player;

        public StepButton(float x, float y, float radius, Color c, int value, int player) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.c = c;
            this.value = value;
            this.player = player;
        }
    }

    GameBoard game_board;
    PlayerHud hud;
    ArrayList<StepButton> buttons;
    float center_x;
    float center_y;

    float radius = 13f;
    public GameplayButtons(GameBoard gb, PlayerHud ph) {

        game_board = gb;
        hud = ph;
        buttons = new ArrayList<>();

        center_x = hud.x + hud.BUTTON_SIDE_DIST;
        if (hud.parity == -1){
            center_x = hud.x + hud.HUD_WIDTH - hud.BUTTON_SIDE_DIST;
        }

        center_y = radius * 2f;

        for (int i = 0; i <= 2; i++) {
            StepButton b = new StepButton(
                center_x + (i - 1) * radius * 2.7f * hud.parity,
                center_y,
                radius, game_board.game.action_colors[i], i, 1);
            buttons.add(b);
        }
    }

    public StepButton check_buttons(float mouse_x, float mouse_y){
        for (StepButton b : buttons) {
            float d = (float) pow(pow(mouse_x - b.x, 2f) + pow(mouse_y - b.y, 2f), 0.5);
            if (d < radius){
                return b;
            }
        }
        return null;
    }

    public void render() {
        game_board.game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        for (StepButton b : buttons) {
            game_board.game.shape_renderer.setColor(game_board.game.foreground_color);
            game_board.game.shape_renderer.circle(b.x, b.y, b.radius * 1.2f);
        }

        game_board.game.shape_renderer.end();
        game_board.game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (StepButton b : buttons) {
            game_board.game.shape_renderer.setColor(b.c);
            game_board.game.shape_renderer.circle(b.x, b.y, b.radius);
        }

        game_board.game.shape_renderer.end();

    }
}



