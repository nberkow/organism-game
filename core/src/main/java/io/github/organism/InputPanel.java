package io.github.organism;

import static java.lang.Math.pow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;

import java.util.ArrayList;

public class InputPanel {
    /*
    A temp class for input handling. Just 3 "buttons"
     */

    GameBoard game_board;
    ArrayList<StepButton> buttons;
    float x = 400f;
    float y = 100f;

    float radius = 10f;

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

    public InputPanel(GameBoard gb) {
        game_board = gb;
        buttons = new ArrayList<>();
        Color [] colors = {Color.RED, Color.BLUE, Color.GREEN};

        for (int i=0; i<3; i++){
            StepButton b = new StepButton(
                this.x,
                this.y + i * radius * 2.5f,
                radius, colors[i], i);
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
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (StepButton b : buttons) {
            game_board.shape_renderer.setColor(b.c);
            game_board.shape_renderer.circle(b.x, b.y, b.radius);
        }
        game_board.shape_renderer.end();
    }
}


