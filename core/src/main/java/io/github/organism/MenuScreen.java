package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class MenuScreen implements Screen {

    OrganismGame game;
    GameBoard menu_game_board;

    MenuInputProcessor menu_input_processor;
    GameOrchestrator menu_orchestrator;
    public MenuScreen(OrganismGame g){
        game = g;
        GameConfig cfg = new GameConfig();
        cfg.radius = 12;
        cfg.resource_centers = 1;
        cfg.seed = 21;
        cfg.human_players = 0;
        cfg.bot_players = 6;
        cfg.map_view_size_param = 350;

        menu_game_board = new GameBoard(game, cfg);
        menu_game_board.center_y = this.game.VIRTUAL_HEIGHT / 2f;
        menu_game_board.show_data = false;


        menu_input_processor = new MenuInputProcessor();
        menu_orchestrator = new GameOrchestrator(menu_game_board);
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
        input();
        logic();
        draw();
    }

    private void input() {
        menu_orchestrator.update_timers_and_flags();
    }

    private void logic() {
        menu_orchestrator.update_players();
    }

    private void draw() {
        // Ensure the camera is updated before drawing
        menu_game_board.game.camera.update();
        menu_game_board.render();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport and camera based on the new window size
        menu_game_board.game.viewport.update(width, height, true);  // true centers the camera
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
