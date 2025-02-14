package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

public class LabInputProcessor implements InputProcessor {
    LabScreen lab_screen;
    String dragging_slider;
    String button_clicked;

    public LabInputProcessor(LabScreen screen){

        lab_screen = screen;
        dragging_slider = null;
        button_clicked = null;
    }

    /**
     * @param keycode one of the constants in {@link Input.Keys}
     * @return
     */
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    /**
     * @param keycode one of the constants in {@link Input.Keys}
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
        lab_screen.game.viewport.unproject(touchPos);

        dragging_slider = lab_screen.overlay.sliders.poll_sliders(touchPos.x, touchPos.y);
        if (dragging_slider == null) {
            lab_screen.overlay.sliders.update_on_single_click(touchPos.x, touchPos.y);
        }

        button_clicked = lab_screen.all_buttons.poll_buttons(touchPos.x, touchPos.y);

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

        Vector2 touchPos = new Vector2(screenX, screenY);
        lab_screen.overlay.game.viewport.unproject(touchPos);

        if (button_clicked != null) {
            String button_up = lab_screen.all_buttons.poll_buttons(touchPos.x, touchPos.y);

            if (Objects.equals(button_up, button_clicked)) {
                lab_screen.handle_button_click(button_clicked);
            }
        }

        if (dragging_slider == null){
            return false;
        }

        float[][] tick_coords = lab_screen.overlay.sliders.bar_tick_coords.get(dragging_slider);
        float [] slider_coords = lab_screen.overlay.sliders.slider_coords.get(dragging_slider);
        float [] slider_vals = lab_screen.overlay.sliders.slider_tick_values.get(dragging_slider);

        float min_dist = lab_screen.overlay.sliders.bar_width;
        float min_dist_x = 0;
        float slider_val = 0;

        int t = 0;
        for (float[] tickCoord : tick_coords) {
            float x = tickCoord[0];
            if (Math.abs(x - touchPos.x) < min_dist) {
                min_dist = Math.abs(x - touchPos.x);
                min_dist_x = x - lab_screen.overlay.sliders.slider_width / 2;
                slider_val = slider_vals[t];
            }
            t++;
        }
        lab_screen.overlay.sliders.slider_selected_values.put(dragging_slider, slider_val);
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
        lab_screen.game.viewport.unproject(touchPos);
        float x = touchPos.x - lab_screen.overlay.sliders.slider_width/2f;

        float [] coord = lab_screen.overlay.sliders.slider_coords.get(dragging_slider);
        float [] bar_coord = lab_screen.overlay.sliders.bar_coords.get(dragging_slider);

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

