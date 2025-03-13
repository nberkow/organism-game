package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SliderGroup {
    OrganismGame game;
    Screen screen;
    float bar_x;
    float bar_width;
    float bar_height;
    float slider_width;
    float slider_height;
    float bar_spacing;

    HashMap<String, float[]> slider_parameters;
    HashMap<String, float[]> slider_coords;
    HashMap<String, float[]> bar_coords;
    HashMap<String, float[][]> bar_tick_coords;
    HashMap<String, float[]> label_coords;
    HashMap<String, float[]> value_coords;
    ArrayList<String> slider_label_order;
    HashMap<String, float[]> slider_tick_values;
    HashMap<String, Float> slider_selected_values;

    float slider_box_w;
    float slider_box_h;

    float slider_box_x;
    float slider_box_y;

    BitmapFont font;

    public SliderGroup(OrganismGame g, Screen s, float x, float y, float w, float h) {
        game = g;
        screen = s;

        slider_box_x = x;
        slider_box_y = y;
        slider_box_w = w;
        slider_box_h = h;

        bar_width = slider_box_w * .8f;
        bar_height = 4;
        bar_x = slider_box_x + (slider_box_w - bar_width)/2;

        slider_width = bar_width * .1f;
        slider_height = bar_height * 3;

        font = game.fonts.get(16);

        bar_coords = new HashMap<>();
        label_coords = new HashMap<>();
        value_coords = new HashMap<>();
        slider_coords = new HashMap<>();
        bar_tick_coords = new HashMap<>();
        slider_tick_values = new HashMap<>();
        slider_selected_values = new HashMap<>();
        slider_parameters = new HashMap<>();
        slider_label_order = new ArrayList<>();
    }

    public void add_slider(String label, float range_min, float range_max, float increment, float starting_val){
        slider_label_order.add(label);
        slider_parameters.put(label, new float[]{range_min, range_max, increment, starting_val});
        slider_selected_values.put(label, starting_val);
    }

    public void reset_sliders() {
        float slider_x;
        for (String s : slider_label_order) {
            float [] values = slider_parameters.get(s);
            slider_x = (values[3] - values[0]) / (values[1] - values[0]) * bar_width;

            float[] coords = slider_coords.get(s);
            float[] new_coords = new float [] {
                bar_x + slider_x - slider_width/2,
                coords[1],
                slider_width,
                slider_height
            };

            slider_coords.put(s, new_coords);
            slider_selected_values.put(s, values[3]);
        }
    }

    public void load_initial_positions() {

        bar_spacing = slider_box_h / (.7f + slider_parameters.size());
        float y = slider_box_y + slider_box_h - bar_spacing;
        float slider_x;
        int ticks;
        float tick_spacing;

        // rect coordinates in order
        float [] bar_coord;
        float [] label_coord;
        float [] value_coord;
        float [] slider_coord;

        // rect coordinates in order, per tick
        float [][] bar_tick_coord;

        for (String p : slider_label_order){

            label_coord = new float[] {slider_box_x, y + slider_height * 2};
            label_coords.put(p, label_coord);

            value_coord = new float[] {slider_box_x + slider_box_w, y + slider_height * 2};
            value_coords.put(p, value_coord);

            // lower bound, upper bound, step size, current value
            float [] values = slider_parameters.get(p);

            ticks = (int) ((values[1] - values[0]) / values[2]);
            bar_tick_coord = new float [ticks+1][4];
            float [] tick_vals = new float [ticks+1];

            tick_spacing = bar_width / ticks;
            for (int i=0; i<=ticks; i++){
                bar_tick_coord[i][0] = bar_x + tick_spacing * i - 1;
                bar_tick_coord[i][1] = y - slider_height * .6f;
                bar_tick_coord[i][2] = 0;
                bar_tick_coord[i][3] = slider_height * 1.2f;
                tick_vals[i] = i * values[2] + values[0];
            }
            bar_tick_coords.put(p, bar_tick_coord);
            slider_tick_values.put(p, tick_vals);
            slider_selected_values.put(p, values[3]);


            bar_coord = new float []{
                bar_x,
                y - bar_height / 2,
                bar_width,
                bar_height
            };
            bar_coords.put(p, bar_coord);

            slider_x = (values[3] - values[0]) / (values[1] - values[0]) * bar_width;
            slider_coord = new float [] {
                bar_x + slider_x - slider_width/2,
                y - slider_height/2,
                slider_width,
                slider_height
            };
            slider_coords.put(p, slider_coord);

            y -= bar_spacing;
        }
    }

    public String poll_sliders(float screenX, float screenY) {

        String r = null;
        for (String p : slider_label_order){
            float [] coord = slider_coords.get(p);
            if (screenX > coord[0] && screenX < coord[0] + coord[2]){
                if (screenY > coord[1] && screenY < coord[1] + coord[3]){
                    r = p;
                }
            }
        }
        return r;
    }


    public void update_on_single_click(float x, float y) {

        for (String n : bar_coords.keySet()){

            float best_dist = Float.MAX_VALUE;
            float best_dist_val = -1;
            float best_dist_pos = -1;
            float dist;

            float [] coord = bar_coords.get(n);
            if (x > coord[0] && x < coord[0] + coord[2] &&
                y > coord[1] - slider_height && y < coord[1] + coord[3] + slider_height){
                float[][] tick_coord = bar_tick_coords.get(n);
                float [] values = slider_tick_values.get(n);

                for (int i=0; i<tick_coord.length; i++) {
                    float[] rect_coords = tick_coord[i];
                    dist = Math.abs(rect_coords[0] - x);
                    if (dist < best_dist){
                        best_dist = dist;
                        best_dist_val = values[i];
                        best_dist_pos = rect_coords[0];
                    }
                }
            }

            if (best_dist < Float.MAX_VALUE) {
                slider_selected_values.put(n, best_dist_val);
                float [] s_coord = slider_coords.get(n);
                s_coord[0] = best_dist_pos - slider_width/2;
                slider_coords.put(n, s_coord);
            }
        }
    }

    public void render(){

        /*
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.setColor(Color.CYAN);
        game.shape_renderer.rect(
            overlay.slider_box_x, overlay.slider_box_y, overlay.slider_box_w, overlay.slider_box_h
        );
        game.shape_renderer.end();
        //System.out.println(overlay.slider_box_x + "\t" + overlay.slider_box_y + "\t" + overlay.slider_box_w + "\t" + overlay.slider_box_h);
        */


        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (String p : slider_label_order){

            game.shapeRenderer.setColor(Color.DARK_GRAY);
            float[] bar_coord = bar_coords.get(p);
            game.shapeRenderer.rect(
                bar_coord[0],
                bar_coord[1],
                bar_coord[2],
                bar_coord[3]
            );

            game.shapeRenderer.setColor(Color.GRAY);
            float[] slider_coord = slider_coords.get(p);

            game.shapeRenderer.rect(
                slider_coord[0],
                slider_coord[1],
                slider_coord[2],
                slider_coord[3]
            );
        }
        game.shapeRenderer.end();

        game.batch.begin();

        for (String p : slider_label_order){
            float[] label_coord = label_coords.get(p);
            font.draw(
                game.batch,
                p,
                label_coord[0],
                label_coord[1]);
        }

        for (String p : slider_label_order){
            float[] value_coord = value_coords.get(p);
            float val = slider_selected_values.get(p);
            if (Objects.equals(p, "iterations")) {
                val = (float) Math.pow(10, val);
            }
            if (Objects.equals(p, "speed")) {
                val = (float) Math.pow(2, val);
            }
            GlyphLayout g = new GlyphLayout(font, String.format(Locale.US,"%.2f", val));
            font.draw(
                game.batch,
                g,
                value_coord[0] - g.width,
                value_coord[1]
            );
        }

        game.batch.end();
    }


}
