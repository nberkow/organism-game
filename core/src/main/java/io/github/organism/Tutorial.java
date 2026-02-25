package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.organism.hud.PlayerHud;
import io.github.organism.player.BotPlayer;
import io.github.organism.player.DummyPlayer;
import io.github.organism.player.IO_Player;
import io.github.organism.player.Player;

public class Tutorial implements GameSession {

    public GlobalResourceIndicator globalResourceIndicator;
    OrganismGame game;
    TutorialScreen screen;
    GameConfig currentConfig;
    GameBoard currentGame;
    public GameOrchestrator currentGameOrchestrator;
    HashMap<Point, PlayerHud> playerIdToHud;



    public Tutorial(OrganismGame g, TutorialScreen tut, GameConfig cfg) {
        game = g;
        screen = tut;
        currentConfig = cfg;
        playerIdToHud = new HashMap<>();

    }

    public void setupBasicMovesTutorial() {
        /*
        Setup to explain energy, extraction and expansion
        hud components
         */

        createGameBoard();
        createBotPlayer();
        createDummyPlayers();
        createPlayerStarts();

        currentGameOrchestrator.pause();

    }


    private void createPlayerStarts() {
        int sc = (int) Math.floor(Math.pow(currentConfig.radius, currentConfig.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = currentGame.playerStartAssigner.randomizeStartingCoords();
            currentGame.playerStartAssigner.assignStartingHexes(starting_coords);
        }
    }

    public void advanceFrameCount(){
        if (!currentGameOrchestrator.paused) {
            globalResourceIndicator.advanceTurn();

        }
    }

    /**
     * @return
     */
    @Override
    public Screen getScreen() {
        return screen;
    }

    /**
     * @param p
     * @param organism
     */
    @Override
    public void updateHud(Point p, Organism organism) {
        PlayerHud hud = playerIdToHud.get(p);
        hud.setEnergy(organism.energy / SettingsManager.MAX_ENERGY);
        hud.setIncome(organism.income / SettingsManager.MAX_ENERGY);
        hud.setSpend(organism.expandRate / SettingsManager.MAX_ENERGY);

        hud.setResources(organism.resources);
    }

    private void createBotPlayer() {
        Point playerId = new Point(-999, 0);
        screen.player1Hud = new PlayerHud(game, this, screen, false);
        playerIdToHud.put(playerId, screen.player1Hud);

        Color color = Color.RED;
        String name = "Player " + (1);
        Organism organism = new Organism(currentGame);
        Player player = new BotPlayer(
            currentGame,
            name,
            0,
            playerId,
            organism,
            screen.player1Hud,
            color
        );
        organism.player = player;
        currentGame.players.put(playerId, player);
        currentGame.botPlayerIds.add(playerId);
        currentGame.allPlayerIds.add(playerId);
    }

    private void createHumanPlayer() {
        Point playerId = new Point(-1, 0);
        screen.player1Hud = new PlayerHud(game, this, screen, false);
        playerIdToHud.put(playerId, screen.player1Hud);

        Color color = Color.RED;
        String name = "Player " + (1);
        Organism organism = new Organism(currentGame);
        Player player = new IO_Player(
            currentGame,
            name,
            0,
            playerId,
            organism,
            screen.player1Hud,
            color
        );
        organism.player = player;
        currentGame.players.put(playerId, player);
        currentGame.humanPlayerIds.add(playerId);
        currentGame.allPlayerIds.add(playerId);
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
        currentGame = new GameBoard(game, currentConfig, this);
        currentGame.voidDistributor.distribute();
        currentGame.resourceDistributor.distribute();

        currentGameOrchestrator = new GameOrchestrator(currentGame);
        currentGame.setOrchestrator(currentGameOrchestrator);
    }

    public void render(float timeDelta) {
        currentGame.render(timeDelta);
        globalResourceIndicator.render();
    }


    /**
     * @return
     */
    @Override
    public InputProcessor getInputProcessor() {
        return screen.inputProcessor;
    }

}
