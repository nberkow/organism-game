package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Arrays;
import java.util.HashMap;

public class LabSettingsButtons {

    LabScreenControlOverlay overlay;
    LabScreen lab_screen;
    OrganismGame game;

    BitmapFont font;

    HashMap<String, float []> overlay_button_coords;
    HashMap<String, float []> side_button_coords;

    float base_button_height;
    float base_button_width;

    public LabSettingsButtons(OrganismGame g, LabScreen lsc, LabScreenControlOverlay oly){
        game = g;
        lab_screen = lsc;
        overlay = oly;
        font = game.fonts.get(16);

        base_button_width = game.VIRTUAL_WIDTH / 10f;
        base_button_height = game.VIRTUAL_HEIGHT / 20f;

        side_button_coords = new HashMap<>();
        overlay_button_coords = new HashMap<>();
    }

    public void render() {

        game.shape_renderer.setColor(Color.CYAN);
        for (String b : side_button_coords.keySet()) {
            draw_button(b, side_button_coords.get(b));
        }

        /* debug rect
        game.shape_renderer.setColor(Color.CYAN);
        game.shape_renderer.rect(
            lab_screen.buttons_box_x, lab_screen.buttons_box_y, lab_screen.buttons_box_w, lab_screen.buttons_box_w
        );*/

        if (lab_screen.show_control_overlay) {
            for (String b : overlay_button_coords.keySet()) {
                draw_button(b, overlay_button_coords.get(b));
            }

            /* debug rect
            game.shape_renderer.rect(
                overlay.buttons_box_x, overlay.buttons_box_y, overlay.buttons_box_w, overlay.buttons_box_h
            );*/

        }
    }

    public String poll_buttons(float x, float y) {
        if (lab_screen.show_control_overlay) {
            for (String b : lab_screen.all_buttons.overlay_button_coords.keySet()) {
                float [] coords = lab_screen.all_buttons.overlay_button_coords.get(b);
                if (x >= coords[0] & x <= (coords[0] + coords[2])){
                    if (y >= coords[1] & y <= (coords[1] + coords[3])){
                        return(b);
                    }
                }
            }
        }

        else {
            for (String b : lab_screen.all_buttons.side_button_coords.keySet()) {
                float[] coords = lab_screen.all_buttons.side_button_coords.get(b);
                if (x >= coords[0] & x <= (coords[0] + coords[2])) {
                    if (y >= coords[1] & y <= (coords[1] + coords[3])) {
                        return(b);
                    }
                }
            }
        }
        return null;
    }


    private void draw_button(String b, float[] button_coords) {

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
