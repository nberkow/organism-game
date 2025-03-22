package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import java.awt.Point;
import java.util.ArrayList;

public class Tutorial implements GameMode {

    OrganismGame game;
    TutorialScreen screen;
    GameConfig currentConfig;
    GameBoard currentGame;
    public GameOrchestrator currentGameOrchestrator;

    public TutorialOverlayHandler overlayHandler;

    public Tutorial(OrganismGame g, TutorialScreen tut, GameConfig cfg) {
        game = g;
        screen = tut;
        currentConfig = cfg;
        overlayHandler = new TutorialOverlayHandler(game, this);
    }

    public void setupBasicMovesTutorial() {
        /*
        Setup to explain energy, extraction and expansion

        hud components, move queue

         */

        createGameBoard();

        createHumanPlayer();

        createDummyPlayers();

        createPlayerStarts();

        currentGameOrchestrator.run();

        /*
        currentGame.createPlayerSummaryDisplays();
        currentGame.showPlayerSummary = false;

        currentGame.diplomacyGraph = new DiplomacyGraph(game, currentGame);
        currentGame.showDiplomacy = false;

         */
    }

    public void setupEnemyInteraction() {

        /*
        Setup to explain enemy specific expansion

        enemy summary bar and queue

         */


        createGameBoard();

        createBotPlayer();

        createHumanPlayer();
        createPlayerStarts();

        setPlayerButtonColors();

        currentGame.createPlayerSummaryDisplays();
        currentGame.showPlayerSummary = false;

        currentGame.diplomacyGraph = new DiplomacyGraph(game, currentGame);
        currentGame.showDiplomacy = false;
    }

    public void setupDiplomacy(){

        /*
        explain diplomacy

        diplomacy income bonus
         */

        setupEnemyInteraction();
    }

    private void setPlayerButtonColors() {
    }

    private void createPlayerStarts() {
        int sc = (int) Math.floor(Math.pow(currentConfig.radius, currentConfig.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = currentGame.player_start_assigner.randomizeStartingCoords();
            currentGame.player_start_assigner.assignStartingHexes(starting_coords);
        }
    }

    private void createBotPlayer() {
    }

    private void createHumanPlayer() {
        Point playerId = new Point(-1, 0);

        Color color = Color.RED;
        String name = "Player " + (1);
        Organism organism = new Organism(currentGame);
        Player player = new IO_Player(
            currentGame,
            name,
            0,
            playerId,
            organism,
            false,
            color
        );
        organism.player = player;
        currentGame.players.put(playerId, player);

        currentGame.humanPlayerIds.add(playerId);
        currentGame.allPlayerIds.add(playerId);

        screen.player1Hud = new PlayerHud(game, screen, player, false);
        screen.inputProcessor.add_player(playerId);

    }

    public void createDummyPlayers(){
        for (int i=0; i<2; i++) {
            DummyPlayer player = new DummyPlayer();
            Point playerId = new Point(0, i);
            currentGame.players.put(playerId, player);
            currentGame.allPlayerIds.add(playerId);
        }
    }

    private void createGameBoard() {
        currentGame = new GameBoard(game, currentConfig, screen);
        currentGame.voidDistributor.distribute();
        currentGame.resourceDistributor.distribute();

        currentGameOrchestrator = new GameOrchestrator(currentGame);
        currentGame.set_orchestrator(currentGameOrchestrator);

        screen.inputProcessor.gameBoard = currentGame;

    }

    public void render() {
        currentGame.render();
    }

    public void logic() {

    }

    public void draw() {

    }

}
