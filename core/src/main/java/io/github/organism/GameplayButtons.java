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
    float center_x;
    float center_y;

    float radius = 30f;

    private class StepButton {
        float x;
        float y;
        float radius;
        Color c;
        int value;

        public StepButton(float x, float y, float radius, Color c, int value) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.c = c;
            this.value = value;
        }
    }

    public GameplayButtons(GameBoard gb) {

        game_board = gb;
        buttons = new ArrayList<>();
        center_x = game_board.main.VIRTUAL_WIDTH / 2f;
        center_y = game_board.main.VIRTUAL_HEIGHT / game_board.GAMEPLAY_BUTTONS_HEIGHT;

        for (int i=0; i<=2; i++){
            StepButton b = new StepButton(
                center_x + (i-1) * radius * 2.7f,
                center_y,
                radius, game_board.colors[i], i);
            buttons.add(b);
        }
    }

    public int check_buttons(float mouse_x, float mouse_y){
        for (StepButton b : buttons) {
            float d = (float) pow(pow(mouse_x - b.x, 2f) + pow(mouse_y - b.y, 2f), 0.5);
            if (d < radius){
                return b.value;
            }
        }
        return -1;
    }

    public void render() {
        game_board.shape_renderer.setProjectionMatrix(game_board.main.viewport.getCamera().combined); // Set the correct projection matrix

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



