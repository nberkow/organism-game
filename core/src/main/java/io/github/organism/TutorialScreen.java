package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.sql.Time;

import io.github.organism.hud.HudInputProcessor;
import io.github.organism.hud.PlayerHud;

public class TutorialScreen  implements Screen {
    float mapCenterX;
    float mapCenterY;
    public HudInputProcessor inputProcessor;
    OrganismGame game;
    PlayerHud player1Hud;

    SettingsOverlay overlay;
    Tutorial tutorial;
    GameConfig currentConfig;

    public TutorialScreen(OrganismGame g) {

        game = g;
        setup_overlay();

        currentConfig = new GameConfig();
        currentConfig.gameplaySettings = overlay.savedSettings;

        mapCenterX = OrganismGame.VIRTUAL_WIDTH / 2f;
        mapCenterY = OrganismGame.VIRTUAL_HEIGHT / 2f;

        tutorial = new Tutorial(game, this, currentConfig);
        tutorial.setupBasicMovesTutorial();

        tutorial.currentGameOrchestrator.run();
        setupTimeIndicator();
    }

    private void setup_overlay(){

        float overlay_w = OrganismGame.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (OrganismGame.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = OrganismGame.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (OrganismGame.VIRTUAL_HEIGHT - overlay_h) / 2f;

        overlay = new SettingsOverlay(game, this, overlay_x, overlay_y, overlay_w, overlay_h);
        overlay.setupSliders();
        overlay.setupButtons();
    }

    private void setupTimeIndicator(){
        float sideLen = OrganismGame.VIRTUAL_WIDTH / 20f;
        float x = OrganismGame.VIRTUAL_WIDTH / 4f;
        float y = OrganismGame.VIRTUAL_HEIGHT - sideLen * 1.5f;
        tutorial.timeIndicator = new TimeIndicator(game, tutorial, x, y, sideLen);
    }


    private void input() {
        if (!tutorial.currentGameOrchestrator.paused) {
            tutorial.currentGameOrchestrator.updatePlayers();
            tutorial.currentGameOrchestrator.updateTimersAndFlags();
        }
    }

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
        ScreenUtils.clear(game.backgroundColor);
        input();
        tutorial.render();
        player1Hud.render();

    }

    /**
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {

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
