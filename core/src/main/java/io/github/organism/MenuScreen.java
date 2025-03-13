package io.github.organism;

import com.badlogic.gdx.Screen;

import java.util.Objects;

public class MenuScreen implements Screen {

    OrganismGame game;
    MenuInputProcessor inputProcessor;

    GameConfig cfg;
    Simulation menu_simulation;

    MenuScreenButtons buttons;
    String [] button_names;


    public MenuScreen(OrganismGame g){
        game = g;
        cfg = new GameConfig();
        menu_simulation = new Simulation(game,this, cfg, 10);

        button_names = new String[] {
            "single player",
            "two player",
            "simulation",
            "tutorial",
            "exit to desktop"
        };
        buttons = new MenuScreenButtons(game, this);
        buttons.setup_buttons(button_names);
    }

    public void handle_button_click(String button_label){

        if (Objects.equals(button_label, "single player")){

        }
        if (Objects.equals(button_label, "two player")){

        }
        if (Objects.equals(button_label, "simulation")){

        }
        if (Objects.equals(button_label, "tutorial")){

        }
        if (Objects.equals(button_label, "exit to desktop")){

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
        if (menu_simulation.currentGameOrchestrator != null) {
            menu_simulation.currentGameOrchestrator.updateTimersAndFlags();
        }
    }

    private void logic() {
        if (menu_simulation.currentGameOrchestrator != null) {
            menu_simulation.currentGameOrchestrator.updatePlayers();
        }
    }

    private void draw() {
        // Ensure the camera is updated before drawing
        if (menu_simulation.currentGame != null) {
            game.camera.update();
            menu_simulation.currentGame.render();
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
