package io.github.organism;

import static java.lang.Math.pow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class GameplayButtons {
    /*
    A temp class for input handling.
     */

    GameBoard game_board;
    ArrayList<StepButton> buttons;
    float [] center_x;
    float center_y;

    float radius = 13f;

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

    public GameplayButtons(GameBoard gb, int players) {

        game_board = gb;
        buttons = new ArrayList<>();

        center_x = new float [] {game_board.main.VIRTUAL_WIDTH / 2f};

        if (players == 2){
            center_x = new float [] {
                game_board.main.VIRTUAL_WIDTH / 3f,
                game_board.main.VIRTUAL_WIDTH  * 2/ 3f,
            };
        }

        center_y = game_board.main.VIRTUAL_HEIGHT / game_board.GAMEPLAY_BUTTONS_HEIGHT;

        for (int j=0; j<players; j++) {
            for (int i = 0; i <= 2; i++) {
                StepButton b = new StepButton(
                    center_x[j] + (i - 1) * radius * 2.7f,
                    center_y,
                    radius, game_board.colors[i], i, j);
                buttons.add(b);
            }
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
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        for (StepButton b : buttons) {
            game_board.shape_renderer.setColor(game_board.foreground_color);
            game_board.shape_renderer.circle(b.x, b.y, b.radius * 1.2f);
        }

        game_board.shape_renderer.end();
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (StepButton b : buttons) {
            game_board.shape_renderer.setColor(b.c);
            game_board.shape_renderer.circle(b.x, b.y, b.radius);
        }

        game_board.shape_renderer.end();

    }
}



