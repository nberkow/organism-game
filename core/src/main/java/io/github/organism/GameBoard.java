package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Disposable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import io.github.organism.map.UniverseMap;
import io.github.organism.player.BotPlayer;
import io.github.organism.player.Player;

public class GameBoard implements Disposable {


    public static final float DEFAULT_SPEED = 2f;
    // Visualization Settings
    public final float GRID_WINDOW_HEIGHT = 1.7f;
    public boolean showDiplomacy;
    public boolean showPlayerSummary;
    public float hexSideLen;
    public float centerX;
    public float centerY;
    float grid_window_y;
    static final float PLAYER_SUMMARY_X = 30;
    static final float PLAYER_SUMMARY_Y = 400;
    final float PLAYER_SUMMARY_HEIGHT = 40;
    public long seed;

    public GameSession session;

    SettingsManager settings_manager;

    GameOrchestrator orchestrator;

    DiplomacyGraph diplomacyGraph;

    // Gameplay parameters
    public static final int DEFAULT_STARTING_ENERGY = 6;

    // Gameplay
    HashMap<Point, Player> players = new HashMap<>();

    HashMap<Point, ArrayList<ExpandEdge>> expandEdges = new HashMap<>();

    public GridWindow gridWindow;
    public UniverseMap universeMap;
    public ArrayList<PlayerSummaryDisplay> playerSummaryDisplays;
    public ArrayList<Point> humanPlayerIds;
    public ArrayList<Point> bot_player_ids;
    public ArrayList<Point> allPlayerIds;
    PlayerStartAssigner playerStartAssigner;
    ResourceDistributor resourceDistributor;
    VoidDistributor voidDistributor;
    public GameConfig config;
    int radius;
    public Random rng;
    public OrganismGame game;
    MoveLogger move_logger;

    public GameBoard(OrganismGame g, GameConfig cfg, GameSession gs) {
        game = g;
        config = cfg;
        session = gs;
        showDiplomacy = false;
        showPlayerSummary = false;

        if (session.getScreen() instanceof LabScreen){
            settings_manager = ((LabScreen) session.getScreen()).settingsManager;
        }
        if (session.getScreen() instanceof GameScreen){
            settings_manager = ((GameScreen) session.getScreen()).settings_manager;
        }

        seed = config.seed;
        radius = config.radius;
        grid_window_y = GRID_WINDOW_HEIGHT;
        move_logger = null;

        hexSideLen = config.map_view_size_param/radius; // starting default
        centerX = OrganismGame.VIRTUAL_WIDTH / 2f;
        centerY = OrganismGame.VIRTUAL_HEIGHT / grid_window_y;

        rng = new Random();
        rng.setSeed(seed);

        // Initialize other game objects here
        universeMap = new UniverseMap(this, radius);

        diplomacyGraph = new DiplomacyGraph(this.game, this);

        playerStartAssigner = new PlayerStartAssigner(this);
        resourceDistributor = new ResourceDistributor(this);
        voidDistributor = new VoidDistributor(this);
        gridWindow = new GridWindow(this, 2);

        playerSummaryDisplays = new ArrayList<>();
        humanPlayerIds = new ArrayList<>();
        bot_player_ids = new ArrayList<>();
        allPlayerIds = new ArrayList<>();

    }

    public void set_orchestrator(GameOrchestrator o) {
        orchestrator = o;
    }


    public void create_bot_player(String name, Point playerId, Color color, Model model){

        int index = allPlayerIds.size();

        Organism organism = new Organism(this);
        BotPlayer player = new BotPlayer(
            this,
            name,
            index,
            playerId,
            organism,
            model,
            color
        );

        organism.player = player;
        players.put(playerId, player);
        bot_player_ids.add(playerId);
        allPlayerIds.add(playerId);

    }

    public void createPlayerSummaryDisplays(){

        float y = PLAYER_SUMMARY_Y;
        for (Player p : players.values()){
            PlayerSummaryDisplay display = new PlayerSummaryDisplay(
                this, p,
                PLAYER_SUMMARY_X, y) ;
            y += PLAYER_SUMMARY_HEIGHT;
            playerSummaryDisplays.add(display);
        }
    }

    public int count_resources(){
        return universeMap.hexGrid.countResources();
    }

    public void logic() {
        //
    }

    public void render() {

        logic();

        ScreenUtils.clear(game.backgroundColor);

        gridWindow.render();
        if (showPlayerSummary) {
            for (PlayerSummaryDisplay p : playerSummaryDisplays) {
                p.render();
            }
        }

        if (showDiplomacy) {
            diplomacyGraph.render();
        }

        for (Point p : expandEdges.keySet()) {
            for (ExpandEdge e : expandEdges.get(p)) {
                e.render();
            }
        }
    }

    @Override
    public void dispose() {
        gridWindow.dispose();
        universeMap.dispose();
        orchestrator.dispose();
        diplomacyGraph.dispose();
        players.clear();
        humanPlayerIds.clear();
        bot_player_ids.clear();
        allPlayerIds.clear();
        playerStartAssigner = null;
        resourceDistributor = null;
        voidDistributor = null;
    }
}
