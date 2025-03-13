package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.Point;
import java.util.ArrayList;

public class GameScreen implements Screen {

    public SettingsManager settings_manager;
    public SettingsOverlay overlay;

    public ArcadeLoop arcadeLoop;
    OrganismGame game;
    GameInputProcessor inputProcessor;
    PlayerHud player1Hud;
    PlayerHud player2Hud;
    ArrayList<String> ioPlayerNames;
    ArrayList<Point> ioPlayerIds;

    public GameScreen(OrganismGame g){
        game = g;
        setupOverlay();
        overlay.setupSliders();

        ioPlayerNames = new ArrayList<>();
        ioPlayerIds = new ArrayList<>();
    }
    public ArrayList<Player> getIoPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        if (arcadeLoop.currentGame == null){
            return players;
        }
        for (Point p : ioPlayerIds) {
            players.add(arcadeLoop.currentGame.players.get(p));
        }
        return players;
    }


    public void add_player(Player player, boolean player2){

        ioPlayerNames.add(player.getPlayerName());
        Point player_id = player.getTournamentId();
        ioPlayerIds.add(player_id);

        if (!player2) {
            player1Hud = new PlayerHud(game, this, player, false);
        }

        else {
            player2Hud = new PlayerHud(game, this, player, true);
        }
        inputProcessor.add_player(player_id);

    }

    private void setupOverlay(){

        float overlay_w = OrganismGame.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (OrganismGame.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = OrganismGame.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (OrganismGame.VIRTUAL_HEIGHT - overlay_h) / 2f;

        overlay = new SettingsOverlay(game, this, overlay_x, overlay_y, overlay_w, overlay_h);
    }

    private void input() {
        if (!arcadeLoop.currentGameOrchestrator.paused) {
            inputProcessor.updateTimers(Gdx.graphics.getDeltaTime());
            inputProcessor.updateQueuesWithInput();
            arcadeLoop.currentGameOrchestrator.updatePlayers();
            arcadeLoop.currentGameOrchestrator.updateTimersAndFlags();
        }
    }

    private void logic() {}

    private void draw() {}

    @Override
    public void resize(int width, int height) { }
    /**
     *
     */
    @Override
    public void show() {

    }

    /**
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        input();

        ScreenUtils.clear(game.backgroundColor);

        if (arcadeLoop != null) {
            arcadeLoop.render();
        }

        if (player1Hud != null) {
            player1Hud.render();
        }


        if (player2Hud != null) {
            player2Hud.render();
        }

        if (overlay.showControlOverlay) {
            overlay.render();
        }
    }

    /**
     *
     */
    @Override
    public void pause() {

    }

    /**
     *
     */
    @Override
    public void resume() {

    }

    /**
     *
     */
    @Override
    public void hide() {

    }

    /**
     *
     */
    @Override
    public void dispose() {

    }


}
