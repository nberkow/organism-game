package io.github.organism;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {

    GameBoard game_board;
    OrthographicCamera camera;
    FitViewport viewport;

    private final int VIRTUAL_WIDTH = 1080/2;  // Virtual resolution width (aspect ratio: 16:9)
    private final int VIRTUAL_HEIGHT = 1920/2; // Virtual resolution height

    @Override
    public void create() {
        // Initialize the camera and viewport for the virtual game world dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        game_board = new GameBoard(this);  // Pass the viewport to GameBoard (to be adjusted there)

        // Set up the input processor with viewport transformation for input coordinates
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                Vector2 touchPos = new Vector2(screenX, screenY);
                viewport.unproject(touchPos);  // Convert screen coordinates to game world coordinates

                // Handle input using the transformed coordinates
                System.out.println(game_board.input_panel.check_buttons(touchPos.x, touchPos.y));
                return true;
            }
        });
    }

    @Override
    public void render() {
        // Clear the screen and render the game board
        ScreenUtils.clear(0, 0, 0, 1);  // Clear with black color
        input();
        logic();
        draw();
    }

    private void input() {
        // Add any additional input processing here if needed
    }

    private void logic() {
        // Add any game logic updates here
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
