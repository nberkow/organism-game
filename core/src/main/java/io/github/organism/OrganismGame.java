package io.github.organism;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.HashMap;
import java.util.Random;

import io.github.organism.hud.HudInputProcessor;

public class OrganismGame extends Game {

    public HudTestScreen hudTestScreen;

    public ShapeRenderer shapeRenderer;
    GameScreen gameScreen;
    MenuScreen menuScreen;
    LabScreen labScreen;

    TutorialScreen tutorialScreen;

    FileHandler fileHandler;

    MapSettingsScreen mapSettingsScreen;
    public SpriteBatch batch;
    GameBoard gameBoard;
    OrthographicCamera camera;
    FitViewport viewport;
    Random rng;

    // Game colors
    public Color backgroundColor = Color.BLACK;
    public Color foregroundColor = Color.CYAN;

    public Color [] action_colors = {Color.RED, Color.BLUE, Color.RED};
    public HashMap<Integer, BitmapFont> fonts;

    Color [] playerColors = {
        new Color(0xB91372FF),
        new Color(0xD497A7FF),
        new Color(0x6EEB83FF),
        Color.GREEN, Color.RED, Color.MAROON, Color.MAGENTA, Color.BROWN, Color.LIME,
        Color.OLIVE, Color.FIREBRICK, Color.FOREST, Color.PINK, Color.GOLDENROD,
        Color.CORAL, Color.CHARTREUSE, Color.SCARLET, Color.ORANGE, Color.SKY
    };


    public Color [] resourceColorsDark = {
        new Color(0f, 0f, 0.3f, 0f),
        new Color(0f, 0.2f, 0f, 0f),
        new Color(0.2f, 0f, 0.2f, 0f)};
    public Color [] resourceColorsBright = {
        new Color(0f, 0f, 0.5f, 0f),
        new Color(0f, 0.4f, 0f, 0f),
        new Color(0.4f, 0f, 0.2f, 0f)};
    public static final int VIRTUAL_WIDTH = 1920/2;  // Virtual resolution width
    public static final int VIRTUAL_HEIGHT = 1080/2; // Virtual resolution height

    @Override
    public void create() {
        // Initialize the camera and viewport for the virtual game world dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true); // Ensure viewport is set up
        camera.update();

        batch = new SpriteBatch();
        batch.enableBlending();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.setProjectionMatrix(this.viewport.getCamera().combined);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(this.viewport.getCamera().combined);

        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        fileHandler = new FileHandler(this);

        rng = new Random();
        rng.setSeed(10);

        fonts = new HashMap<>();
        for (int i=3;i<7;i++){
            int size = (int) Math.pow(2, i);
            fonts.put(size, new BitmapFont(Gdx.files.internal("fonts/dubai" + size + ".fnt")));
        }


        hudTestScreen = new HudTestScreen(this);
        hudTestScreen.inputProcessor = new HudInputProcessor(hudTestScreen);

        /*
        menuScreen = new MenuScreen(this);
        menuScreen.inputProcessor = new MenuInputProcessor(menuScreen);

        gameScreen = new GameScreen(this);
        gameScreen.inputProcessor = new GameInputProcessor(gameScreen);
        gameScreen.overlay.input_processor = new SettingsOverlayInputProcessor(gameScreen.overlay);

        mapSettingsScreen = new MapSettingsScreen(this);
        mapSettingsScreen.inputProcessor = new MapSettingsInputProcessor(mapSettingsScreen);

        labScreen = new LabScreen(this);
        labScreen.inputProcessor = new LabScreenInputProcessor(labScreen);
        labScreen.overlay.input_processor = new SettingsOverlayInputProcessor(labScreen.overlay);

        tutorialScreen = new TutorialScreen(this);
        tutorialScreen.inputProcessor = new TutorialInputProcessor(tutorialScreen);


        Gdx.input.setInputProcessor(mapSettingsScreen.inputProcessor);
        this.setScreen(mapSettingsScreen);

        Gdx.input.setInputProcessor(gameScreen.inputProcessor);
        this.setScreen(gameScreen);



        Gdx.input.setInputProcessor(menuScreen.inputProcessor);
        this.setScreen(menuScreen);
         */

        //Gdx.input.setInputProcessor(labScreen.inputProcessor);
        //this.setScreen(labScreen);

        Gdx.input.setInputProcessor(hudTestScreen.inputProcessor);
        this.setScreen(hudTestScreen);

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
        if (!(gameBoard == null)){
            gameBoard.dispose();
        }
    }

}
