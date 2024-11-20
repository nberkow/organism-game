package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;

public class MapSettingsButtons {

    float button_width;
    float button_height;

    float buttons_x;
    float buttons_y;
    float [] preview_button_coords;
    float [] save_button_coords;
    float [] start_button_coords;
    HashMap<String, float []> buttons;
    HashMap<String, GlyphLayout> button_text_layouts;
    MapSettingsScreen map_settings_screen;

    GameBoard game_board;
    OrganismGame game;

    public MapSettingsButtons(MapSettingsScreen msc) {
        map_settings_screen = msc;
        game = map_settings_screen.game;
        game_board = map_settings_screen.game_board;

        buttons = new HashMap<>();
        buttons_x = map_settings_screen.controls_x;
        buttons_y = map_settings_screen.buttons_y;

        button_width = 120;
        button_height = 20;

        preview_button_coords = new float[]{
            buttons_x,
            buttons_y,
            button_width,
            button_height
        };
        buttons.put("preview", preview_button_coords);

        save_button_coords = new float[]{
            buttons_x + button_width * 1.1f,
            buttons_y,
            button_width,
            button_height
        };
        buttons.put("save", save_button_coords);

        start_button_coords = new float[]{
            buttons_x + button_width * 2.2f,
            buttons_y,
            button_width,
            button_height
        };
        buttons.put("start", start_button_coords);

        button_text_layouts = new HashMap<>();
        button_text_layouts.put("preview", new GlyphLayout(game_board.font, "preview"));
        button_text_layouts.put("save", new GlyphLayout(game_board.font, "save"));
        button_text_layouts.put("start", new GlyphLayout(game_board.font, "start"));

    }

    public String poll_buttons(float screenX, float screenY) {

        for (String button_name : buttons.keySet()) {
            float[] button_coords = buttons.get(button_name);
            if (screenX >= button_coords[0] && screenX <= button_coords[0] + button_coords[2]) {
                if (screenY >= button_coords[1] && screenY <= button_coords[1] + button_coords[3]) {
                    return button_name;
                }
            }
        }
        return null;
    }

    public void render() {

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        /*game_board.shape_renderer.rect(
            buttons_x, buttons_y, map_settings_screen.controls_w, button_height
        */;
        game_board.shape_renderer.end();

        for (String b : buttons.keySet()) {
            game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
            game_board.shape_renderer.setColor(Color.LIGHT_GRAY);
            float[] button_coords = buttons.get(b);
            game_board.shape_renderer.rect(
                button_coords[0],
                button_coords[1],
                button_coords[2],
                button_coords[3]
            );

            game_board.shape_renderer.setColor(game_board.background_color);
            game_board.shape_renderer.rect(
                button_coords[0] + 2,
                button_coords[1] + 2,
                button_coords[2] - 4,
                button_coords[3] - 4
            );

            game_board.shape_renderer.end();
            game_board.batch.begin();
            GlyphLayout layout = button_text_layouts.get(b);
            float b_x = button_coords[0] + button_coords[2]/2 - layout.width/2;
            float b_y = button_coords[1] + button_coords[3]/2 + layout.height/2;
            game_board.font.getData().setScale(1f);
            game_board.font.draw(
                game_board.batch,
                b,
                b_x, b_y);
            game_board.batch.end();
        }
    }
}
