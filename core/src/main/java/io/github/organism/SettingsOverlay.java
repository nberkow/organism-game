package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.Objects;

public class SettingsOverlay {


    public InputProcessor input_processor;
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
        overlay_x = x;
        overlay_y = y;
        overlay_h = h;
        overlay_w = w;
        savedSettings = new HashMap<>();
    }

    public void setupSliders() {

        slider_box_w = overlay_w * .75f;
        slider_box_h = overlay_h * .55f;

        slider_box_x = (game.VIRTUAL_WIDTH - slider_box_w) / 2f;
        slider_box_y = (game.VIRTUAL_HEIGHT - slider_box_h) / 2f + game.VIRTUAL_HEIGHT / 8f;

        sliders = new SliderGroup(game, screen, slider_box_x, slider_box_y, slider_box_w, slider_box_h);

        sliders.addSlider("resource value", 1, 6, 0.1f, SettingsManager.BASE_RESOURCE_VALUE);
        sliders.addSlider("attack enemy cost", 0, 24, .2f, SettingsManager.VERTEX_COST_REMOVE_ENEMY);
        sliders.addSlider("attack ally cost", 0, 24, .2f, SettingsManager.VERTEX_COST_REMOVE_ALLY);
        sliders.addSlider("attack neutral cost", 0, 24, .2f, SettingsManager.VERTEX_COST_REMOVE_NEUTRAL);
        sliders.addSlider("claim vertex cost", 0, 24, .2f, SettingsManager.VERTEX_COST_TAKE_VERTEX);
        sliders.addSlider("speed", 1, 7, 1, 7f);
        sliders.addSlider("iterations", 1, 9, 1, 1f);
        sliders.loadInitialPositions();
        save_slider_settings(); // populates the data structure with defaults
    }

    public void setupButtons() {

        buttons = new SettingsOverlayButtons(game, screen, this);
        button_names = new String [] {
            "back", "reset", "save"
        };

        buttons_box_w = slider_box_w;
        buttons_box_h = overlay_h * .20f;

        buttons_box_x = slider_box_x;
        buttons_box_y = (game.VIRTUAL_HEIGHT - slider_box_h) / 2f - game.VIRTUAL_HEIGHT / 10f;

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
        for (String p : sliders.sliderLabelOrder){
            float val = sliders.sliderSelectedValues.get(p);
            if (Objects.equals(p, "iterations")) {
                val = (float) Math.pow(10, val);
            }
            if (Objects.equals(p, "speed")) {
                val = (float) Math.pow(2, val);
            }
            savedSettings.put(p, val);
        }
    }

    public void handle_button_click(String button_clicked) {
        System.out.println(button_clicked);
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
            sliders.resetSliders();
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
