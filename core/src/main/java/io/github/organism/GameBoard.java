package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameBoard implements Disposable {


    // Visualization Settings
    final float GRID_WINDOW_HEIGHT = 1.7f;
    public boolean show_data;
    float hex_side_len;
    float center_x;
    float center_y;
    float grid_window_y;
    final int MAX_QUEUED_ACTIONS = 20;
    final float PLAYER_SUMMARY_X = 30;
    final float PLAYER_SUMMARY_Y = 400;
    final float PLAYER_SUMMARY_HEIGHT = 40;
    public long seed;

    GameOrchestrator orchestrator;

    // Gameplay parameters
    public static final int DEFAULT_STARTING_ENERGY = 6;

    // Gameplay
    HashMap<String, Player> players = new HashMap<>();

    GridWindow grid_window;
    UniverseMap universe_map;
    ArrayList<PlayerSummaryDisplay> player_summary_displays;
    ArrayList<String> human_player_names;
    ArrayList<String> bot_player_names;
    ArrayList<String> all_player_names;
    ShapeRenderer shape_renderer;
    PlayerStartAssigner player_start_assigner;
    ResourceDistributor resource_distributor;
    VoidDistributor void_distributor;
    PlayerHud player1_hud;
    PlayerHud player2_hud;
    SpriteBatch batch;
    GameConfig config;
    int radius;
    Random rng;
    OrganismGame game;

    MoveLogger move_logger;

    public GameBoard(OrganismGame game, GameConfig cfg) {
        this.game = game;
        this.config = cfg;
        this.batch = game.batch;
        seed = config.seed;
        radius = config.radius;
        grid_window_y = GRID_WINDOW_HEIGHT;
        move_logger = null;
        show_data = true;

        shape_renderer = game.shape_renderer;
        hex_side_len = config.map_view_size_param/radius; // starting default
        center_x = this.game.VIRTUAL_WIDTH / 2f;
        center_y = this.game.VIRTUAL_HEIGHT / grid_window_y;

        rng = new Random();
        rng.setSeed(seed);

        // Initialize other game objects here
        universe_map = new UniverseMap(this, radius);
        player_start_assigner = new PlayerStartAssigner(this);
        resource_distributor = new ResourceDistributor(this);
        void_distributor = new VoidDistributor(this);
        grid_window = new GridWindow(this, 2);

        player_summary_displays = new ArrayList<>();
        human_player_names = new ArrayList<>();
        bot_player_names = new ArrayList<>();
        all_player_names = new ArrayList<>();

        // Setup the players based on the config

        create_human_players();
        create_bot_players();
        create_player_summary_displays();
        if (!human_player_names.isEmpty()) {
            player1_hud = new PlayerHud(this, players.get(human_player_names.get(0)), false);
        }
        if (human_player_names.size() > 1) {
            player2_hud = new PlayerHud(this, players.get(human_player_names.get(1)),  true);
        }
    }

    public void set_orchestrator(GameOrchestrator o) {
        orchestrator = o;
    }

    public void create_human_players(){
        for (int p=0; p<config.human_players; p++){
            int index = all_player_names.size();
            Color color = game.player_colors[index];
            String name = "human " + p;
            Organism organism = new Organism(this);
            Player player = new IO_Player(
                this,
                name,
                index,
                organism,
                false,
                color
            );
            organism.color = game.player_colors[p];
            organism.player = player;
            players.put(name, player);

            human_player_names.add(name);
            all_player_names.add(name);
        }
    }

    public void create_bot_players(){
        for (int b=0; b<config.bot_players; b++){
            int index = all_player_names.size();
            Color color = game.player_colors[index];
            String name = "bot " + b;
            HMM m = new HMM(this, 6, 0.5f, 6);
            Organism organism = new Organism(this);
            BotPlayer player = new BotPlayer(
                this,
                name,
                index,
                organism,
                m,
                color
            );
            organism.color = game.player_colors[b + config.human_players];
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

    public void logic() {
        //
    }

    public void render() {

        logic();

        ScreenUtils.clear(game.background_color);
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
