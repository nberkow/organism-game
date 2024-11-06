package io.github.organism;

import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {

    OrganismGame game;
    GameBoard game_board;
    public GameScreen(OrganismGame g){
        game = g;
        game_board = g.game_board;

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
        game_board.render();
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
