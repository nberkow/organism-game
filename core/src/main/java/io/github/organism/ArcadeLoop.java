package io.github.organism;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import io.github.organism.player.IO_Player;
import io.github.organism.player.Player;

public class ArcadeLoop implements GameSession {

    int POOL_SIZE = 20;
    float mapCenterX;
    float mapCenterY;
    GameConfig gameCfg;
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

    HashSet<Point> botPool;
    HashMap<Point, Point> winRecords;
    int currentIteration;
    OrganismGame game;
    GameBoard currentGame;
    GameOrchestrator currentGameOrchestrator;
    RoundSummary roundSummary;
    Screen currentScreen;

    public ArcadeLoop(OrganismGame g) {
        game = g;

        botPool = new HashSet<>();
        for (int x = 0; x < POOL_SIZE; x++) {
            botPool.add(new Point(x, 0));
        }

        winRecords = new HashMap<>();

        setupOverlays();

        availableColors = new ArrayList<>();

        tournamentPlayerColors = new HashMap<>();
        playerNames = new HashMap<>();

        roundSummary = new RoundSummary(game, this);

        showSummaryScreen = false;
        nextRoundBegin = true;

        mapCenterX = OrganismGame.VIRTUAL_WIDTH / 2f;
        mapCenterY = OrganismGame.VIRTUAL_HEIGHT / 2f;

    }

    public String getPlayerName(Point p){
        return playerNames.get(p);
    }

    /**
     * @param p
     * @return
     */
    @Override
    public Color getPlayerColor(Point p) {
        return tournamentPlayerColors.get(p);
    }

    public HashMap<Point, String> getPlayerNames(){
        return playerNames;
    }

    public Point getWinRecord(Point p){
        return winRecords.get(p);
    }



    private void setupOverlays() {

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


    public void setup(int n){

        if (currentGame != null) {
            currentGame.dispose();
        }
        if (currentGameOrchestrator != null) {
            currentGameOrchestrator.dispose();
        }

        game.gameScreen.ioPlayerNames = new ArrayList<>();
        game.gameScreen.ioPlayerIds = new ArrayList<>();
        game.gameScreen.player1Hud = null;
        game.gameScreen.player2Hud = null;

        betweenRoundPauseTimer = 2;

        gameCfg.humanPlayers = n;
        gameCfg.botPlayers = 3 - n;
        currentIteration = 1;
        playerPrimaryIndex = 0;
        currentScreen = game.gameScreen;

        createGameBoard();

        createBotPlayers(gameCfg.botPlayers);
        createHumanPlayers();
        createPlayerStarts();

        currentGameOrchestrator = new GameOrchestrator(currentGame);
        currentGame.setOrchestrator(currentGameOrchestrator);

        currentGame.createPlayerSummaryDisplays();
        currentGame.showPlayerSummary = true;

        currentGameOrchestrator.initializeFromGameBoard();
        currentGameOrchestrator.run();
        currentGameOrchestrator.startGame();
    }


    public void createGameBoard() {

        if (availableColors.size() < 3){
            availableColors.addAll(Arrays.asList(game.playerColors).subList(2, game.playerColors.length));
        }

        currentGame = new GameBoard(game, gameCfg, this);
        currentGame.voidDistributor.distribute();
        currentGame.resourceDistributor.distribute();

        currentGame.centerX = mapCenterX;
        currentGame.centerY = mapCenterY;
    }

    public void createPlayerStarts() {
        int sc = (int) Math.floor(Math.pow(gameCfg.radius, gameCfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> startingCoords = currentGame.playerStartAssigner.randomizeStartingCoords();
            currentGame.playerStartAssigner.assignStartingHexes(startingCoords);
        }
    }

    public void advanceFrameCount(){

    }

    /**
     * @return
     */
    @Override
    public Screen getScreen() {
        return currentScreen;
    }

    /**
     * @param p
     * @param organism
     */
    @Override
    public void updateHud(Point p, Organism organism) {
        /*FIXME
        PlayerHud hud = playerIdToHud.get(p);
        hud.setIncome(organism.income);
        hud.setEnergy(organism.energy);
        hud.setSpend(organism.spend);*/
    }


    public void finishThisRound(Point winnerId) {

        System.out.println("iteration: " + currentIteration + "/" + iterations);

        for (Point p : currentGame.players.keySet()) {
            Point rec = winRecords.get(p);
            if (p == winnerId){
                rec.x += 1;
            }
            else {
                rec.y += 1;
            }
            winRecords.put(p, rec);
        }

        roundSummary.setWinner(winnerId);
        showSummaryScreen = true;
        betweenRoundPauseTimer = betweenRoundPause;
    }




    public void createHumanPlayers(){

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
            PlayerHud humanHud = new PlayerHud(game, this, currentScreen, p==1, true);
            Player player = new IO_Player(
                currentGame,
                name,
                p,
                playerId,
                organism,
                humanHud,
                color
            );
            humanHud.setPlayer(player);
            organism.player = player;
            currentGame.players.put(playerId, player);
            game.gameScreen.add_player(player, p==1);

            currentGame.humanPlayerIds.add(playerId);
            currentGame.allPlayerIds.add(playerId);
        }
    }

    private void createBotPlayers(int n) {

        ArrayList<Point> available = new ArrayList<>(botPool);
        Collections.shuffle(available, game.rng);

        for (int i = 0; i < n; i++) {

            // Generate a fresh tournament ID for this bot
            Point playerId = available.get(i);

            // Generate name from your naming arrays
            String name = playerNamesArray[playerId.x % playerNamesArray.length]
                + " " + numerals[playerId.y % numerals.length];

            // Assign a color (reuse or pick new)
            Color color;
            if (tournamentPlayerColors.containsKey(playerId)) {
                color = tournamentPlayerColors.get(playerId);
            } else {
                color = availableColors.remove(0);
                tournamentPlayerColors.put(playerId, color);
            }

            // Create the actual BotPlayer via GameBoard (handles Organism + SlimeRLAgent wiring)
            currentGame.createBotPlayer(name, playerId, color);

            // Register in tracking maps for tournament scoring
            playerNames.put(playerId, name);
            winRecords.put(playerId, new Point(0, 0));  // init win/loss record
        }
    }

    public void logic(){

    }

    private void finish_arcade_loop() {
    }

    public void draw(float delta){
        currentGame.game.camera.update();
        currentGame.render(delta);
        if (showSummaryScreen & betweenRoundPause > 0 ) {
            roundSummary.render();
        }
    }

    public void render(float delta){

        if (kill) {
            dispose();
        }
        else {
            logic();
            draw(delta);

            if (showSummaryScreen) {
                betweenRoundPauseTimer -= delta;  // ← Decrement timer
                if (betweenRoundPauseTimer <= 0) {  // ← Timer expired
                    showSummaryScreen = false;
                    setup(gameCfg.humanPlayers);  // ← Start next game
                }
            }
        }
    }

    public void dispose() {
        botPool.clear();
        winRecords.clear();
        
        // Dispose all agents in model pool
        for (SlimeRLAgent.GameRLInterface agent : modelPool.values()) {
            agent.dispose();
        }
        modelPool.clear();
        
        if (currentGame != null) {
            currentGame.dispose();
        }
        if (currentGameOrchestrator != null) {
            currentGameOrchestrator.dispose();
        }
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
