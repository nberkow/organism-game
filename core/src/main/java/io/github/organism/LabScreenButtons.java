package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;

public class LabScreenButtons {

    SettingsOverlay overlay;
    LabScreen lab_screen;
    OrganismGame game;

    BitmapFont font;
    HashMap<String, float []> side_button_coords;

    float base_button_height;
    float base_button_width;

    public LabScreenButtons(OrganismGame g, LabScreen lsc){
        game = g;
        lab_screen = lsc;
        font = game.fonts.get(16);

        base_button_width = game.VIRTUAL_WIDTH / 10f;
        base_button_height = game.VIRTUAL_HEIGHT / 20f;

        side_button_coords = new HashMap<>();
    }

    public void render() {

        if (game.shape_renderer == null){
            return;
        }

        game.shape_renderer.setColor(Color.CYAN);
        for (String b : side_button_coords.keySet()) {
            draw_button(b, side_button_coords.get(b));
        }

    }

    public String poll_buttons(float x, float y) {

        for (String b : lab_screen.buttons.side_button_coords.keySet()) {
            float[] coords = lab_screen.buttons.side_button_coords.get(b);
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
