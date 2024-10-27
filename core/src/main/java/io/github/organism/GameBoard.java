package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GameBoard implements Disposable {
 public final double EXPAND_SUBSET = 3d;
    // Visualization Settings
    final float GRID_WINDOW_HEIGHT = 1.7f;
    final float ACTION_HISTORY_HEIGHT = 5.5f;
    final float ENERGY_BAR_HEIGHT = 8.25f;
    final float GAMEPLAY_BUTTONS_HEIGHT = 14f;
    final int MAX_QUEUED_ACTIONS = 20;

    final float PLAYER_SUMMARY_X = 30;
    final float PLAYER_SUMMARY_Y = 27;

    final float PLAYER_SUMMARY_HEIGHT = 40;

    public long seed = 12;

    // Game colors
    Color background_color = Color.BLACK;
    Color foreground_color = Color.CYAN;
    Color resource_color = new Color(0f, 0.5f, 0f, 0.5f);

    Color [] colors = {Color.RED, Color.BLUE, Color.GREEN};

    BitmapFont font;

    // Gameplay parameters

    public final double ASSIMILATION_THRESHOLD = 0.37d;
    // percent of total energy transferred per action (transfers faster when more full);
    public final double ENERGY_PER_ACTION = 0.5d;
    public final double MAX_ENERGY = 6d;

    public final double MIN_DELTA = 10e-9f;
    public final int HEX_MAX_ENERGY = 2;

    // Gameplay
    HashMap<String, Player> players = new HashMap<>();
    GridWindow grid_window;
    UniversalHexGrid universal_grid;
    GameplayButtons input_panel;

    ArrayList<PlayerSummaryDisplay> player_summary_displays;
    HashMap<String, EnergyBar> human_player_energy_bars;
    HashMap<String, ActionQueueBar> human_player_action_queue_bars;

    ArrayList<String> human_player_names;
    ArrayList<String> bot_player_names;
    ArrayList<String> all_player_names;
    ShapeRenderer shape_renderer;

    SpriteBatch batch;

    GradientSet gradient_set;

    GameConfig config;

    int radius = 12;
    Random rng;
    Main main;

    public GameBoard(Main main, GameConfig cfg) {
        this.main = main;
        this.config = cfg;

        shape_renderer = new ShapeRenderer();

        rng = new Random();
        rng.setSeed(seed);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(foreground_color);
        font.getData().setScale(1f);

        // Initialize other game objects here
        gradient_set = new GradientSet(this, radius, 3);
        universal_grid = new UniversalHexGrid(this, radius);
        grid_window = new GridWindow(this, 2);
        input_panel = new GameplayButtons(this, config.human_players);
        human_player_energy_bars = new HashMap<>();
        human_player_action_queue_bars = new HashMap<>();
        player_summary_displays = new ArrayList<>();
        human_player_names = new ArrayList<>();
        bot_player_names = new ArrayList<>();
        all_player_names = new ArrayList<>();

        // Setup the players based on the config
        create_human_players();
        create_bot_players();
        create_player_summary_displays();
        ArrayList<int[]> starting_coords = randomize_starting_coords();
        assign_starting_hexes(starting_coords);

    }

    private void assign_starting_hexes(ArrayList<int[]> startingCoords) {

        int i = 0;
        for (String player_name : players.keySet()) {
            Organism organism = players.get(player_name).get_organism();
            int [] coords = startingCoords.get(i);
            organism.create_assimilated_hex(coords[0], coords[1], coords[2]);
            i ++;
        }
    }

    public ArrayList<int[]> randomize_starting_coords(){

        // randomly select a valid hex
        int r = (int) Math.floor(radius * rng.nextFloat() + radius / 2d);
        int a = rng.nextInt(r * 2 + 1) - r;
        int min_b = Math.max(-r, -r-a);
        int max_b = Math.min(r, r-a);
        int b = rng.nextInt(max_b * 2 + 1) + min_b;
        int c = -a - b;

        ArrayList<int []> starting_coords = new ArrayList<>();

        starting_coords.add(new int[] {a, b, c});
        starting_coords.add(new int[] {b, c, a});
        starting_coords.add(new int[] {c, a, b});

        if (config.human_players + config.bot_players == 6){
            starting_coords.add(new int[] {c, b, a});
            starting_coords.add(new int[] {b, a, c});
            starting_coords.add(new int[] {a, c, b});
        }

        return starting_coords;
    }

    public void create_human_players(){
        for (int p=0; p<config.human_players; p++){
            String name = "human " + p;
            Organism organism = new Organism(this);
            Player player = new IO_Player(
                this,
                name,
                organism,
                false
            );
            organism.player = player;
            players.put(name, player);
            human_player_energy_bars.put(name, new EnergyBar(this, name, main.VIRTUAL_WIDTH / 2f));
            human_player_action_queue_bars.put(name, new ActionQueueBar(this, name, main.VIRTUAL_WIDTH / 2f));
            human_player_names.add(name);
            all_player_names.add(name);
        }
    }

    public void create_bot_players(){
        for (int b=0; b<config.bot_players; b++){
            String name = "bot " + b;
            HMM m = new HMM(this);
            m.init(.5);
            Organism organism = new Organism(this);
            HMM_Bot player = new HMM_Bot(
                this,
                name,
                organism,
                m
            );
            organism.player = player;
            players.put(name, player);
            bot_player_names.add(name);
            all_player_names.add(name);
        }
    }

    public void create_player_summary_displays(){

        float y = PLAYER_SUMMARY_Y;
        for (Player p : players.values()){
            PlayerSummaryDisplay display = new PlayerSummaryDisplay(
                this, p,
                PLAYER_SUMMARY_X, y) ;
            y += PLAYER_SUMMARY_HEIGHT;
            player_summary_displays.add(display);
        }
    }

    public void render() {
        ScreenUtils.clear(background_color);
        batch.setProjectionMatrix(this.main.viewport.getCamera().combined);
        shape_renderer.setProjectionMatrix(this.main.viewport.getCamera().combined);
        grid_window.render();

        input_panel.render();
        for (String a : human_player_action_queue_bars.keySet()){
            human_player_action_queue_bars.get(a).render();
        }
        for (String e : human_player_energy_bars.keySet()){
            human_player_energy_bars.get(e).render();
        }

        for (PlayerSummaryDisplay p : player_summary_displays) {
            p.render();
        }
    }

    @Override
    public void dispose() {
        // Dispose of resources properly
        if (shape_renderer != null) shape_renderer.dispose();
        // Clean up any other resources like textures or sounds here
    }

    public void enqueue_action(String player_name, int button_val) {
        // run one of the three actions depending on the button val
        Player player = players.get(player_name);
        player.queue_move(button_val);
    }

    public void run_actions_from_queue(){
        for (String name : players.keySet()){
            Player player = players.get(name);

            Organism player_organism = player.get_organism();
            Integer queue_val = player.get_move();

            if (queue_val == 0){
                player_organism.extract();
            } else {
            if (queue_val == 1){
                player_organism.expand();
            } else {
            if (queue_val == 2) {
                player_organism.explore();
            }}}

        }
    }
}
