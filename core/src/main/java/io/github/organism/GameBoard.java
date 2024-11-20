package io.github.organism;

import static java.lang.Math.max;
import static java.lang.Math.min;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameBoard implements Disposable {


    // Visualization Settings
    final float GRID_WINDOW_HEIGHT = 1.7f;
    public boolean show_data;

    float grid_window_y;
    final int MAX_QUEUED_ACTIONS = 20;
    final float PLAYER_SUMMARY_X = 30;
    final float PLAYER_SUMMARY_Y = 400;
    final float PLAYER_SUMMARY_HEIGHT = 40;
    public long seed;

    // Game colors
    Color background_color = Color.BLACK;
    Color foreground_color = Color.CYAN;

    Color [] colors = {Color.RED, Color.BLUE, Color.GREEN};

    BitmapFont font;

    // Gameplay parameters
    public static final int DEFAULT_STARTING_ENERGY = 6;

    // Gameplay
    HashMap<String, Player> players = new HashMap<>();

    Color [] player_colors = {
        new Color(0xB91372FF),
        new Color(0xD497A7FF),
        new Color(0x6EEB83FF),
        new Color(0xE4FF1AFF),
        new Color(0xE8AA14FF),
        new Color(0xFF5714FF)
    };

    Color [] player_light_colors = {
        new Color(0xB9137255),
        new Color(0xD497A755),
        new Color(0x6EEB8355),
        new Color(0xE4FF1A55),
        new Color(0xE8AA1455),
        new Color(0xFF571455)
    };

        Color [] resource_colors_dark = {
        new Color(0f, 0f, 0.3f, 0f),
        new Color(0f, 0.2f, 0f, 0f),
        new Color(0.2f, 0f, 0.2f, 0f)};
    Color [] resource_colors_bright = {
        new Color(0f, 0f, 0.5f, 0f),
        new Color(0f, 0.4f, 0f, 0f),
        new Color(0.4f, 0f, 0.2f, 0f)};
    GridWindow grid_window;
    UniverseMap universe_map;
    ArrayList<PlayerSummaryDisplay> player_summary_displays;
    ArrayList<String> human_player_names;
    ArrayList<String> bot_player_names;
    ArrayList<String> all_player_names;
    ShapeRenderer shape_renderer;

    ResourceDistributor resource_distributor;
    PlayerHud player1_hud;
    PlayerHud player2_hud;

    SpriteBatch batch;

    GameConfig config;

    float hex_side_len;
    float center_x;
    float center_y;

    int radius;
    Random rng;
    OrganismGame game;

    public GameBoard(OrganismGame game, GameConfig cfg) {
        this.game = game;
        this.config = cfg;
        this.batch = game.batch;
        seed = config.seed;
        radius = config.radius;
        grid_window_y = GRID_WINDOW_HEIGHT;
        show_data = true;

        shape_renderer = game.shape_renderer;
        hex_side_len = config.map_view_size_param/radius; // starting default
        center_x = this.game.VIRTUAL_WIDTH / 2f;
        center_y = this.game.VIRTUAL_HEIGHT / grid_window_y;

        rng = new Random();
        rng.setSeed(seed);

        font = new BitmapFont();
        font.setColor(foreground_color);

        // Initialize other game objects here
        universe_map = new UniverseMap(this, radius);
        resource_distributor = new ResourceDistributor(this);
        grid_window = new GridWindow(this, 2);

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
        if (!human_player_names.isEmpty()) {
            player1_hud = new PlayerHud(this, players.get(human_player_names.get(0)), false);
        }
        if (human_player_names.size() > 1) {
            player2_hud = new PlayerHud(this, players.get(human_player_names.get(1)),  true);
        }
    }

    private void assign_starting_hexes(ArrayList<int[]> starting_coords) {
        int i = 0;
        for (String player_name : players.keySet()) {
            Organism organism = players.get(player_name).get_organism();
            int [] coords = starting_coords.get(i);
            organism.claim_hex(coords[0], coords[1], coords[2]);
            MapHex hex = (MapHex) universe_map.hex_grid.get_pos(coords[0], coords[1], coords[2]).content;
            hex.add_resource(i%3, 3);
            i ++;
        }
    }

    public ArrayList<int[]> randomize_starting_coords(){

        // randomly select a valid hex
        int r = radius / 2;
        int a = rng.nextInt(r + 1) - r/2;
        int min_b = max(-r - a, -r);
        int max_b = min(r - a, r);
        int b = rng.nextInt(max_b - min_b) + min_b;
        int c = -a - b;

        ArrayList<int []> starting_coords = new ArrayList<>();

        starting_coords.add(new int[] {a * 2, b * 2, c * 2});
        starting_coords.add(new int[] {b * 2, c * 2, a * 2});
        starting_coords.add(new int[] {c * 2, a * 2, b * 2});

        if (config.human_players + config.bot_players == 6){
            starting_coords.add(new int[] {-a * 2, -b * 2, -c * 2});
            starting_coords.add(new int[] {-b * 2, -c * 2, -a * 2});
            starting_coords.add(new int[] {-c * 2, -a * 2, -b * 2});
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
            organism.color = player_colors[p];
            organism.player = player;
            players.put(name, player);

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
            BotPlayer player = new BotPlayer(
                this,
                name,
                organism,
                m
            );
            organism.color = player_colors[b + config.human_players];
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
        batch.setProjectionMatrix(this.game.viewport.getCamera().combined);
        shape_renderer.setProjectionMatrix(this.game.viewport.getCamera().combined);
        grid_window.render();

        if (player1_hud != null) {
            player1_hud.render();
        }

        if (player2_hud != null) {
            player2_hud.render();
        }

        if (show_data) {
            for (PlayerSummaryDisplay p : player_summary_displays) {
                p.render();
            }
        }
    }

    @Override
    public void dispose() {
        // Dispose of resources properly
        if (shape_renderer != null) shape_renderer.dispose();
        // Clean up any other resources like textures or sounds here
    }
}
