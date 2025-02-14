package io.github.organism;

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
    public boolean show_data;
    public float speed;

    float hex_side_len;
    float center_x;
    float center_y;
    float grid_window_y;
    final int MAX_QUEUED_ACTIONS = 12;
    static final float PLAYER_SUMMARY_X = 30;
    static final float PLAYER_SUMMARY_Y = 400;
    final float PLAYER_SUMMARY_HEIGHT = 40;
    public long seed;

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
    PlayerHud player1_hud;
    PlayerHud player2_hud;
    GameConfig config;
    int radius;
    Random rng;
    OrganismGame game;

    MoveLogger move_logger;

    public GameBoard(OrganismGame game, GameConfig cfg) {
        this.game = game;
        this.config = cfg;

        seed = config.seed;
        radius = config.radius;
        grid_window_y = GRID_WINDOW_HEIGHT;
        move_logger = null;
        show_data = true;

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

        // Setup the players based on the config

        //create_human_players();
        //create_bot_players();

        /*if (!human_player_ids.isEmpty()) {
            player1_hud = new PlayerHud(this, players.get(human_player_ids.get(0)), false);
        }
        if (human_player_ids.size() > 1) {
            player2_hud = new PlayerHud(this, players.get(human_player_ids.get(1)),  true);
        }*/
    }

    public void set_orchestrator(GameOrchestrator o) {
        orchestrator = o;
    }

    public void create_human_players(){
        for (int p=0; p<config.human_players; p++){
            int index = all_player_ids.size();
            Point player_id = new Point(index, 0);
            Color color = game.player_colors[index];
            String name = "human " + p;
            Organism organism = new Organism(this);
            Player player = new IO_Player(
                this,
                name,
                index,
                player_id,
                organism,
                false,
                color
            );
            organism.player = player;
            players.put(player_id, player);

            human_player_ids.add(player_id);
            all_player_ids.add(player_id);
        }
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

        diplomacy_graph.render();
    }

    @Override
    public void dispose() {
        grid_window.dispose();
        universe_map.dispose();
        orchestrator.dispose();
        diplomacy_graph.dispose();
        for (Player p : players.values()) {
            p.dispose();
        }
        human_player_ids.clear();
        bot_player_ids.clear();
        all_player_ids.clear();
        game.shape_renderer = null;
        player_start_assigner = null;
        resource_distributor = null;
        void_distributor = null;
        player1_hud = null;
        player2_hud = null;
        game.batch = null;
    }
}
