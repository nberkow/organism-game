package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Disposable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GameBoard implements Disposable {


    public static final float DEFAULT_SPEED = 2f;
    // Visualization Settings
    final float GRID_WINDOW_HEIGHT = 1.7f;
    public boolean show_diplomacy;
    public boolean show_player_summary;
    float hex_side_len;
    float center_x;
    float center_y;
    float grid_window_y;
    static final int MAX_QUEUED_ACTIONS = 12;
    static final float PLAYER_SUMMARY_X = 30;
    static final float PLAYER_SUMMARY_Y = 400;
    final float PLAYER_SUMMARY_HEIGHT = 40;
    public long seed;

    public Screen screen;

    SettingsManager settings_manager;

    GameOrchestrator orchestrator;

    DiplomacyGraph diplomacy_graph;

    // Gameplay parameters
    public static final int DEFAULT_STARTING_ENERGY = 6;

    // Gameplay
    HashMap<Point, Player> players = new HashMap<>();

    GridWindow grid_window;
    UniverseMap universe_map;
    ArrayList<PlayerSummaryDisplay> player_summary_displays;
    ArrayList<Point> human_player_ids;
    ArrayList<Point> bot_player_ids;
    ArrayList<Point> all_player_ids;
    PlayerStartAssigner player_start_assigner;
    ResourceDistributor resource_distributor;
    VoidDistributor void_distributor;
    GameConfig config;
    int radius;
    Random rng;
    OrganismGame game;
    MoveLogger move_logger;

    public GameBoard(OrganismGame g, GameConfig cfg, Screen scr) {
        game = g;
        config = cfg;
        screen = scr;
        show_diplomacy = false;
        show_player_summary = false;

        if (screen instanceof LabScreen){
            settings_manager = ((LabScreen) screen).settings_manager;
        }
        if (screen instanceof GameScreen){
            settings_manager = ((GameScreen) screen).settings_manager;
        }

        seed = config.seed;
        radius = config.radius;
        grid_window_y = GRID_WINDOW_HEIGHT;
        move_logger = null;

        hex_side_len = config.map_view_size_param/radius; // starting default
        center_x = this.game.VIRTUAL_WIDTH / 2f;
        center_y = this.game.VIRTUAL_HEIGHT / grid_window_y;

        rng = new Random();
        rng.setSeed(seed);

        // Initialize other game objects here
        universe_map = new UniverseMap(this, radius);

        diplomacy_graph = new DiplomacyGraph(this.game, this);

        player_start_assigner = new PlayerStartAssigner(this);
        resource_distributor = new ResourceDistributor(this);
        void_distributor = new VoidDistributor(this);
        grid_window = new GridWindow(this, 2);

        player_summary_displays = new ArrayList<>();
        human_player_ids = new ArrayList<>();
        bot_player_ids = new ArrayList<>();
        all_player_ids = new ArrayList<>();

    }

    public void set_orchestrator(GameOrchestrator o) {
        orchestrator = o;
    }


    public void create_bot_player(String name, Point player_id, Color color, HMM model){

        int index = all_player_ids.size();

        Organism organism = new Organism(this);
        BotPlayer player = new BotPlayer(
            this,
            name,
            index,
            player_id,
            organism,
            model,
            color
        );

        organism.player = player;
        players.put(player_id, player);
        bot_player_ids.add(player_id);
        all_player_ids.add(player_id);

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

        grid_window.render();



        if (show_player_summary) {
            for (PlayerSummaryDisplay p : player_summary_displays) {
                p.render();
            }
        }

        if (show_diplomacy) {
            diplomacy_graph.render();
        }
    }

    @Override
    public void dispose() {
        grid_window.dispose();
        universe_map.dispose();
        orchestrator.dispose();
        diplomacy_graph.dispose();
        players.clear();
        human_player_ids.clear();
        bot_player_ids.clear();
        all_player_ids.clear();
        player_start_assigner = null;
        resource_distributor = null;
        void_distributor = null;
    }
}
