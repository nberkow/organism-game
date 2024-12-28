package io.github.organism;

import com.badlogic.gdx.Screen;

import java.util.ArrayList;

public class LabScreen implements Screen {

    final int DEFAULT_SIZE = 6;
    ArrayList<Simulation> running_simulations;
    ArrayList<GameConfig> game_configs;
    GameBoard visible_game;
    Simulation current_sim;
    float controls_x;
    float controls_w;


    LabControlBar control_bar;
    OrganismGame game;
    public LabScreen(OrganismGame organism_game) {
        game = organism_game;
        visible_game = null;
        running_simulations = new ArrayList<>();
        game_configs = new ArrayList<>();

        controls_x = this.game.VIRTUAL_WIDTH * 5 / 6f;
        controls_w = this.game.VIRTUAL_WIDTH / 2.5f;

        control_bar = new LabControlBar(this, this.game.VIRTUAL_HEIGHT - 10);

        test_setup();

    }

    public void test_setup(){
        GameConfig cfg = game.file_handler.read_cfg("kingdoms", "map");
        cfg.human_players = 0;
        cfg.bot_players = 3;
        System.out.println(cfg.layout);

        current_sim = new Simulation(this, cfg, 3);
        current_sim.create_game();
        current_sim.set_random_models();
        current_sim.run_simulation();
        visible_game = current_sim.current_game;
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
        current_sim.render();
        control_bar.render();
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
