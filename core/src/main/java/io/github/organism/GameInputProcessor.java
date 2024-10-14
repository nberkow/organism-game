package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

public class GameInputProcessor implements InputProcessor {

    GameBoard game_board;
    Boolean holding_button;
    Boolean touched_button;
    double time_held;

    double actions_staged;

    final double HOLD_DELAY = 0.5d;

    int button_val;
    boolean action_ready;
    public GameInputProcessor(GameBoard gb){
        game_board = gb;
        holding_button = false;
        touched_button = false;
        time_held = 0;
        action_ready = false;
    }

    public boolean keyDown (int keycode) {
        return false;
    }

    public boolean keyUp (int keycode) {
        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        action_ready = false;
        Vector2 touchPos = new Vector2(screenX, screenY);

        // Convert screen coordinates to game world coordinates
        game_board.main.viewport.unproject(touchPos);

        button_val = game_board.input_panel.check_buttons(touchPos.x, touchPos.y);
        if (button_val > -1){
            touched_button = true;
        }
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        // if the player clicked and un-clicked quickly register the click
        if (button_val > -1 && !holding_button) {
            action_ready = true;
        }

        holding_button = false;
        touched_button = false;
        time_held = 0;
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged (int x, int y, int pointer) {
        return false;
    }

    public boolean mouseMoved (int x, int y) {
        return false;
    }

    public boolean scrolled (float amountX, float amountY) {
        return false;
    }
}
