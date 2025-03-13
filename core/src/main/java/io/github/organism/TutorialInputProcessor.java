package io.github.organism;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.awt.Point;
import java.util.HashMap;

public class TutorialInputProcessor implements InputProcessor {
    GameBoard gameBoard;
    final double BUTTON_HOLD_ACTIVATION_TIME = .2d;
    final double MIN_BETWEEN_PRESS_TIME = .1d;
    TutorialScreen screen;
    double held_button_base_freq;

    HashMap<Point, PlayerGameInputData> player_input_data;

    public TutorialInputProcessor(TutorialScreen scr){
        screen = scr;
        held_button_base_freq = .2;
        player_input_data = new HashMap<>();
    }

    public static class PlayerGameInputData {
        Boolean holding_button;
        Boolean touched_button;
        double time_held;
        double time_not_held;
        Integer button_val;
        boolean button_input_ready;
        double rate_modifier;

        public PlayerGameInputData() {
            holding_button = false;
            touched_button = false;
            time_held = 0d;
            time_not_held = 0d;
            button_input_ready = false;
            rate_modifier = 1d;
        }
    }

    public void clearPlayers() {
        player_input_data = new HashMap<>();
    }

    public void add_player(Point player_id) {
        player_input_data.put(player_id, new PlayerGameInputData());
    }

    public boolean keyDown (int keycode) {

        int p = 0;
        if (keycode == Input.Keys.K || keycode == Input.Keys.L || keycode == Input.Keys.SEMICOLON) {
            p = 1;
        }
        Point player_id = gameBoard.humanPlayerIds.get(p);
        PlayerGameInputData input_data = player_input_data.get(player_id);

        int key_val = -1;
        if (keycode == Input.Keys.A || keycode == Input.Keys.SEMICOLON) {
            key_val = 0;
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.L) {
            key_val = 1;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.K) {
            key_val = 2;
        }

        input_data.button_val = key_val;
        input_data.button_input_ready = false;
        input_data.time_held = 0d;
        input_data.touched_button = true;
        input_data.time_not_held = input_data.time_not_held % MIN_BETWEEN_PRESS_TIME;

        return false;
    }

    public boolean keyUp (int keycode) {

        int p = 0;
        if (keycode == Input.Keys.K || keycode == Input.Keys.L || keycode == Input.Keys.SEMICOLON) {
            p = 1;
        }

        Point player_id = gameBoard.humanPlayerIds.get(p);
        PlayerGameInputData input_data = player_input_data.get(player_id);

        int key_val = -1;
        if (keycode == Input.Keys.A || keycode == Input.Keys.SEMICOLON) {
            key_val = 0;
        }
        if (keycode == Input.Keys.S || keycode == Input.Keys.L) {
            key_val = 1;
        }
        if (keycode == Input.Keys.D || keycode == Input.Keys.K) {
            key_val = 2;
        }

        input_data.button_val = key_val;
        input_data.button_input_ready = true;
        input_data.time_held = 0d;
        input_data.touched_button = false;
        input_data.holding_button = false;
        input_data.time_not_held = 0;

        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int screenX, int screenY, int pointer, int button) {

        // Convert screen coordinates to game world coordinates

        Vector2 touchPos = new Vector2(screenX, screenY);
        screen.game.viewport.unproject(touchPos);
        GameplayButtons.StepButton button_data = screen.player1Hud.gameButtons.check_buttons(touchPos.x, touchPos.y);

        if (button_data != null){

            Point player_id = gameBoard.humanPlayerIds.get(button_data.player - 1);
            PlayerGameInputData input_data = player_input_data.get(player_id);

            if (input_data.time_not_held > MIN_BETWEEN_PRESS_TIME) {
                if (!input_data.holding_button) {
                    input_data.button_val = button_data.value;
                    input_data.time_held = 0d;
                    input_data.holding_button = false;
                    input_data.touched_button = true;
                    input_data.time_not_held = 0d;
                    input_data.button_input_ready = false;
                }
            }
        }
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        // Convert screen coordinates to game world coordinates
        Vector2 touchPos = new Vector2(screenX, screenY);
        gameBoard.game.viewport.unproject(touchPos);
        GameplayButtons.StepButton button_data = screen.player1Hud.gameButtons.check_buttons(touchPos.x, touchPos.y);

        if (button_data != null) {
            Point player_id = gameBoard.humanPlayerIds.get(button_data.player - 1);
            PlayerGameInputData input_data = player_input_data.get(player_id);
            if (input_data.touched_button){
                input_data.time_held = 0d;
                input_data.holding_button = false;
                input_data.touched_button = false;
                input_data.time_not_held = 0d;
                input_data.button_input_ready = true;
            }
        }

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

    public void updateTimers(double time_delta){
        for (Point p : gameBoard.humanPlayerIds){
            PlayerGameInputData input_data = player_input_data.get(p);

            input_data.time_held += time_delta;
            if (input_data.touched_button && !input_data.holding_button) {
                if (input_data.time_held > BUTTON_HOLD_ACTIVATION_TIME) {
                    input_data.holding_button = true;
                }
            }
            input_data.time_not_held += time_delta;
        }
    }

    public void updateQueuesWithInput(){
        for (Point p : gameBoard.humanPlayerIds){

            Player player = gameBoard.players.get(p);
            PlayerGameInputData input_data = player_input_data.get(p);

            // if the button is being held
            if (input_data.holding_button){
                input_data.button_input_ready = false;
                if (input_data.time_held > held_button_base_freq){
                    input_data.time_held = input_data.time_held % held_button_base_freq;
                    input_data.button_input_ready = true;
                }
            }

            // button_input_ready is already true for a quick press
            if (input_data.button_val != null && input_data.button_input_ready){
                player.queue_move(input_data.button_val);
                input_data.button_input_ready = false;
            }
        }
    }
}
