package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Objects;

public class LabScreen implements Screen {
    Simulation current_sim;

    float buttonsBoxW;
    float buttonsBoxH;
    float buttonsBoxX;
    float buttonsBoxY;

    float checkboxAreaW;
    float checkboxAreaH;
    float checkboxAreaX;
    float checkboxAreaY;
    boolean writeFiles;
    TerritoryBar territoryBar;
    OrganismGame game;

    SettingsOverlay overlay;

    LabScreenButtons buttons;

    CheckBoxGroup checkboxes;

    SettingsManager settingsManager;

    LabScreenInputProcessor inputProcessor;

    String [] buttonNames;

    boolean silent = false;
    public LabScreen(OrganismGame organismGame) {

        game = organismGame;
        territoryBar = new TerritoryBar(game);

        setupOverlay();
        setupButtons();
        setupCheckboxes();

    }

    public void setupSim(){
        GameConfig cfg = game.fileHandler.read_cfg("kingdoms", "map");
        cfg.humanPlayers = 0;
        cfg.botPlayers = 3;
        cfg.gameplaySettings = overlay.savedSettings;
        int iterations = Math.round(overlay.savedSettings.get("iterations"));
        current_sim = new Simulation(game, this, cfg, iterations);
        current_sim.runSimulation();
    }

    public void setupSilentSim(){
        GameConfig cfg = game.fileHandler.read_cfg("kingdoms", "map");
        cfg.humanPlayers = 0;
        cfg.botPlayers = 3;
        cfg.gameplaySettings = overlay.savedSettings;
        int iterations = Math.round(overlay.savedSettings.get("iterations"));
        current_sim = new Simulation(game,this, cfg, iterations);
        current_sim.initialize_model_pool();
        current_sim.run_silent();
    }
    private void setupOverlay(){

        float overlay_w = OrganismGame.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (OrganismGame.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = OrganismGame.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (OrganismGame.VIRTUAL_HEIGHT - overlay_h) / 2f;

        overlay = new SettingsOverlay(game, this, overlay_x, overlay_y, overlay_w, overlay_h);
        overlay.setupSliders();
        overlay.setupButtons();
    }


    public void setupButtons() {
        buttonsBoxW = OrganismGame.VIRTUAL_WIDTH / 6.5f;
        buttonsBoxH = OrganismGame.VIRTUAL_HEIGHT / 5f;
        buttonsBoxX = GameBoard.PLAYER_SUMMARY_X * 0.9f;
        buttonsBoxY = GameBoard.PLAYER_SUMMARY_Y - (buttonsBoxH * 1.75f);
        buttons = new LabScreenButtons(game, this);

        buttonNames = new String[]{
            "run", "kill", "setup"
        };

        float combined_button_height = buttons.base_button_height * buttonNames.length;
        float spacing = (buttonsBoxH - combined_button_height) / (buttonNames.length + 1);

        for (int i = 0; i< buttonNames.length; i++) {
            buttons.side_button_coords.put(buttonNames[i],
                new float[]{
                    buttonsBoxX,
                    buttonsBoxY + (spacing + buttons.base_button_height) * i,
                    buttons.base_button_width,
                    buttons.base_button_height
                });
        }
    }

    private void setupCheckboxes() {
        checkboxAreaW = OrganismGame.VIRTUAL_WIDTH / 6.5f;
        checkboxAreaH = OrganismGame.VIRTUAL_HEIGHT / 5f;
        checkboxAreaX = GameBoard.PLAYER_SUMMARY_X * 0.9f;
        checkboxAreaY = buttonsBoxY + buttonsBoxH;
        checkboxes = new CheckBoxGroup(game, checkboxAreaX, checkboxAreaY, checkboxAreaW, checkboxAreaH);

        checkboxes.add_checkbox("histograms", false);
        checkboxes.add_checkbox("save winners", true);
        checkboxes.add_checkbox("skip leaderboard", false);

        checkboxes.calculate_coords();
    }

    public void handleButtonClick(String button_clicked) {

        if (Objects.equals(button_clicked, "setup")) {
            overlay.showControlOverlay = true;
            Gdx.input.setInputProcessor(overlay.input_processor);
        }

        if (Objects.equals(button_clicked, "run")) {
            int iterations = Math.round(overlay.savedSettings.get("iterations"));
            if (iterations > 1000) {
                silent = true;
                setupSilentSim();
            }
            else {
                silent = false;
                setupSim();
            }

        }

        if (Objects.equals(button_clicked, "kill")) {
            current_sim.kill = true;
        }
    }
    public void handleCheckboxClick(String box_clicked) {
        boolean state = checkboxes.checkbox_states.get(box_clicked);

        if (current_sim != null) {
            if (Objects.equals(box_clicked, "skip leaderboard")) {
                if (!state) {
                    current_sim.between_round_pause = 0;
                } else {
                    current_sim.between_round_pause = 2;
                }
            }

            if (Objects.equals(box_clicked, "histograms")){
                current_sim.show_histograms = !state;
            }

            checkboxes.checkbox_states.put(box_clicked, !state);
        }

        if (Objects.equals(box_clicked, "save winners")){
            writeFiles = !state;
            checkboxes.checkbox_states.put(box_clicked, !state);
        }
    }

    public boolean getWriteFiles(){
        return writeFiles;
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
        ScreenUtils.clear(game.backgroundColor);

        if (current_sim != null) {
            current_sim.render();
            territoryBar.render(current_sim.currentGame);
        }

        if (overlay.showControlOverlay) {
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
