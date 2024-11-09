package io.github.organism;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class OrganismGame extends Game {
    ShapeRenderer shape_renderer;
    GameScreen game_screen;
    MenuScreen menu_screen;
    LabScreen lab_screen;
    SpriteBatch batch;
    GameBoard game_board;
    OrthographicCamera camera;
    FitViewport viewport;
    GameInputProcessor input_processor;
    public final int VIRTUAL_WIDTH = 1920/2;  // Virtual resolution width
    public final int VIRTUAL_HEIGHT = 1080/2; // Virtual resolution height

    @Override
    public void create() {
        // Initialize the camera and viewport for the virtual game world dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        batch = new SpriteBatch();
        shape_renderer = new ShapeRenderer();

        menu_screen = new MenuScreen(this);
        game_screen = new GameScreen(this);
        lab_screen = new LabScreen(this);

        input_processor = new GameInputProcessor(game_screen);
        game_screen.input_processor = input_processor;
        Gdx.input.setInputProcessor(input_processor);

        this.setScreen(game_screen);

    }

    @Override
    public void render() {
        // Clear the screen and render the game board
        super.render();
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
