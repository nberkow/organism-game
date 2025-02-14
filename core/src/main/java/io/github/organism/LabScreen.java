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

    float overlay_x;
    float overlay_w;
    float overlay_y;
    float overlay_h;
    float center_y;

    float buttons_box_w;
    float buttons_box_h;
    float buttons_box_x;
    float buttons_box_y;
    TerritoryBar territory_bar;
    OrganismGame game;

    LabScreenControlOverlay overlay;

    LabSettingsButtons all_buttons;

    SettingsManager settings_manager;

    boolean show_control_overlay;

    String [] button_names;
    public LabScreen(OrganismGame organism_game) {

        game = organism_game;
        show_control_overlay = false;
        settings_manager = new SettingsManager(game, this);

        running_simulations = new ArrayList<>();
        game_configs = new ArrayList<>();

        overlay_w = this.game.VIRTUAL_WIDTH / 1.8f;
        overlay_x = (this.game.VIRTUAL_WIDTH - overlay_w) / 2;
        overlay_h = this.game.VIRTUAL_HEIGHT * 0.9f;
        overlay_y = (this.game.VIRTUAL_HEIGHT - overlay_h) / 2f;

        center_y = this.game.VIRTUAL_HEIGHT / 2f;

        overlay = new LabScreenControlOverlay(game, this);
        territory_bar = new TerritoryBar(game);

        buttons_box_w = this.game.VIRTUAL_WIDTH / 6.5f;
        buttons_box_h = this.game.VIRTUAL_HEIGHT / 5f;
        buttons_box_x = GameBoard.PLAYER_SUMMARY_X * 0.9f;
        buttons_box_y = GameBoard.PLAYER_SUMMARY_Y - (buttons_box_h * 1.75f);

        all_buttons = new LabSettingsButtons(game, this, overlay);
        overlay.setup_buttons();

        setup_buttons();

    }

    public void setup_sim(){
        GameConfig cfg = game.file_handler.read_cfg("kingdoms", "map");
        cfg.human_players = 0;
        cfg.bot_players = 3;

        current_sim = new Simulation(this, cfg, 1000);
        current_sim.run_simulation();
        visible_game = current_sim.current_game;
    }

    public void setup_buttons() {

        button_names = new String[]{
            "setup",
            "run",
            "save models"
        };

        float combined_button_height = all_buttons.base_button_height * button_names.length;
        float spacing = (buttons_box_h - combined_button_height) / (button_names.length + 1);

        for (int i=0; i<button_names.length; i++) {
            all_buttons.side_button_coords.put(button_names[i],
                new float[]{
                    buttons_box_x,
                    buttons_box_y + (spacing + all_buttons.base_button_height) * i,
                    all_buttons.base_button_width,
                    all_buttons.base_button_height
                });
        }
    }

    public void handle_button_click(String button_clicked) {


        if (show_control_overlay) {
            if (Objects.equals(button_clicked, "back")) {
                show_control_overlay = false;
            }
            if (Objects.equals(button_clicked, "reset")) {
                overlay.sliders.reset_sliders();
            }
            if (Objects.equals(button_clicked, "save")) {
                overlay.sliders.update_settings_from_sliders();
            }
        }
        else {
            if (Objects.equals(button_clicked, "setup")) {
                show_control_overlay = true;
            }
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

        if (show_control_overlay) {
            overlay.render();
        }
        all_buttons.render();

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
