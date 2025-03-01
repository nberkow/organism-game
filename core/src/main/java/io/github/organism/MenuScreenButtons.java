package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;

public class MenuScreenButtons {

    SettingsOverlay overlay;
    MenuScreen menu_screen;
    OrganismGame game;
    BitmapFont font;
    HashMap<String, float []> button_coords;

    float base_button_height;
    float base_button_width;
    float v_space = base_button_height / 5;

    public MenuScreenButtons(OrganismGame g, MenuScreen lsc){
        game = g;
        menu_screen = lsc;
        font = game.fonts.get(16);

        base_button_width = game.VIRTUAL_WIDTH / 10f;
        base_button_height = game.VIRTUAL_HEIGHT / 20f;

        button_coords = new HashMap<>();
    }

    public void render() {

        if (game.shape_renderer == null){
            return;
        }

        game.shape_renderer.setColor(Color.CYAN);
        for (String b : button_coords.keySet()) {
            draw_button(b, button_coords.get(b));
        }
    }

    public void setup_buttons(String [] button_names){
        float total_button_height = button_names.length * (base_button_height + v_space) - v_space;
        float base_y = (game.VIRTUAL_HEIGHT - total_button_height)/2;
        float button_x = (game.VIRTUAL_WIDTH - base_button_width)/2;

        for (int i=button_names.length-1; i>=0; i--){
            button_coords.put(button_names[i], new float[] {
                button_x,
                base_y + (i * (base_button_height + v_space)),
                base_button_width,
                base_button_height
            });
        }
    }

    public String poll_buttons(float x, float y) {

        for (String b : menu_screen.buttons.button_coords.keySet()) {
            float[] coords = menu_screen.buttons.button_coords.get(b);
            if (x >= coords[0] & x <= (coords[0] + coords[2])) {
                if (y >= coords[1] & y <= (coords[1] + coords[3])) {
                    return(b);
                }
            }
        }

        return null;
    }


    private void draw_button(String b, float[] button_coords) {

        if (game.batch == null){
            return;
        }

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game.shape_renderer.rect(
            button_coords[0],
            button_coords[1],
            button_coords[2],
            button_coords[3]
        );
        game.shape_renderer.end();

        game.batch.begin();

        font.setColor(game.foreground_color);
        GlyphLayout layout = new GlyphLayout(font, b);

        float b_x = Math.round(button_coords[0] + button_coords[2] / 2 - layout.width / 2);
        float b_y = Math.round(button_coords[1] + button_coords[3] / 2 + layout.height / 2);

        font.draw(
            game.batch,
            b,
            b_x, b_y);
        game.batch.end();
    }
}
