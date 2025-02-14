package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public class LabScreenControlOverlay {

    float slider_box_x;
    float slider_box_y;
    float slider_box_w;
    float slider_box_h;

    float buttons_box_x;
    float buttons_box_y;
    float buttons_box_w;
    float buttons_box_h;
    OrganismGame game;
    LabScreen lab_screen;
    BitmapFont font;
    SliderGroup sliders;

    String [] button_names;

    public LabScreenControlOverlay(OrganismGame g, LabScreen lsc) {
        game = g;
        lab_screen = lsc;
        font = game.fonts.get(16);
        setup_sliders();



    }

    public void setup_sliders() {

        slider_box_w = lab_screen.overlay_w * .75f;
        slider_box_h = lab_screen.overlay_h * .55f;

        slider_box_x = (game.VIRTUAL_WIDTH - slider_box_w) / 2f;
        slider_box_y = (game.VIRTUAL_HEIGHT - slider_box_h) / 2f + game.VIRTUAL_HEIGHT / 8f;

        sliders = new SliderGroup(game, lab_screen, slider_box_x, slider_box_y, slider_box_w, slider_box_h);

        sliders.add_slider("resource value", 1, 6, 0.2f, SettingsManager.BASE_RESOURCE_VALUE);
        sliders.add_slider("attack enemy cost", 0, 24, .2f, SettingsManager.VERTEX_COST_REMOVE_ENEMY);
        sliders.add_slider("attack ally cost", 0, 24, .2f, SettingsManager.VERTEX_COST_REMOVE_ALLY);
        sliders.add_slider("attack neutral cost", 0, 24, .2f, SettingsManager.VERTEX_COST_REMOVE_NEUTRAL);
        sliders.add_slider("claim vertex cost", 0, 24, .2f, SettingsManager.VERTEX_COST_TAKE_VERTEX);
        sliders.add_slider("speed", 1, 5, 1, 1f);
        sliders.load_initial_positions();
    }

    public void setup_buttons() {

        buttons_box_w = slider_box_w;
        buttons_box_h = lab_screen.overlay_h * .20f;

        buttons_box_x = slider_box_x;
        buttons_box_y = (game.VIRTUAL_HEIGHT - slider_box_h) / 2f - game.VIRTUAL_HEIGHT / 10f;

        button_names = new String[]{
            "back",
            "reset",
            "save"
        };

        float combined_button_width = lab_screen.all_buttons.base_button_width * button_names.length;
        float spacing = (buttons_box_w - combined_button_width) / (button_names.length + 1);

        for (int i=0; i<button_names.length; i++) {
            lab_screen.all_buttons.overlay_button_coords.put(button_names[i],
                new float[]{
                    buttons_box_x + (spacing) * (i+1) + (lab_screen.all_buttons.base_button_width * i),
                    buttons_box_y,
                    lab_screen.all_buttons.base_button_width,
                    lab_screen.all_buttons.base_button_height
                });
        }
    }

    public void render(){

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.setColor(Color.BLACK);
        game.shape_renderer.rect(
            lab_screen.overlay_x, lab_screen.overlay_y, lab_screen.overlay_w, lab_screen.overlay_h
        );
        game.shape_renderer.end();

        float margin = lab_screen.overlay_w * 0.05f;
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game.shape_renderer.setColor(Color.DARK_GRAY);
        game.shape_renderer.rect(
            lab_screen.overlay_x + margin,
            lab_screen.overlay_y + margin,
            lab_screen.overlay_w - (margin * 2),
            lab_screen.overlay_h - (margin * 2)
        );
        game.shape_renderer.end();

        sliders.render();

    }
}
