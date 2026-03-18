package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.Objects;

import io.github.organism.hud.DebugLogger;

public class SettingsOverlay {


    public InputProcessor inputProcessor;
    float overlay_x;
    float overlay_y;
    float overlay_w;
    float overlay_h;

    float slider_box_x;
    float slider_box_y;
    float slider_box_w;
    float slider_box_h;

    float buttons_box_x;
    float buttons_box_y;
    float buttons_box_w;
    float buttons_box_h;
    OrganismGame game;
    Screen screen;
    BitmapFont font;
    SliderGroup sliders;
    SettingsOverlayButtons buttons;
    String [] button_names;
    boolean showControlOverlay;
    HashMap<String, Float> savedSettings;
    public SettingsOverlay(OrganismGame g, Screen scr, float x, float y, float w, float h) {
        game = g;
        screen = scr;
        font = game.fonts.get(16);
        font.setColor(Color.CYAN);
        overlay_x = x;
        overlay_y = y;
        overlay_h = h;
        overlay_w = w;
        savedSettings = new HashMap<>();
    }

    public void setupSliders() {

        slider_box_w = overlay_w * .75f;
        slider_box_h = overlay_h * .70f; // More height for better spacing

        slider_box_x = (OrganismGame.VIRTUAL_WIDTH - slider_box_w) / 2f;
        slider_box_y = (OrganismGame.VIRTUAL_HEIGHT - slider_box_h) / 2f + OrganismGame.VIRTUAL_HEIGHT / 12f; // Shifted down

        sliders = new SliderGroup(game, screen, slider_box_x, slider_box_y, slider_box_w, slider_box_h);

        // Energy settings - using log scales where appropriate
        // Max energy: 10^1, 10^2, 10^3 (10, 100, 1000)
        sliders.add_slider("max energy", 1, 3, 1f, (float)Math.log10(SettingsManager.MAX_ENERGY));

        // Starting energy: 0-100% in 5% increments
        sliders.add_slider("starting energy %", 0, 100, 5f, SettingsManager.DEFAULT_STARTING_ENERGY);

        // Base income: 2^-3 to 2^2 (0.125%, 0.25%, 0.5%, 1%, 2%, 4%)
        sliders.add_slider("base income %", -3, 2, 1f, (float)(Math.log(SettingsManager.BASE_INCOME_PERCENT) / Math.log(2)));

        // Vertex energy cost: 1-10 in 0.5 increments
        sliders.add_slider("vertex energy cost", 1, 10, 0.5f, SettingsManager.VERTEX_ENERGY_COST);

        // Game speed settings
        sliders.add_slider("speed", 1, 7, 1, 7f);
        sliders.add_slider("iterations", 1, 9, 1, 1f);

        sliders.load_initial_positions();
        save_slider_settings(); // populates the data structure with defaults
    }

    public void setupButtons() {

        buttons = new SettingsOverlayButtons(game, screen, this);
        button_names = new String [] {
            "back", "reset", "save"
        };

        buttons_box_w = slider_box_w;
        buttons_box_h = overlay_h * .15f;

        buttons_box_x = slider_box_x;
        buttons_box_y = slider_box_y - game.VIRTUAL_HEIGHT / 8f; // More space below sliders

        float combined_button_width = buttons.base_button_width * button_names.length;
        float spacing = (buttons_box_w - combined_button_width) / (button_names.length + 1);

        for (int i=0; i<button_names.length; i++) {
            buttons.overlay_button_coords.put(button_names[i],
                new float[]{
                    buttons_box_x + (spacing) * (i+1) + (buttons.base_button_width * i),
                    buttons_box_y,
                    buttons.base_button_width,
                    buttons.base_button_height
                });
        }
    }

    public void render(){

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(Color.BLACK);
        game.shapeRenderer.rect(
            overlay_x, overlay_y, overlay_w, overlay_h
        );
        game.shapeRenderer.end();

        float margin = overlay_w * 0.05f;
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(
            overlay_x + margin,
            overlay_y + margin,
            overlay_w - (margin * 2),
            overlay_h - (margin * 2)
        );
        game.shapeRenderer.end();

        sliders.render();
        buttons.render();

    }

    public void save_slider_settings(){
        DebugLogger.getInstance().log("=== SettingsOverlay.save_slider_settings() called ===");

        for (String p : sliders.slider_label_order){
            float val = sliders.slider_selected_values.get(p);

            // Apply transformations for exponential/log sliders
            if (Objects.equals(p, "iterations")) {
                val = (float) Math.pow(10, val);
            }
            if (Objects.equals(p, "speed")) {
                val = (float) Math.pow(2, val);
            }
            if (Objects.equals(p, "max energy")) {
                val = (float) Math.pow(10, val);
            }
            if (Objects.equals(p, "base income %")) {
                val = (float) Math.pow(2, val);
            }

            savedSettings.put(p, val);

            // Update SettingsManager static values when saved
            if (Objects.equals(p, "max energy")) {
                SettingsManager.MAX_ENERGY = val;
                DebugLogger.getInstance().log("  Updated MAX_ENERGY to: " + val);
            }
            if (Objects.equals(p, "starting energy %")) {
                SettingsManager.DEFAULT_STARTING_ENERGY = val;
                DebugLogger.getInstance().log("  Updated DEFAULT_STARTING_ENERGY to: " + val + "%");
            }
            if (Objects.equals(p, "base income %")) {
                SettingsManager.BASE_INCOME_PERCENT = val;
                DebugLogger.getInstance().log("  Updated BASE_INCOME_PERCENT to: " + val + "%");
            }
            if (Objects.equals(p, "vertex energy cost")) {
                SettingsManager.VERTEX_ENERGY_COST = val;
                DebugLogger.getInstance().log("  Updated VERTEX_ENERGY_COST to: " + val);
            }
        }

        DebugLogger logger = DebugLogger.getInstance();
        logger.log("=== Settings saved. Current SettingsManager values: ===");
        logger.log("  MAX_ENERGY: " + SettingsManager.MAX_ENERGY);
        logger.log("  DEFAULT_STARTING_ENERGY: " + SettingsManager.DEFAULT_STARTING_ENERGY + "%");
        logger.log("  BASE_INCOME_PERCENT: " + SettingsManager.BASE_INCOME_PERCENT + "%");
        logger.log("  VERTEX_ENERGY_COST: " + SettingsManager.VERTEX_ENERGY_COST);
    }

    public void handle_button_click(String button_clicked) {
        DebugLogger.getInstance().log("Button clicked: " + button_clicked);
        if (Objects.equals(button_clicked, "back")) {
            showControlOverlay = false;
            if (screen instanceof LabScreen){
                Gdx.input.setInputProcessor(((LabScreen) screen).inputProcessor);
            }
            if (screen instanceof GameScreen){
                Gdx.input.setInputProcessor(((GameScreen) screen).inputProcessor);
            }
        }
        if (Objects.equals(button_clicked, "reset")) {
            sliders.reset_sliders();
        }
        if (Objects.equals(button_clicked, "save")) {
            save_slider_settings();
            showControlOverlay = false;
            if (screen instanceof LabScreen){
                Gdx.input.setInputProcessor(((LabScreen) screen).inputProcessor);
            }
            if (screen instanceof GameScreen){
                Gdx.input.setInputProcessor(((GameScreen) screen).inputProcessor);
            }
        }
    }
}
