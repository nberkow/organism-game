package io.github.organism;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.HashMap;
import java.util.Random;

public class OrganismGame extends Game {

    ShapeRenderer shape_renderer;
    GameScreen game_screen;
    MenuScreen menu_screen;
    LabScreen lab_screen;

    FileHandler file_handler;

    MapSettingsScreen map_edit_screen;
    SpriteBatch batch;
    GameBoard game_board;
    OrthographicCamera camera;
    FitViewport viewport;
    MapSettingsInputProcessor map_input_processor;
    LabInputProcessor lab_input_processor;
    GameInputProcessor input_processor;

    Random rng;

    // Game colors
    Color background_color = Color.BLACK;
    Color foreground_color = Color.CYAN;

    Color [] colors = {Color.RED, Color.BLUE, Color.RED};

    Color exterminate_color = Color.RED;
    HashMap<Integer, BitmapFont> fonts;

    Color [] player_colors = {
        new Color(0xB91372FF),
        new Color(0xD497A7FF),
        new Color(0x6EEB83FF),
        Color.GREEN, Color.RED, Color.MAROON, Color.MAGENTA, Color.BROWN, Color.LIME,
        Color.OLIVE, Color.FIREBRICK, Color.FOREST, Color.PINK, Color.GOLDENROD,
        Color.CORAL, Color.CHARTREUSE, Color.SCARLET, Color.ORANGE, Color.SKY
    };


    Color [] resource_colors_dark = {
        new Color(0f, 0f, 0.3f, 0f),
        new Color(0f, 0.2f, 0f, 0f),
        new Color(0.2f, 0f, 0.2f, 0f)};
    Color [] resource_colors_bright = {
        new Color(0f, 0f, 0.5f, 0f),
        new Color(0f, 0.4f, 0f, 0f),
        new Color(0.4f, 0f, 0.2f, 0f)};
    public final int VIRTUAL_WIDTH = 1920/2;  // Virtual resolution width
    public final int VIRTUAL_HEIGHT = 1080/2; // Virtual resolution height

    @Override
    public void create() {
        // Initialize the camera and viewport for the virtual game world dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        file_handler = new FileHandler(this);
        batch = new SpriteBatch();
        shape_renderer = new ShapeRenderer();
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        rng = new Random();
        rng.setSeed(10);

        fonts = new HashMap<>();
        for (int i=3;i<7;i++){
            int size = (int) Math.pow(2, i);
            fonts.put(size, new BitmapFont(Gdx.files.internal("fonts/dubai" + size + ".fnt")));
        }

        menu_screen = new MenuScreen(this);
        game_screen = new GameScreen(this);
        lab_screen = new LabScreen(this);
        map_edit_screen = new MapSettingsScreen(this);

        input_processor = new GameInputProcessor(game_screen);
        game_screen.input_processor = input_processor;
        map_input_processor = new MapSettingsInputProcessor(map_edit_screen);
        map_edit_screen.input_processor = map_input_processor;


        lab_input_processor = new LabInputProcessor(lab_screen);

        //Gdx.input.setInputProcessor(map_input_processor);
        //this.setScreen(map_edit_screen);

        //Gdx.input.setInputProcessor(input_processor);
        //this.setScreen(game_screen);

        Gdx.input.setInputProcessor(lab_input_processor);
        this.setScreen(lab_screen);

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
        if (!(game_board == null)){
            game_board.dispose();
        }
    }

}
