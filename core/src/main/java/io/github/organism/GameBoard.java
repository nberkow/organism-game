package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;

public class GameBoard implements Disposable {

    // Visualization Settings
    final float GRID_WINDOW_HEIGHT = 1.5f;
    final float ENERGY_BAR_HEIGHT = 4.25f;
    final float ACTION_HISTORY_HEIGHT = 3f;
    final float GAMEPLAY_BUTTONS_HEIGHT = 6.5f;

    final float PLAY_PAUSE_HEIGHT = .5f;

    Color background_color = Color.BLACK;
    Color foreground_color = Color.CYAN;

    Color resource_color = new Color(0f, 0.6f, 0f, 1);

    // Gameplay parameters

    public final double ASSIMILATION_THRESHOLD = 0.37d;

    // percent of total energy transferred per action (transfers faster when more full);
    public final double ENERGY_PER_ACTION = .5d;

    public final double MIN_DELTA = 10e-5f;

    public final double HEX_MAX_ENERGY = 1;

    // Gameplay
    HashMap<String, Organism> players = new HashMap<>();

    Color [] colors = {Color.RED, Color.BLUE, Color.GREEN};
    GridWindow grid_window;
    UniversalHexGrid grid;
    Organism player_organism;
    GameplayButtons input_panel;
    EnergyBar energy_bar;
    ActionHistoryBar action_history_bar;
    ShapeRenderer shape_renderer;

    Main main;

    public GameBoard(Main main) {
        this.main = main;

        shape_renderer = new ShapeRenderer();

        // Initialize other game objects here
        grid = new UniversalHexGrid(5);
        grid_window = new GridWindow(this, 2);
        input_panel = new GameplayButtons(this);
        action_history_bar = new ActionHistoryBar(this);
        energy_bar = new EnergyBar(this);

        // starting state
        player_organism = new Organism(this);
        players.put("player1", player_organism);
        player_organism.create_assimilated_hex(-2, 2, 0);
        player_organism.create_assimilated_hex(-1, -1, 2);
        player_organism.create_assimilated_hex(2, 0, -2);
        player_organism.create_assimilated_hex(-1, 0, 1);
        player_organism.expand();
    }

    public void render() {
        ScreenUtils.clear(background_color);

        shape_renderer.setProjectionMatrix(main.camera.combined); // Ensure ShapeRenderer uses the correct projection
        grid_window.render();
        input_panel.render();
        action_history_bar.render();
        energy_bar.render();
    }

    @Override
    public void dispose() {
        // Dispose of resources properly
        if (shape_renderer != null) shape_renderer.dispose();
        // Clean up any other resources like textures or sounds here
    }

    public void apply_action(int button_val, double actions_staged) {
        // run one of the three actions depending on the button val
        action_history_bar.add_action(button_val);
        if (button_val == 0){
            player_organism.extract();
        }
    }
}
