package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

public class MapSettingsInputProcessor implements InputProcessor {

    MapSettingsScreen map_screen;

    String dragging_slider;
    String [] radio_press;
    boolean preview_button_click;
    boolean save_button_click;
    boolean start_button_click;
    public MapSettingsInputProcessor(MapSettingsScreen screen) {
        map_screen = screen;
        dragging_slider = null;
        radio_press = new String [2];
        preview_button_click = false;
        save_button_click = false;
        start_button_click = false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    /**
     * @param character The character
     * @return
     */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /**
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        Vector2 touchPos = new Vector2(screenX, screenY);
        map_screen.game_board.game.viewport.unproject(touchPos);
        dragging_slider = map_screen.sliders.poll_sliders(touchPos.x, touchPos.y);
        if (dragging_slider == null) {
            map_screen.sliders.update_on_single_click(touchPos.x, touchPos.y);
        }

        String button_press = map_screen.buttons.poll_buttons(touchPos.x, touchPos.y);
        radio_press = map_screen.selection_boxes.poll_selection_boxes(touchPos.x, touchPos.y);
        if (Objects.equals(button_press, "preview")) {
            preview_button_click = true;
        }
        if (Objects.equals(button_press, "start")) {
            start_button_click = true;
        }
        return false;
    }

    /**
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        if (preview_button_click) {
            map_screen.update_map();
            preview_button_click = false;
        }

        if (save_button_click) {
            map_screen.update_map();
            preview_button_click = false;
        }

        if (radio_press[0] != null){
            map_screen.selection_boxes.selected_vals.put(radio_press[0], radio_press[1]);
        }

        if (dragging_slider == null){
            return false;
        }

        Vector2 touchPos = new Vector2(screenX, screenY);
        map_screen.game_board.game.viewport.unproject(touchPos);

        float[][] tick_coords = map_screen.sliders.bar_tick_coords.get(dragging_slider);
        float [] slider_coords = map_screen.sliders.slider_coords.get(dragging_slider);
        float [] slider_vals = map_screen.sliders.slider_values.get(dragging_slider);

        float min_dist = map_screen.sliders.bar_width;
        float min_dist_x = 0;
        float slider_val = 0;

        int t = 0;
        for (float[] tickCoord : tick_coords) {
            float x = tickCoord[0];
            if (Math.abs(x - touchPos.x) < min_dist) {
                min_dist = Math.abs(x - touchPos.x);
                min_dist_x = x - map_screen.sliders.slider_width / 2;
                slider_val = slider_vals[t];
            }
            t++;
        }
        map_screen.sliders.slider_selected_vals.put(dragging_slider, slider_val);
        slider_coords[0] = min_dist_x;

        dragging_slider = null;
        return false;
    }


    /**
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @param button  the button
     * @return
     */
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * @param screenX
     * @param screenY
     * @param pointer the pointer for the event.
     * @return
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        Vector2 touchPos = new Vector2(screenX, screenY);
        map_screen.game_board.game.viewport.unproject(touchPos);
        float x = touchPos.x - map_screen.sliders.slider_width/2f;

        float [] coord = map_screen.sliders.slider_coords.get(dragging_slider);
        float [] bar_coord = map_screen.sliders.bar_coords.get(dragging_slider);

        if (dragging_slider != null) {
            coord[0] = Math.min(Math.max(bar_coord[0], x), x);
        }

        return false;

    }

    /**
     * @param screenX
     * @param screenY
     * @return
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * @param amountX the horizontal scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @param amountY the vertical scroll amount, negative or positive depending on the direction the wheel was scrolled.
     * @return
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
