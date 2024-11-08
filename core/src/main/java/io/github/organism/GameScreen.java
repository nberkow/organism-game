package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class GameScreen implements Screen {

    OrganismGame game;
    GameBoard game_board;

    GameOrchestrator orchestrator;
    public GameScreen(OrganismGame g){
        game = g;
        game_board = g.game_board;
        orchestrator = new GameOrchestrator(game_board);
    }

    private void input() {
        orchestrator.update_timers_and_flags();
    }

    private void logic() {
        orchestrator.update_players();
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
