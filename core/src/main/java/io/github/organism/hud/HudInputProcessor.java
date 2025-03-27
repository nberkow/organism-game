package io.github.organism.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;

import java.util.HashMap;

import io.github.organism.DoublePair;
import io.github.organism.HudTestScreen;
import io.github.organism.Pair;

@SuppressWarnings("ALL")
public class HudInputProcessor implements InputProcessor {

    public HashMap<Integer, Boolean> keysDown;
    public HashMap<Integer, Double> playerOneKeyAngles;
    public HashMap<Integer, Double> playerTwoKeyAngles;

    public Screen screen;


    public HudInputProcessor(Screen scr){
        screen = scr;
        keysDown = new HashMap<>();
        playerOneKeyAngles = new HashMap<>();
        playerTwoKeyAngles = new HashMap<>();
        setupKeys();
    }

    public void setupKeys(){

        int [] p1Codes = {
            Input.Keys.W, Input.Keys.D, Input.Keys.S, Input.Keys.A,
        };

        int [] p2Codes = {
            Input.Keys.O, Input.Keys.SEMICOLON, Input.Keys.L, Input.Keys.K
        };

        float [] angles = {
            0, .25f, .5f, .75f
        };

        for (int k=0; k<4; k++) {
            keysDown.put(p1Codes[k], false);
            keysDown.put(p2Codes[k], false);
            playerOneKeyAngles.put(p1Codes[k], angles[k] * Math.PI * 2f);
            playerTwoKeyAngles.put(p2Codes[k], angles[k] * Math.PI * 2f);
        }
    }


    public DoublePair<Double> getThetaFromKeys(boolean playerTwo){

        HashMap<Integer, Double> keyAngles;

        if (playerTwo) {
            keyAngles = playerTwoKeyAngles;
        } else {
            keyAngles = playerOneKeyAngles;
        }

        double sumX = 0f;
        double sumY = 0f;
        double a;

        for (int k : keyAngles.keySet()) {
            if (keysDown.get(k)) {
                a = keyAngles.get(k);
                sumX += Math.cos(a);
                sumY += Math.sin(a);
            }
        }

        double theta = Math.atan2(sumX, sumY);
        double m = Math.sqrt(Math.pow(sumX, 2) + Math.pow(sumY, 2));
        return new DoublePair<Double>(m, theta);
    }


    /**
     * @param keycode one of the constants in {@link Input.Keys}
     * @return
     */
    @Override
    public boolean keyDown(int keycode) {
        keysDown.put(keycode, true);
        return false;
    }

    /**
     * @param keycode one of the constants in {@link Input.Keys}
     * @return
     */
    @Override
    public boolean keyUp(int keycode) {
        keysDown.put(keycode, false);
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
