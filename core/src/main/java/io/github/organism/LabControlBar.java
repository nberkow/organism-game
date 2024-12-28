package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public class LabControlBar {
    final float BUTTON_RADIUS = 6;
    float button_width;
    float button_height;

    float buttons_x;
    float buttons_y;
    ArrayList<String> button_labels;
    HashMap<String, GlyphLayout> button_text_layouts;
    HashMap<String, float[]> button_coords;

    ArrayList<String> radio_button_labels;

    ArrayList<String> model_names;

    boolean options_updated;

    OrganismGame game;
    float [] file_box_coords;
    float [] model_box_coords;

    HashMap<String, float []> radio_button_coords;
    HashMap<String, Boolean> radio_button_states;

    LabScreen lab_screen;
    public LabControlBar(LabScreen screen, float b_y) {
        lab_screen = screen;
        game = lab_screen.game;
        options_updated = true;

        buttons_x = lab_screen.controls_x + 10;
        buttons_y = b_y;

        button_width = 120;
        button_height = 20;
        button_coords = new HashMap<>();
        button_text_layouts = new HashMap<>();
        radio_button_coords = new HashMap<>();
        radio_button_states = new HashMap<>();

        button_labels =  new ArrayList<>();
        button_labels.add("new simulation");
        button_labels.add("maps");
        button_labels.add("models");

        radio_button_labels = new ArrayList<>();
        radio_button_labels.add("weighted average");
        radio_button_labels.add("selection only");
        radio_button_labels.add("winner mutates");

        for (int i=0; i<button_labels.size(); i++) {
            button_coords.put(button_labels.get(i), new float[] {
                buttons_x,
                (float) (buttons_y - (button_height * i * 1.2)),
                button_width,
                button_height}
            );
            button_text_layouts.put(button_labels.get(i), new GlyphLayout(game.font, button_labels.get(i)));
        }
        button_coords.get("new simulation")[1] = 20;

        file_box_coords = new float [] {
            lab_screen.controls_x - 10,
            buttons_y - (2 * button_height * (1 + button_labels.size()) * 1.2f),
            this.game.VIRTUAL_WIDTH / 6f,
            this.game.VIRTUAL_HEIGHT / 4f
        };

        model_box_coords = new float [] {
            lab_screen.controls_x - 10,
            buttons_y - (2 * button_height * (1 + button_labels.size()) * 1.2f) - this.game.VIRTUAL_HEIGHT / 4f - 20,
            this.game.VIRTUAL_WIDTH / 6f,
            this.game.VIRTUAL_HEIGHT / 4f
        };

        int r=0;
        boolean state = true;
        for (String radio_button : radio_button_labels) {
            button_text_layouts.put(radio_button, new GlyphLayout(game.font, radio_button));
            radio_button_states.put(radio_button, state);
            radio_button_coords.put(radio_button, new float [] {
                lab_screen.controls_x + 10,
                40 + button_height * 3 + (3 * r * BUTTON_RADIUS)
            });
            r++;
            state = false;
        }
    }

    public void render_option_names(){

        int i =0;
        game.batch.begin();
        if (options_updated) {
            FileHandle[] files = Gdx.files.local("map_configs/").list();
            for (FileHandle file : files) {
                String name = file.name().substring(0, file.name().length() - 4);
                GlyphLayout layout = new GlyphLayout(game.font, name);

                game.font.draw(
                    game.batch,
                    layout,
                    file_box_coords[0] + BUTTON_RADIUS * 1.5f,
                    file_box_coords[1] + file_box_coords[3] - (layout.height * 1.7f * i) - 5);
                i++;

            }
        }
        game.batch.end();
        //options_updated = false;
    }

    public void render_selected_names(){
        int i =0;
        game.batch.begin();
        if (options_updated) {
            FileHandle[] files = Gdx.files.local("model_configs/").list();
            for (FileHandle file : files) {
                String name = file.name().substring(0, file.name().length() - 4);
                GlyphLayout layout = new GlyphLayout(game.font, name);

                game.font.draw(
                    game.batch,
                    layout,
                    model_box_coords[0] + BUTTON_RADIUS * 1.5f,
                    model_box_coords[1] + model_box_coords[3] - (layout.height * 1.7f * i) - 5);
                i++;

            }
        }
        game.batch.end();
    }

    public void render_radio_buttons() {
        for (String b : radio_button_labels) {
            game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shape_renderer.setColor(game.foreground_color);
            float[] coords = radio_button_coords.get(b);
            game.shape_renderer.circle(
                coords[0],
                coords[1],
                BUTTON_RADIUS
            );
            game.shape_renderer.setColor(game.background_color);
            game.shape_renderer.circle(
                coords[0],
                coords[1],
                BUTTON_RADIUS - 1
            );

            boolean filled = radio_button_states.get(b);
            if (filled) {
                game.shape_renderer.setColor(game.foreground_color);
                game.shape_renderer.circle(
                    coords[0],
                    coords[1],
                    BUTTON_RADIUS - 2
                );
            }


            game.shape_renderer.end();
            game.batch.begin();
            game.font.draw(
                game.batch,
                button_text_layouts.get(b),
                coords[0] + BUTTON_RADIUS * 1.5f,
                coords[1] + BUTTON_RADIUS);
            game.batch.end();
        }

    }
    public void render_buttons() {

        for (String b : button_labels) {
            game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shape_renderer.setColor(Color.LIGHT_GRAY);
            float[] coords = button_coords.get(b);
            game.shape_renderer.rect(
                coords[0],
                coords[1],
                coords[2],
                coords[3]
            );

            game.shape_renderer.setColor(game.background_color);
            game.shape_renderer.rect(
                coords[0] + 2,
                coords[1] + 2,
                coords[2] - 4,
                coords[3] - 4
            );

            game.shape_renderer.end();
            game.batch.begin();
            GlyphLayout layout = button_text_layouts.get(b);
            float b_x = coords[0] + coords[2]/2 - layout.width/2;
            float b_y = coords[1] + coords[3]/2 + layout.height/2;
            game.font.getData().setScale(1f);
            game.font.draw(
                game.batch,
                b,
                b_x, b_y);
            game.batch.end();
        }
    }

    public void render() {

        render_buttons();
        render_radio_buttons();

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.setColor(Color.DARK_GRAY);
        game.shape_renderer.rect(
            file_box_coords[0],
            file_box_coords[1],
            file_box_coords[2],
            file_box_coords[3]
        );

        game.shape_renderer.rect(
            model_box_coords[0],
            model_box_coords[1],
            model_box_coords[2],
            model_box_coords[3]
        );

        game.shape_renderer.end();
        render_option_names();
        render_selected_names();
    }
}
