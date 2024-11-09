package io.github.organism;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class GameInputProcessor implements InputProcessor {
    GameBoard game_board;
    final double BUTTON_HOLD_ACTIVATION_TIME = .2d;
    final double MIN_BETWEEN_PRESS_TIME = .1d;
    GameScreen screen;
    double held_button_base_freq;

    HashMap<String, PlayerGameInputData> player_input_data;

    public GameInputProcessor(GameScreen screen){
        this.screen = screen;
        game_board = screen.game_board;

        held_button_base_freq = .2;
        player_input_data = new HashMap<>();
        for (String player_name : game_board.human_player_names){
            player_input_data.put(player_name, new PlayerGameInputData());
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class PlayerGameInputData {
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

    public boolean keyDown (int keycode) {

        PlayerGameInputData player_input = new PlayerGameInputData();
        // TODO: populate player_input
        // match downstream with click behavior

        return false;
    }

    public boolean keyUp (int keycode) {
        return false;
    }

    public boolean keyTyped (char character) {
        return false;
    }

    public boolean touchDown (int screenX, int screenY, int pointer, int button) {

        // Convert screen coordinates to game world coordinates

        Vector2 touchPos = new Vector2(screenX, screenY);
        game_board.game.viewport.unproject(touchPos);
        GameplayButtons.StepButton button_data = game_board.player1_hud.game_buttons.check_buttons(touchPos.x, touchPos.y);

        if (button_data != null){

            String player_name = game_board.human_player_names.get(button_data.player - 1);
            PlayerGameInputData input_data = player_input_data.get(player_name);

            if (input_data.time_not_held > MIN_BETWEEN_PRESS_TIME) {
                if (!input_data.holding_button) {
                    input_data.button_val = button_data.value;
                    input_data.button_input_ready = false;
                    input_data.time_held = 0d;
                    input_data.touched_button = true;
                    input_data.time_not_held = input_data.time_not_held % MIN_BETWEEN_PRESS_TIME;
                }
            }
        }

        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        // Convert screen coordinates to game world coordinates
        Vector2 touchPos = new Vector2(screenX, screenY);
        game_board.game.viewport.unproject(touchPos);
        GameplayButtons.StepButton button_data = game_board.player1_hud.game_buttons.check_buttons(touchPos.x, touchPos.y);

        if (button_data != null) {
            String player_name = game_board.human_player_names.get(button_data.player - 1);
            PlayerGameInputData input_data = player_input_data.get(player_name);
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

    public void update_timers(double time_delta){
        for (String player_name : game_board.human_player_names){
            PlayerGameInputData input_data = player_input_data.get(player_name);

            input_data.time_held += time_delta;
            if (input_data.touched_button && !input_data.holding_button) {
                if (input_data.time_held > BUTTON_HOLD_ACTIVATION_TIME) {
                    input_data.holding_button = true;
                }
            }
            input_data.time_not_held += time_delta;
        }
    }

    public void update_queues_with_input(){
        for (String player_name : game_board.human_player_names){

            Player player = game_board.players.get(player_name);
            PlayerGameInputData input_data = player_input_data.get(player_name);

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
