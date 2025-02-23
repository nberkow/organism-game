package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

public class LabScreenInputProcessor implements InputProcessor {
    LabScreen lab_screen;
    String dragging_slider;
    String button_clicked;
    String box_clicked;

    public LabScreenInputProcessor(LabScreen screen){

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
        button_clicked = lab_screen.buttons.poll_buttons(touchPos.x, touchPos.y);
        box_clicked = lab_screen.checkboxes.poll_boxes(touchPos.x, touchPos.y);

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
            String button_up = lab_screen.buttons.poll_buttons(touchPos.x, touchPos.y);

            if (Objects.equals(button_up, button_clicked)) {
                lab_screen.handle_button_click(button_clicked);
            }
        }

        if (box_clicked != null) {
            String box_up = lab_screen.checkboxes.poll_boxes(touchPos.x, touchPos.y);

            if (Objects.equals(box_up, box_clicked)) {
                lab_screen.handle_checkbox_click(box_clicked);
            }
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

