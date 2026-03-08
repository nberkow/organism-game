package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Disposable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import io.github.organism.hud.PlayerHud;
import io.github.organism.learning.SlimeRLAgent;
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
    static final float PLAYER_SUMMARY_Y = 350;
    final float PLAYER_SUMMARY_HEIGHT = 65;
    public long seed;

    public GameSession session;

    SettingsManager settings_manager;

    public GameOrchestrator orchestrator;
    public TerritoryBar territoryBar;

    // Gameplay
    HashMap<Point, Player> players = new HashMap<>();
    //HashMap<Point, HashMap<MapVertex, ExpandEdge>> expandEdges = new HashMap<>();
    public GridWindow gridWindow;
    public UniverseMap universeMap;
    public ArrayList<PlayerSummaryDisplay> playerSummaryDisplays;
    public ArrayList<Point> humanPlayerIds;
    public ArrayList<Point> botPlayerIds;
    public ArrayList<Point> allPlayerIds;
    PlayerStartAssigner playerStartAssigner;
    ResourceDistributor resourceDistributor;
    VoidDistributor voidDistributor;
    public GameConfig config;
    int radius;
    public Random rng;
    public OrganismGame game;
    MoveLogger move_logger;

    // Resource leadership tracking
    public Player[] resourceLeaders = new Player[3]; // One leader per resource type


    public GameBoard(OrganismGame g, GameConfig cfg, GameSession gs) {
        game = g;
        config = cfg;
        session = gs;
        showDiplomacy = false;
        showPlayerSummary = false;

        if (session != null) {
            if (session.getScreen() instanceof LabScreen) {
                settings_manager = ((LabScreen) session.getScreen()).settingsManager;
            }
            if (session.getScreen() instanceof GameScreen) {
                settings_manager = ((GameScreen) session.getScreen()).settings_manager;
            }
        }

        seed = config.seed;
        radius = config.radius;
        grid_window_y = GRID_WINDOW_HEIGHT;
        move_logger = null;

        hexSideLen = config.map_view_size_param / radius;
        centerX = OrganismGame.VIRTUAL_WIDTH / 2f;
        centerY = OrganismGame.VIRTUAL_HEIGHT / grid_window_y;

        rng = new Random();
        rng.setSeed(seed);

        // Initialize other game objects here
        universeMap = new UniverseMap(this, radius);

        playerStartAssigner = new PlayerStartAssigner(this);
        resourceDistributor = new ResourceDistributor(this);
        voidDistributor = new VoidDistributor(this);
        gridWindow = new GridWindow(this, 2);

        playerSummaryDisplays = new ArrayList<>();
        humanPlayerIds = new ArrayList<>();
        botPlayerIds = new ArrayList<>();
        allPlayerIds = new ArrayList<>();

        territoryBar = new TerritoryBar(game);

    }

    /*public void updateExpandEdges(float timeDelta) {
        for (Point p : expandEdges.keySet()){
            players.get(p).getOrganism().updateExpandEdges(timeDelta);
        }
    }*/

    public void setOrchestrator(GameOrchestrator o) {
        orchestrator = o;
    }




    public void createBotPlayer(String name, Point playerId, Color color, SlimeRLAgent.GameRLInterface existingAgent){

        int index = allPlayerIds.size();

        Organism organism = new Organism(this);
        PlayerHud botHud = new PlayerHud(game, session, session.getScreen(), false, false);
        BotPlayer player = new BotPlayer(
            this,
            name,
            index,
            playerId,
            organism,
            botHud,
            color,
            existingAgent
        );
        botHud.setPlayer(player);

        organism.player = player;
        players.put(playerId, player);

        botPlayerIds.add(playerId);
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

    public int countResources(){
        return universeMap.hexGrid.countResources();
    }

    public void updateResourceLeadership() {
        // Update which player leads in each resource type
        for (int resourceType = 0; resourceType < 3; resourceType++) {
            Player currentLeader = null;
            int maxCount = 0;
            int playersWithMax = 0;

            // Find the player with the most of this resource
            for (Player player : players.values()) {
                Organism organism = player.getOrganism();
                if (organism != null) {
                    int count = organism.resources[resourceType];
                    if (count > maxCount) {
                        maxCount = count;
                        currentLeader = player;
                        playersWithMax = 1;
                    } else if (count == maxCount && count > 0) {
                        playersWithMax++;
                    }
                }
            }

            // If there's a tie, no one is the leader
            if (playersWithMax > 1) {
                resourceLeaders[resourceType] = null;
            } else {
                resourceLeaders[resourceType] = currentLeader;
            }
        }
    }

    public void logic(float timeDelta) {
        if (orchestrator != null) {
            orchestrator.update(timeDelta);
        }
        updateResourceLeadership();
    }

    public void renderSummaryDisplays(float delta) {
        int i = 0;
        for (Point playerId : allPlayerIds) {
            Player player = players.get(playerId);
            if (player != null && player.getMoveSpaceControl() != null) {
                // PlayerSummaryDisplay handles its own positioning
                playerSummaryDisplays.get(i).render(delta);
            }
            i++;
        }
    }

    public void render(float delta) {

        logic(delta);

        ScreenUtils.clear(game.backgroundColor);

        gridWindow.render();

        // Render territory bar
        if (territoryBar != null) {
            territoryBar.render(this);
        }

        if (showPlayerSummary) {
            for (PlayerSummaryDisplay p : playerSummaryDisplays) {
                p.render(delta);
            }
        }

        // Render candidate vertices for the current player whose turn it is
        if (orchestrator != null) {
            Player currentPlayer = orchestrator.getCurrentPlayer();
            if (currentPlayer != null) {
                Organism o = currentPlayer.getOrganism();
                if (o != null && o.candidateVertices != null && !o.candidateVertices.isEmpty()) {
                    // Calculate total score for normalization
                    float totalScore = 0f;
                    for (float score : o.candidateVertices.values()) {
                        totalScore += score;
                    }

                    game.shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
                    Color playerColor = currentPlayer.getColor();
                    game.shapeRenderer.setColor(playerColor.r, playerColor.g, playerColor.b, 0.6f);

                    for (CandidateVertex cv : o.candidateVertices.keySet()) {
                        float probability = totalScore > 0 ? o.candidateVertices.get(cv) / totalScore : 0f;
                        cv.render(probability);
                    }

                    game.shapeRenderer.end();
                }
            }
        }
    }

    @Override
    public void dispose() {
        gridWindow.dispose();
        universeMap.dispose();
        orchestrator.dispose();
        players.clear();
        humanPlayerIds.clear();
        botPlayerIds.clear();
        allPlayerIds.clear();
        playerStartAssigner = null;
        resourceDistributor = null;
        voidDistributor = null;
    }
}
