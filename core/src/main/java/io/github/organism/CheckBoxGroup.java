package io.github.organism;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CheckBoxGroup {

    float box_x;
    float box_y;
    float box_w;
    float box_h;

    float spacing;

    float inset;

    HashMap<String, Float> box_y_coords;
    float box_side_length;
    HashMap<String, Boolean> checkbox_states;
    ArrayList<String> labels;
    OrganismGame game;
    BitmapFont font;
    public CheckBoxGroup(OrganismGame g, float x, float y, float w, float h){

        game = g;

        box_x = x;
        box_y = y;
        box_w = w;
        box_h = h;

        inset = .7f;

        box_side_length = game.VIRTUAL_HEIGHT / 40f;
        font = game.fonts.get(16);

        checkbox_states = new HashMap<>();
        labels = new ArrayList<>();
    }

    public void add_checkbox(String label, boolean value){
        labels.add(label);
        checkbox_states.put(label, value);
    }

    public void calculate_coords() {
        float free_vertical_space = box_h = labels.size() * box_side_length;
        spacing = free_vertical_space / (labels.size() + 1);
        box_y_coords = new HashMap<>();

        int i = 0;
        for (String s : labels) {
            box_y_coords.put(s, i * (spacing + box_side_length));
            i++;
        }
    }

    public String poll_boxes(float x, float y){

        for (String s : box_y_coords.keySet()) {
            float checkbox_y = box_y_coords.get(s) + box_y;
            if (x >= box_x & x <= box_x + box_side_length) {
                if (y >= checkbox_y & y <= checkbox_y + box_side_length){

                    return s;
                }
            }
        }
        return null;
    }

    public void render(){

        for (String s : labels){
            float y = box_y_coords.get(s) + box_y;
            boolean state = checkbox_states.get(s);
            game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
            game.shape_renderer.rect(
                box_x,
                y,
                box_side_length, box_side_length
            );
            game.shape_renderer.end();

            if (state) {
                game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
                game.shape_renderer.rect(
                    box_x + (box_side_length * (1-inset)/2),
                    y + (box_side_length * (1-inset)/2),
                    box_side_length * inset, box_side_length * inset
                );
                game.shape_renderer.end();
            }

            game.batch.begin();

            GlyphLayout g = new GlyphLayout(font, s);
            float layout_y = y + (box_side_length / 2) + (g.height / 2);
            float layout_x = box_x + box_side_length * 1.5f;
            font.draw(game.batch, g, layout_x, layout_y);
            game.batch.end();
        }

    }
}
