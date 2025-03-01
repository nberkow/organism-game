package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Objects;

public class LabScreen implements Screen {
    Simulation current_sim;

    float buttons_box_w;
    float buttons_box_h;
    float buttons_box_x;
    float buttons_box_y;

    float checkbox_area_w;
    float checkbox_area_h;
    float checkbox_area_x;
    float checkbox_area_y;
    boolean write_files;
    TerritoryBar territory_bar;
    OrganismGame game;

    SettingsOverlay overlay;

    LabScreenButtons buttons;

    CheckBoxGroup checkboxes;

    SettingsManager settings_manager;

    LabScreenInputProcessor input_processor;

    String [] button_names;

    boolean silent = false;
    public LabScreen(OrganismGame organism_game) {

        game = organism_game;
        territory_bar = new TerritoryBar(game);

        setup_overlay();
        setup_buttons();
        setup_checkboxes();

    }

    public void setup_sim(){
        GameConfig cfg = game.file_handler.read_cfg("kingdoms", "map");
        cfg.human_players = 0;
        cfg.bot_players = 3;
        cfg.gameplay_settings = overlay.saved_settings;
        int iterations = Math.round(overlay.saved_settings.get("iterations"));
        current_sim = new Simulation(this, cfg, iterations);

        current_sim.run_simulation();
    }

    public void setup_silent_sim(){
        GameConfig cfg = game.file_handler.read_cfg("kingdoms", "map");
        cfg.human_players = 0;
        cfg.bot_players = 3;
        cfg.gameplay_settings = overlay.saved_settings;
        int iterations = Math.round(overlay.saved_settings.get("iterations"));
        current_sim = new Simulation(this, cfg, iterations);
        current_sim.initialize_model_pool();
        current_sim.run_silent();
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

    private void setup_checkboxes() {
        checkbox_area_w = this.game.VIRTUAL_WIDTH / 6.5f;
        checkbox_area_h = this.game.VIRTUAL_HEIGHT / 5f;
        checkbox_area_x = GameBoard.PLAYER_SUMMARY_X * 0.9f;
        checkbox_area_y = buttons_box_y + buttons_box_h;
        checkboxes = new CheckBoxGroup(game, checkbox_area_x, checkbox_area_y, checkbox_area_w, checkbox_area_h);

        checkboxes.add_checkbox("save winners", false);
        checkboxes.add_checkbox("show leaderboard", true);

        checkboxes.calculate_coords();
    }

    public void handle_button_click(String button_clicked) {

        if (Objects.equals(button_clicked, "setup")) {
            overlay.show_control_overlay = true;
            Gdx.input.setInputProcessor(overlay.input_processor);
        }

        if (Objects.equals(button_clicked, "run")) {
            silent = true;
            setup_silent_sim();

        }

        if (Objects.equals(button_clicked, "kill")) {
            current_sim.kill = true;
        }
    }
    public void handle_checkbox_click(String box_clicked) {
        boolean state = checkboxes.checkbox_states.get(box_clicked);

        if (Objects.equals(box_clicked, "show leaderboard")){
            if (!state) {
                current_sim.between_round_pause = 2;
            }
            else {
                current_sim.between_round_pause = 0;
            }
        }

        if (Objects.equals(box_clicked, "save winners")){
            write_files = !state;
        }

        checkboxes.checkbox_states.put(box_clicked, !state);
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
        checkboxes.render();

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
