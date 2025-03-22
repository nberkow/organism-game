package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.Point;
import java.util.ArrayList;

public class TutorialScreen  implements Screen {
    float mapCenterX;
    float mapCenterY;
    public TutorialInputProcessor inputProcessor;
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

    public void setup(){
        tutorial = new Tutorial(game, this, currentConfig);
        tutorial.setupBasicMovesTutorial();
    }

    private void input() {
        if (!tutorial.currentGameOrchestrator.paused) {
            inputProcessor.updateTimers(Gdx.graphics.getDeltaTime());
            inputProcessor.updateQueuesWithInput();
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
        tutorial.overlayHandler.render();
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
