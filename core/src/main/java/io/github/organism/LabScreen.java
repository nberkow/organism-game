package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Objects;

public class LabScreen implements Screen {

    ArrayList<Simulation> running_simulations;
    ArrayList<GameConfig> game_configs;
    GameBoard visible_game;
    Simulation current_sim;

    float buttons_box_w;
    float buttons_box_h;
    float buttons_box_x;
    float buttons_box_y;
    TerritoryBar territory_bar;
    OrganismGame game;

    SettingsOverlay overlay;

    LabScreenButtons buttons;

    SettingsManager settings_manager;

    LabScreenInputProcessor input_processor;

    String [] button_names;
    public LabScreen(OrganismGame organism_game) {

        game = organism_game;

        running_simulations = new ArrayList<>();
        game_configs = new ArrayList<>();
        territory_bar = new TerritoryBar(game);

        setup_overlay();
        setup_buttons();

    }
    public void setup_sim(){
        GameConfig cfg = game.file_handler.read_cfg("kingdoms", "map");
        cfg.human_players = 0;
        cfg.bot_players = 3;
        cfg.gameplay_settings = overlay.saved_settings;
        int iterations = Math.round(overlay.saved_settings.get("iterations"));
        current_sim = new Simulation(this, cfg, iterations);

        current_sim.run_simulation();
        visible_game = current_sim.current_game;
    }
    private void setup_overlay(){

        float overlay_w = this.game.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (this.game.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = this.game.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (this.game.VIRTUAL_HEIGHT - overlay_h) / 2f;

        overlay = new SettingsOverlay(game, this, overlay_x, overlay_y, overlay_w, overlay_h);
        overlay.setup_sliders();
        overlay.setup_buttons();
    }


    public void setup_buttons() {
        buttons_box_w = this.game.VIRTUAL_WIDTH / 6.5f;
        buttons_box_h = this.game.VIRTUAL_HEIGHT / 5f;
        buttons_box_x = GameBoard.PLAYER_SUMMARY_X * 0.9f;
        buttons_box_y = GameBoard.PLAYER_SUMMARY_Y - (buttons_box_h * 1.75f);
        buttons = new LabScreenButtons(game, this);

        button_names = new String[]{
            "run", "kill", "setup"
        };

        float combined_button_height = buttons.base_button_height * button_names.length;
        float spacing = (buttons_box_h - combined_button_height) / (button_names.length + 1);

        for (int i=0; i<button_names.length; i++) {
            buttons.side_button_coords.put(button_names[i],
                new float[]{
                    buttons_box_x,
                    buttons_box_y + (spacing + buttons.base_button_height) * i,
                    buttons.base_button_width,
                    buttons.base_button_height
                });
        }
    }

    public void handle_button_click(String button_clicked) {

        if (Objects.equals(button_clicked, "setup")) {
            overlay.show_control_overlay = true;
            Gdx.input.setInputProcessor(overlay.input_processor);
        }

        if (Objects.equals(button_clicked, "run")) {
            setup_sim();
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
        ScreenUtils.clear(game.background_color);
        if (current_sim != null) {
            current_sim.render();
            territory_bar.render(current_sim.current_game);
        }

        if (overlay.show_control_overlay) {
            overlay.render();
        }
        buttons.render();

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
