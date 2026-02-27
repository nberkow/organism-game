package io.github.organism.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.organism.OrganismGame;
import io.github.organism.Util;

public class MoveSpaceControl {

    public float radius;
    public float aggressionRadius;
    public float cursorRadius;
    public float planchetteRadius;
    public Vector2 centerCoord;
    public Vector2 cursorCoord;
    public Vector2 planchetteFromCenterVector;
    public Vector2 planchetteMovementVector;
    PlayerHud hud;
    float availableRadius;

    float TURN_DURATION = 1.5f;


    public MoveSpaceControl(PlayerHud ph, float r) {
        hud = ph;
        radius = r;
        setup();
    }

    public void setup(){

        planchetteRadius = radius / 10;
        cursorRadius = radius / 10;

        availableRadius = radius - planchetteRadius;

        centerCoord = new Vector2(radius, radius);
        if (hud.isPlayerTwo) {
            centerCoord.x = OrganismGame.VIRTUAL_WIDTH - radius;
        }

        cursorCoord = new Vector2(0f, 0f);
        planchetteFromCenterVector = new Vector2(0f, 0f);
        planchetteMovementVector = new Vector2(0f, 0f);

    }

    private Vector2 decisionStartPlanchette = new Vector2();

    // In setCursor() (called at decision time):
    public void setCursor(Vector2 targetXY) {
        decisionStartPlanchette.set(planchetteFromCenterVector);  // Save start position
        // ... rest of setCursor logic ...
    }

    // In getLogicalPlanchettePosition():
    public Vector2 getLogicalPlanchettePosition(float elapsedSinceDecision) {
        float t = Math.min(1.0f, elapsedSinceDecision / TURN_DURATION);
        float x = decisionStartPlanchette.x + (cursorCoord.x - decisionStartPlanchette.x) * t;
        float y = decisionStartPlanchette.y + (cursorCoord.y - decisionStartPlanchette.y) * t;
        return new Vector2(x, y);
    }

    // In MoveSpaceControl.java:

    /**
     * Update visual drift. Call every frame before drawing.
     * @param delta Frame time in seconds
     */
    public void update(float delta) {
        // Drift planchette toward cursor (heuristic, not game-accurate)
        float driftSpeed = 2.0f;  // Units per second, tune for smoothness

        float deltaX = cursorCoord.x - planchetteFromCenterVector.x;
        float deltaY = cursorCoord.y - planchetteFromCenterVector.y;

        planchetteFromCenterVector.x += deltaX * driftSpeed * delta;
        planchetteFromCenterVector.y += deltaY * driftSpeed * delta;
    }

    /**
     * Get current cursor position (for IO_Player to capture).
     */
    public Vector2 getCursorCoord() {
        return cursorCoord.cpy();
    }

    /**
     * Draw at arbitrary position and scale.
     * Call update(delta) before this.
     */
    public void drawAt(float drawX, float drawY, float scale) {
        float scaledRadius = radius * scale;
        float scaledCursorRadius = cursorRadius * scale;
        float scaledPlanchetteRadius = planchetteRadius * scale;

        // Background
        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hud.game.shapeRenderer.setColor(hud.game.backgroundColor);
        hud.game.shapeRenderer.circle(drawX, drawY, scaledRadius * 1.05f);
        hud.game.shapeRenderer.end();

        // Control circle outline
        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        hud.game.shapeRenderer.circle(drawX, drawY, scaledRadius);
        hud.game.shapeRenderer.end();

        // Cursor (outline)
        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        hud.game.shapeRenderer.setColor(
            cursorCoord.len() < aggressionRadius * scale ?
                hud.game.foregroundColor : Color.RED
        );
        hud.game.shapeRenderer.circle(
            drawX + cursorCoord.x * scale,
            drawY + cursorCoord.y * scale,
            scaledCursorRadius
        );
        hud.game.shapeRenderer.end();

        // Planchette (filled)
        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hud.game.shapeRenderer.setColor(
            planchetteFromCenterVector.len() < aggressionRadius * scale ?
                hud.game.foregroundColor : Color.RED
        );
        hud.game.shapeRenderer.circle(
            drawX + planchetteFromCenterVector.x * scale,
            drawY + planchetteFromCenterVector.y * scale,
            scaledPlanchetteRadius
        );
        hud.game.shapeRenderer.end();
    }

    public void render(float delta) {
        update(delta);
        drawAt(centerCoord.x, centerCoord.y, 1);  // Full scale at main HUD position
    }
    private void updateCursor() {
        if (hud.isPlayerOne || hud.isPlayerTwo) {
            cursorCoord.add(hud.getPlayerInputVector());
        }

        // Clamp cursor to stay within the available radius
        if (cursorCoord.len() > availableRadius) {
            cursorCoord.setLength(availableRadius);
        }
    }

    public void draw() {

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        hud.game.shapeRenderer.setColor(hud.game.backgroundColor);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, radius * 1.05f);
        hud.game.shapeRenderer.end();

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, radius);

        hud.game.shapeRenderer.circle(
            cursorCoord.x + centerCoord.x,
            cursorCoord.y + centerCoord.y,
            cursorRadius);

        hud.game.shapeRenderer.end();

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (planchetteFromCenterVector.len() < aggressionRadius) {
            hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        }
        else {
            hud.game.shapeRenderer.setColor(Color.RED);
        }

        hud.game.shapeRenderer.circle(
            planchetteFromCenterVector.x + centerCoord.x,
            planchetteFromCenterVector.y + centerCoord.y,
            planchetteRadius);
        hud.game.shapeRenderer.end();
    }

    public Vector2 getPlanchetteFromCenterVector() {
        return planchetteFromCenterVector.cpy().nor();
    }

    public Vector2 getCursorAsVector() {
        return Util.xyToPolarFloat(cursorCoord);
    }

    public void setCursorVector(Vector2 cursor) {
        Vector2 xyPos = Util.polarToXYFloat(cursor.scl(availableRadius));
        cursorCoord.set(xyPos);
    }



    /**
     * Set the target cursor position.
     * Works for both human input and bot decisions.
     * @param targetXY Local XY coordinates (relative to center, range: -availableRadius to +availableRadius)
     */
    public void setTarget(Vector2 targetXY) {
        if (targetXY.len() > availableRadius) {
            cursorCoord.set(targetXY).setLength(availableRadius);
        } else {
            cursorCoord.set(targetXY);
        }
    }


    /**
     * Draw at arbitrary position and scale.
     * @param drawX Screen X for circle center
     * @param drawY Screen Y for circle center
     * @param scale 1.0 = full size, 0.6 = summary display
     */

    /**
     * For human players: accumulate continuous input.
     * Call every frame while human is providing input.
     */
    public void accumulateInput(Vector2 inputDelta) {
        cursorCoord.add(inputDelta);
        if (cursorCoord.len() > availableRadius) {
            cursorCoord.setLength(availableRadius);
        }
    }

}
