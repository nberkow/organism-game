package io.github.organism;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ArcadeLoop implements GameSession {

    float mapCenterX;
    float mapCenterY;
    GameConfig gameCfg;

    SettingsOverlay menuOverlay;
    SettingsOverlay gameOverlay;
    int iterations;

    boolean showSummaryScreen;
    boolean nextRoundBegin;
    boolean kill = false;

    float betweenRoundPause = 2f;
    float betweenRoundPauseTimer;

    HashMap<Point, String> playerNames;
    HashMap<Point, Color> tournamentPlayerColors;

    ArrayList<Color> availableColors;
    int playerPrimaryIndex;
    String[] numerals = {"I", "II", "III", "IV", "V",
                        "VI", "VII", "VIII", "IX", "X",
                        "XI", "XII", "XIII", "XIV", "XV",
                        "XVI", "XVII", "XVIII", "XIX", "XX"};

    String[] playerNamesArray = {
        "Serpula lacrymans",
        "Turkey tail",
        "Hoof fungus",
        "Chicken of the woods",
        "Red-Belted Conk",
        "Honey fungus",
        "Splitgill mushroom",
        "Artist's bracket",
        "Oyster Mushroom",
        "Pleurotus",
        "Coniophora",
        "Dyer's polypore",
        "Shiitake",
        "Kretzschmaria deusta",
        "Sulphur tuft",
        "Porodaedalea pini",
        "Donkioporia expansa",
        "Northern cinnabar polypore",
        "Fibroporia vaillantii",
        "Dead man's fingers",
        "Phanerodontia chrysosporium",
        "Chaetomium",
        "China root",
        "Ceratocystis",
        "Bondarzewia berkeleyi"
    };

    HashMap<Point, Model> modelPool;
    HashMap<Point, Point> winRecords;
    int currentIteration;
    OrganismGame game;
    GameBoard currentGame;
    GameOrchestrator currentGameOrchestrator;
    RoundSummary round_summary;

    Screen currentScreen;

    public ArcadeLoop(OrganismGame g) {
        game = g;
        modelPool = new HashMap<>();
        winRecords = new HashMap<>();

        setup_overlays();
        loadModels();

        availableColors = new ArrayList<>();

        tournamentPlayerColors = new HashMap<>();
        playerNames = new HashMap<>();

        showSummaryScreen = false;
        nextRoundBegin = true;

        mapCenterX = OrganismGame.VIRTUAL_WIDTH / 2f;
        mapCenterY = OrganismGame.VIRTUAL_HEIGHT / 2f;
    }

    private void setup_overlays() {

        gameCfg = game.fileHandler.read_cfg("kingdoms", "map");

        float overlay_w = OrganismGame.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (OrganismGame.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = OrganismGame.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (OrganismGame.VIRTUAL_HEIGHT - overlay_h) / 2f;

        gameOverlay = new SettingsOverlay(game, game.gameScreen, overlay_x, overlay_y, overlay_w, overlay_h);
        gameOverlay.setupSliders();
        gameOverlay.setupButtons();
        gameCfg.gameplaySettings = gameOverlay.savedSettings;

    }

    private void create_players_from_model_pool(int n) {
        ArrayList<Point> player_ids = new ArrayList<>(modelPool.keySet());
        Collections.shuffle(player_ids, game.rng);

        for (int i=0; i<n; i++) {
            Point player_id = player_ids.get(i);
            Model model = modelPool.get(player_id);
            String name = playerNamesArray[player_id.x % playerNamesArray.length] + " " + numerals[player_id.y % numerals.length];

            Color color;
            if (tournamentPlayerColors.containsKey(player_id)){
                color = tournamentPlayerColors.get(player_id);
            } else {
                color = availableColors.remove(0);
            }

            currentGame.create_bot_player(name, player_id, color, model);
            playerNames.put(player_id, name);
            tournamentPlayerColors.put(player_id, color);
        }

        // reset diplomacy with newly created players
        currentGame.diplomacyGraph = new DiplomacyGraph(game, currentGame);
    }

    public void setup(int n) {
        game.gameScreen.ioPlayerNames = new ArrayList<>();
        game.gameScreen.ioPlayerIds = new ArrayList<>();

        betweenRoundPauseTimer = 2;

        gameCfg.humanPlayers = n;
        gameCfg.botPlayers = 3 - n;
        currentIteration = 1;
        playerPrimaryIndex = 0;
        currentScreen = game.gameScreen;

        create_game_board();

        create_players_from_model_pool(gameCfg.botPlayers);
        create_human_players();
        createPlayerStarts();

        currentGame.createPlayerSummaryDisplays();
        currentGame.showPlayerSummary = true;

        currentGame.diplomacyGraph = new DiplomacyGraph(game, currentGame);
        currentGame.showDiplomacy = true;

        currentGameOrchestrator.update_speed(1);
        currentGameOrchestrator.run();
    }


    public void create_game_board() {

        if (availableColors.size() < 3){
            availableColors.addAll(Arrays.asList(game.playerColors).subList(2, game.playerColors.length));
        }

        currentGame = new GameBoard(game, gameCfg, currentScreen);
        currentGame.voidDistributor.distribute();
        currentGame.resourceDistributor.distribute();

        currentGameOrchestrator = new GameOrchestrator(currentGame);
        currentGame.set_orchestrator(currentGameOrchestrator);

        currentGame.center_x = mapCenterX;
        currentGame.center_y = mapCenterY;
    }

    public void createPlayerStarts() {
        int sc = (int) Math.floor(Math.pow(gameCfg.radius, gameCfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> startingCoords = currentGame.player_start_assigner.randomizeStartingCoords();
            currentGame.player_start_assigner.assignStartingHexes(startingCoords);
        }
    }

    public void loadModels() {
        for (Model model : game.fileHandler.load_models()){
            Point p = new Point(playerPrimaryIndex, 0);
            model.setPlayerTournamentId(p);
            modelPool.put(p, model);
            playerPrimaryIndex++;
        }
    }

    public void run_arcade_loop() {
        System.out.println("first iteration");
        currentGameOrchestrator.update_speed(gameCfg.gameplaySettings.get("speed"));
        currentGameOrchestrator.run();
    }

    private void finish_this_round(Point winner_id) {

        System.out.println("iteration: " + currentIteration + "/" + iterations);

        for (Point p : currentGame.players.keySet()) {
            Point rec = winRecords.get(p);
            if (p == winner_id){
                rec.x += 1;
            }
            else {
                rec.y += 1;
            }
            winRecords.put(p, rec);
        }

        round_summary.set_winner(winner_id);
        showSummaryScreen = true;

    }

    private void setup_next_round() {

        currentGame.dispose();
        currentGameOrchestrator.dispose();

        create_game_board();
        create_human_players();
        create_bot_players();
        createPlayerStarts();
        currentGame.createPlayerSummaryDisplays();
        currentGameOrchestrator.update_speed(gameCfg.gameplaySettings.get("speed"));
        currentGameOrchestrator.run();
    }


    public void create_human_players(){

        for (int p = 0; p< gameCfg.humanPlayers; p++){
            Point playerId = new Point(-1, p);

            Color color;
            if (tournamentPlayerColors.containsKey(playerId)){
                color = tournamentPlayerColors.get(playerId);
            } else {
                color = availableColors.remove(0);
                tournamentPlayerColors.put(playerId, color);
            }

            String name = "Player " + (p + 1);
            Organism organism = new Organism(currentGame);
            Player player = new IO_Player(
                currentGame,
                name,
                p,
                playerId,
                organism,
                false,
                color
            );
            organism.player = player;
            currentGame.players.put(playerId, player);
            game.gameScreen.add_player(player, p==1);

            currentGame.humanPlayerIds.add(playerId);
            currentGame.allPlayerIds.add(playerId);
        }
    }
    private void create_bot_players() {

        /*
        randomly select 3 models from the pool

        use these to create 3 players on the current game board

         */


        ArrayList<Point> player_ids = new ArrayList<>(modelPool.keySet());
        Collections.shuffle(player_ids, game.rng);

        for (int i=0; i<3; i++) {
            Point player_id = player_ids.get(i);
            Model model = modelPool.get(player_id);
            String name = playerNamesArray[player_id.x % playerNamesArray.length] + " " + numerals[player_id.y % numerals.length];

            Color color;
            if (tournamentPlayerColors.containsKey(player_id)){
                color = tournamentPlayerColors.get(player_id);
            } else {
                color = availableColors.remove(0);
            }

            currentGame.create_bot_player(name, player_id, color, model);
            playerNames.put(player_id, name);
            tournamentPlayerColors.put(player_id, color);
        }

        // reset diplomacy with newly created players
        currentGame.diplomacyGraph = new DiplomacyGraph(game, currentGame);
    }


    public void logic(){

    }

    private void finish_arcade_loop() {
    }

    public void draw(){
        currentGame.game.camera.update();
        currentGame.render();
        if (showSummaryScreen & betweenRoundPause > 0 ) {
            round_summary.render();
        }
    }

    public void render(){

        if (kill) {
            dispose();
        }
        else {
            logic();
            draw();
        }
    }

    public void dispose() {
        modelPool.clear();
        winRecords.clear();
        currentGame.dispose();
    }

    /**
     * @return
     */
    @Override
    public InputProcessor getInputProcessor() {
        GameScreen g = (GameScreen) currentScreen;
        return g.inputProcessor;
    }
}
