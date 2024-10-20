package io.github.organism;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {

    GameBoard game_board;
    OrthographicCamera camera;
    FitViewport viewport;
    GameInputProcessor input_processor;
    GameTimers game_timers;

    double action_time = 0.2d;

    public final int VIRTUAL_WIDTH = 1920/2;  // Virtual resolution width
    public final int VIRTUAL_HEIGHT = 1080/2; // Virtual resolution height

    @Override
    public void create() {
        // Initialize the camera and viewport for the virtual game world dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        game_board = new GameBoard(this);  // Pass the viewport to GameBoard (to be adjusted there)
        game_timers = new GameTimers();

        input_processor = new GameInputProcessor(game_board);
        Gdx.input.setInputProcessor(input_processor);
    }

    @Override
    public void render() {
        // Clear the screen and render the game board
        ScreenUtils.clear(game_board.background_color);  // Clear with black color
        input();
        logic();
        draw();
    }

    private void input() {
        // handle holding down a button vs simple clicking
        if (input_processor.touched_button && !input_processor.holding_button){
            input_processor.time_held += Gdx.graphics.getDeltaTime();
        }
        if (input_processor.touched_button && input_processor.time_held > input_processor.HOLD_DELAY){
            input_processor.holding_button = true;
        }
    }

    private void logic() {
        // Update the clock
        game_timers.add_time_delta(Gdx.graphics.getDeltaTime());

        if (game_timers.action_clock > action_time){
            game_timers.action_clock -= action_time;
            if (input_processor.holding_button) {
                input_processor.action_ready = true;
            }
        }

        // This will be true if the button is being held down or it was just fast clicked
        if (input_processor.action_ready) {
            game_board.apply_action(input_processor.button_val, input_processor.actions_staged);
            input_processor.action_ready = false;
        }
    }

    private void draw() {
        // Ensure the camera is updated before drawing
        camera.update();
        game_board.render();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport and camera based on the new window size
        viewport.update(width, height, true);  // true centers the camera
    }

    @Override
    public void dispose() {
        // Handle disposing of game resources
        game_board.dispose();
    }
}
