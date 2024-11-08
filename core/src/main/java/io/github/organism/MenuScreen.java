package io.github.organism;

import com.badlogic.gdx.Screen;

public class MenuScreen implements Screen {

    OrganismGame game;
    GameBoard menu_game_board;
    public MenuScreen(OrganismGame g){
        game = g;
        GameConfig cfg = new GameConfig();
        cfg.radius = 7;
        cfg.resource_centers = 1;
        cfg.seed = 21;
        cfg.human_players = 0;
        cfg.bot_players = 3;
        cfg.map_view_size_param = 300;
        menu_game_board = new GameBoard(game, cfg);
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
