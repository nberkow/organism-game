package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.organism.hud.HudInputProcessor;
import io.github.organism.hud.PlayerHud;

public class HudTestScreen implements Screen {

    PlayerHud p1Hud;
    PlayerHud p2Hud;
    public HudInputProcessor inputProcessor;

    public OrganismGame game;

    private static class HudTester implements GameSession {
        HudTestScreen scr;
        public HudTester(HudTestScreen s){
            scr = s;
        }

        /**
         * @return
         */
        @Override
        public InputProcessor getInputProcessor() {
            return scr.inputProcessor;
        }
    }

    public HudTestScreen(OrganismGame g) {
        game = g;
        p1Hud = new PlayerHud(game, new HudTester(this), this, false);
        p2Hud = new PlayerHud(game, new HudTester(this), this, true);
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
        if (p1Hud != null){
            p1Hud.render();
        }

        if (p2Hud != null){
            p2Hud.render();
        }
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
