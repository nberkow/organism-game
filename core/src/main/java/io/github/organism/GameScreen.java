package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {

    OrganismGame game;
    GameBoard game_board;
    GameInputProcessor input_processor;

    GameConfig cfg;
    GameOrchestrator orchestrator;
    public GameScreen(OrganismGame g){
        game = g;
        cfg = new GameConfig();
        game_board = new GameBoard(game, cfg);
        orchestrator = new GameOrchestrator(game_board);
    }

    private void input() {
        input_processor.update_timers(Gdx.graphics.getDeltaTime());
        input_processor.update_queues_with_input();
        orchestrator.update_players();
        orchestrator.update_timers_and_flags();
    }

    private void logic() {


    }

    private void draw() {
        // Ensure the camera is updated before drawing
        game_board.game.camera.update();
        game_board.render();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport and camera based on the new window size
        game_board.game.viewport.update(width, height, true);  // true centers the camera
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
