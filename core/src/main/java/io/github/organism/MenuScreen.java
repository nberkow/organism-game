package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import java.util.Objects;

public class MenuScreen implements Screen {

    OrganismGame game;
    MenuInputProcessor input_processor;

    MenuScreenButtons buttons;
    String [] button_names;


    public MenuScreen(OrganismGame g){
        game = g;
        button_names = new String[] {
            "single player",
            "two player",
            "simulation",
            "settings",
            "exit"
        };
        buttons = new MenuScreenButtons(game, this);
        buttons.setup_buttons(button_names);
    }

    public void handle_button_click(String button_label){
        System.out.println(button_label);

        if (Objects.equals(button_label, "single player")){

        }
        if (Objects.equals(button_label, "two player")){

        }
        if (Objects.equals(button_label, "simulation")){

        }
        if (Objects.equals(button_label, "settings")){

        }
        if (Objects.equals(button_label, "exit")){

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
        input();
        logic();
        draw();
    }

    private void input() {
        if (game.main_arcade_loop.current_game_orchestrator != null) {
            game.main_arcade_loop.current_game_orchestrator.update_timers_and_flags();
        }
    }

    private void logic() {
        if (game.main_arcade_loop.current_game_orchestrator != null) {
            game.main_arcade_loop.current_game_orchestrator.update_players();
        }
    }

    private void draw() {
        // Ensure the camera is updated before drawing
        if (game.main_arcade_loop.current_game != null) {
            game.camera.update();
            game.main_arcade_loop.current_game.render();
        }
        buttons.render();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport and camera based on the new window size
        game.viewport.update(width, height, true);  // true centers the camera
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
