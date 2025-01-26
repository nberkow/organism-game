package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class MapSettingSelectionBoxes {

    final float BUTTON_SPACING = 12;
    final float VERTICAL_SPACING = 17;
    final float BUTTON_RADIUS = 6;
    MapSettingsScreen map_settings_screen;
    GameBoard game_board;
    OrganismGame game;
    float selection_box_x;
    float selection_box_y;
    float selection_box_w;
    float selection_box_h;

    float [] columns_x;

    float max_width;

    BitmapFont font;

    HashMap<String, ArrayList<String>> radio_buttons;
    HashMap<String, String> selected_vals;
    HashMap<String, HashMap<String, float []>> button_coords;
    HashMap<String, HashMap<String, float []>> label_coords;
    ArrayList<String> ordered_keys;

    HashMap<String, GlyphLayout> layouts;
    public MapSettingSelectionBoxes(OrganismGame g, MapSettingsScreen scr){
        map_settings_screen = scr;
        game_board = map_settings_screen.game_board;
        game = g;
        font = game.fonts.get(32);

        selection_box_x = map_settings_screen.controls_x * 1.05f;
        selection_box_y = map_settings_screen.game_board.center_y * .45f;
        selection_box_w = this.game.VIRTUAL_WIDTH / 2.5f;
        selection_box_h = this.game.VIRTUAL_HEIGHT / 3f;

        columns_x = new float [] {selection_box_x, selection_box_x + selection_box_w/2};

        radio_buttons = new HashMap<>();
        radio_buttons.put("human players", new ArrayList<>(Arrays.asList("0", "1", "2")));
        radio_buttons.put("players", new ArrayList<>(Arrays.asList("3", "6")));
        radio_buttons.put("layout", new ArrayList<>(Arrays.asList("radial", "random")));
        radio_buttons.put("opponents", new ArrayList<>(Arrays.asList("easy", "medium", "hard")));

        selected_vals = new HashMap<>();
        selected_vals.put("human players", "1");
        selected_vals.put("players", "3");
        selected_vals.put("player layout", "radial");
        selected_vals.put("layout", "radial");
        selected_vals.put("opponents", "medium");

        ordered_keys = new ArrayList<>(Arrays.asList("human players", "players", "layout", "opponents"));
        layouts = new HashMap<>();

        max_width = 0;
        GlyphLayout layout;
        for (String k : ordered_keys){
            layout = new GlyphLayout(font, k);
            layouts.put(k, layout);
            if (layout.width > max_width){
                max_width = layout.width;
            }
            for (String p : radio_buttons.get(k)){
                layout = new GlyphLayout(font, p);
                layouts.put(p, layout);
                if (layout.width > max_width){
                    max_width = layout.width;
                }
            }
        }

        set_initial_layout();
    }

    public void set_initial_layout(){

        label_coords = new HashMap<>();
        button_coords = new HashMap<>();

        float col_y = selection_box_y + selection_box_h * 0.9f;

        int col_split = (int) Math.ceil(radio_buttons.size() / 2f);
        int start = 0;
        HashMap<String, float []> coords;
        HashMap<String, float [] > button_xy;
        for (float x : columns_x) {
            float j = 0;
            for (int i = start; i < col_split; i++) {
                String n = ordered_keys.get(i);
                ArrayList<String> options = radio_buttons.get(n);

                float[] xy = new float[]{x, col_y - j * VERTICAL_SPACING};
                button_xy = new HashMap<>();
                coords = new HashMap<>();
                coords.put(n, xy);

                j += 1.2f;

                for (String p : options) {
                    xy = new float[]{x, col_y - j * VERTICAL_SPACING};
                    coords.put(p, xy);
                    j += 1;

                    float [] bxy = new float [] {x + max_width, col_y - j * VERTICAL_SPACING + BUTTON_RADIUS * 2};
                    button_xy.put(p, bxy);

                }
                j += 1.2f;
                label_coords.put(n, coords);
                button_coords.put(n, button_xy);
            }
            start = col_split;
            col_split = radio_buttons.size();
        }

    }

    public void render() {

        font.getData().setScale(1f);
        game.batch.begin();
        GlyphLayout layout;
        for (String k : ordered_keys){

            float [] xy = label_coords.get(k).get(k);
            layout = layouts.get(k);
            font.draw(
                game_board.batch, layout, xy[0], xy[1]
            );
            for (String p : label_coords.get(k).keySet()){
                layout = layouts.get(p);
                if (!k.equals(p)) {
                    xy = label_coords.get(k).get(p);
                    font.draw(
                        game_board.batch, layout, xy[0], xy[1]
                    );
                }
            }
        }
        game.batch.end();

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (String k : ordered_keys) {
            for (String p : button_coords.get(k).keySet()){
                float [] xy = button_coords.get(k).get(p);
                String selected = selected_vals.get(k);

                game.shape_renderer.setColor(game_board.game.foreground_color);
                game.shape_renderer.circle(xy[0], xy[1], BUTTON_RADIUS);

                game.shape_renderer.setColor(game_board.game.background_color);
                game.shape_renderer.circle(xy[0], xy[1], BUTTON_RADIUS - 1);

                if (selected.equals(p)) {
                    game.shape_renderer.setColor(game_board.game.foreground_color);
                    game.shape_renderer.circle(xy[0], xy[1], BUTTON_RADIUS - 2);
                }
            }
        }
        game.shape_renderer.end();
    }

    public String [] poll_selection_boxes(float screenX, float screenY){

        String [] clicked = new String [2];
        for (String k : ordered_keys){
            HashMap<String, float []> setting = button_coords.get(k);
            for (String p : setting.keySet()){
                float [] xy = setting.get(p);
                if (Math.abs(xy[0] - screenX) < BUTTON_RADIUS &&
                    Math.abs(xy[1] - screenY) < BUTTON_RADIUS) {
                    clicked[0] = k;
                    clicked[1] = p;
                }
            }
        }
        return clicked;
    }
}
