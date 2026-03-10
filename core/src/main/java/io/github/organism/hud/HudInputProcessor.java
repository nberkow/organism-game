package io.github.organism.hud;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

import io.github.organism.DoublePair;
import io.github.organism.MenuOverlay;
import io.github.organism.OrganismGame;

@SuppressWarnings("ALL")
public class HudInputProcessor implements InputProcessor {

    public HashMap<Integer, Boolean> keysDown;
    public HashMap<Integer, Double> playerOneKeyAngles;
    public HashMap<Integer, Double> playerTwoKeyAngles;

    public Screen screen;

    private MenuOverlay menuOverlay;


    public HudInputProcessor(Screen scr){
        screen = scr;
        keysDown = new HashMap<>();
        playerOneKeyAngles = new HashMap<>();
        playerTwoKeyAngles = new HashMap<>();
        setupKeys();
    }

    public void setupKeys(){

        int [] p1Codes = {
            Input.Keys.W, Input.Keys.D, Input.Keys.S, Input.Keys.A
        };

        int [] p2Codes = {
            Input.Keys.O, Input.Keys.SEMICOLON, Input.Keys.L, Input.Keys.K
        };

        float [] angles = {
            .25f, 0f, .75f, .5f
        };

        keysDown.put(Input.Keys.ESCAPE, false);

        for (int k=0; k<4; k++) {
            keysDown.put(p1Codes[k], false);
            keysDown.put(p2Codes[k], false);
            playerOneKeyAngles.put(p1Codes[k], angles[k] * Math.PI * 2f);
            playerTwoKeyAngles.put(p2Codes[k], angles[k] * Math.PI * 2f);
        }
    }

    public void setMenuOverlay(MenuOverlay overlay) {
        System.out.println("setting menu overlay");
        this.menuOverlay = overlay;
    }

    public Vector2 getInputVectorFromKeys(boolean playerTwo){

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

        // Return normalized vector (length 0-1) for smooth movement
        Vector2 v = new Vector2((float) sumX, (float) sumY);
        if (v.len() > 0) {
            v.nor();  // Normalize to unit vector
        }
        return v;
    }


    /**
     * @param keycode one of the constants in {@link Input.Keys}
     * @return
     */
    @Override
    public boolean keyDown(int keycode) {
        // ESC: toggle menu
        if (keycode == Input.Keys.ESCAPE) {
            if (menuOverlay != null) {
                menuOverlay.toggle();
                screen.pause();
            }
            return true;  // Consume
        }

        // Existing movement key handling
        if (playerOneKeyAngles.containsKey(keycode) || playerTwoKeyAngles.containsKey(keycode)) {
            keysDown.put(keycode, true);
            return true;
        }
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
        if (button == Input.Buttons.LEFT && menuOverlay != null) {
            // Convert screen coords to virtual coords if needed
            float virtualX = screenX;  // Adjust if your virtual viewport differs
            float virtualY = OrganismGame.VIRTUAL_HEIGHT - screenY;  // LibGDX Y is bottom-up

            menuOverlay.handleClick(virtualX, virtualY);
            return true;  // Consume if menu handled it
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

    /**
     * Check if ESC was pressed since last call, and consume the event.
     * @return true if ESC was pressed, false otherwise
     */
    public boolean consumeEscape() {
        Boolean pressed = keysDown.get(Input.Keys.ESCAPE);
        if (pressed != null && pressed) {
            keysDown.put(Input.Keys.ESCAPE, false);  // Reset to avoid re-triggering
            return true;
        }
        return false;
    }

}
