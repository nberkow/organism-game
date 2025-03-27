package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public class MapSettingsSliders {

    final float TICK_WIDTH = 2f;
    float slider_box_x;
    float slider_box_y;
    float slider_box_w;
    float slider_box_h;
    OrganismGame game;
    MapSettingsScreen map_settings_screen;
    GameBoard game_board;
    float bar_x;
    float bar_width;
    float bar_height;
    float slider_width;
    float slider_height;
    float bar_spacing;
    HashMap<String, float []> slider_parameters;
    HashMap<String, float []> slider_coords;
    HashMap<String, float []> bar_coords;
    HashMap<String, float [][]> bar_tick_coords;
    HashMap<String, float []> label_coords;
    ArrayList<String> slider_label_order;
    HashMap<String, float []> slider_values;

    HashMap<String, Float> slider_selected_vals;

    BitmapFont font;

    public MapSettingsSliders(OrganismGame g, MapSettingsScreen msc) {
        game = g;
        map_settings_screen = msc;
        game_board = map_settings_screen.game_board;
        font = game.fonts.get(16);

        slider_box_x = map_settings_screen.controls_x;
        slider_box_y = game_board.centerY;
        slider_box_w = map_settings_screen.controls_w;
        slider_box_h = this.game.VIRTUAL_HEIGHT / 1.8f;

        bar_width = slider_box_w * .9f;
        bar_height = 4;
        bar_x = slider_box_x + (slider_box_w - bar_width)/2;

        slider_width = bar_width * .1f;
        slider_height = bar_height * 3;

        slider_parameters = new HashMap<>();
        slider_parameters.put("radius", new float[]{4f, 30f, 1f, map_settings_screen.DEFAULT_SIZE});
        slider_parameters.put("resources", new float[]{0f, 3, 1/3f, 1});
        slider_parameters.put("density", new float[]{1, 6, 1, 3f});
        slider_parameters.put("starts", new float[]{0, 1.2f, .2f, 0f});

        slider_label_order = new ArrayList<>();
        slider_label_order.add("radius");
        slider_label_order.add("resources");
        slider_label_order.add("density");
        slider_label_order.add("starts");

        bar_spacing = slider_box_h / (.7f + slider_parameters.size());
        bar_coords = new HashMap<>();
        label_coords = new HashMap<>();
        slider_coords = new HashMap<>();
        bar_tick_coords = new HashMap<>();
        slider_values = new HashMap<>();
        slider_selected_vals = new HashMap<>();



        load_initial_positions();
    }

    private void load_initial_positions() {
        float y = slider_box_y + slider_box_h - bar_spacing;
        float slider_x;
        int ticks;
        float tick_spacing;

        // rect coordinates in order
        float [] bar_coord;
        float [] label_coord;
        float [] slider_coord;

        // rect coordinates in order, per tick
        float [][] bar_tick_coord;

        for (String p : slider_label_order){

            label_coord = new float[] {map_settings_screen.controls_x, y + slider_height * 2};
            label_coords.put(p, label_coord);

            // lower bound, upper bound, step size, current value
            float [] values = slider_parameters.get(p);

            ticks = (int) ((values[1] - values[0]) / values[2]);
            bar_tick_coord = new float [ticks+1][4];
            float [] tick_vals = new float [ticks+1];

            tick_spacing = bar_width / ticks;
            for (int i=0; i<=ticks; i++){
                bar_tick_coord[i][0] = bar_x + tick_spacing * i - 1;
                bar_tick_coord[i][1] = y - slider_height * .6f;
                bar_tick_coord[i][2] = TICK_WIDTH;
                bar_tick_coord[i][3] = slider_height * 1.2f;
                tick_vals[i] = i * values[2] + values[0];
            }
            bar_tick_coords.put(p, bar_tick_coord);
            slider_values.put(p, tick_vals);
            slider_selected_vals.put(p, values[3]);

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

    public void render(){

        /*
        shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        shape_renderer.rect(
            slider_box_x, slider_box_y, slider_box_w, slider_box_h
        );
        shape_renderer.end();*/

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (String p : slider_label_order){

            float[][] tick_coord = bar_tick_coords.get(p);

            for (float[] rect_coords : tick_coord) {
                game.shapeRenderer.setColor(Color.WHITE);
                game.shapeRenderer.rect(
                    rect_coords[0],
                    rect_coords[1],
                    rect_coords[2],
                    rect_coords[3]
                );
            }

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

        game.batch.end();
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
                float [] values = slider_values.get(n);

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
                slider_selected_vals.put(n, best_dist_val);
                float [] s_coord = slider_coords.get(n);
                s_coord[0] = best_dist_pos - slider_width/2;
                slider_coords.put(n, s_coord);
            }
        }
    }
}
