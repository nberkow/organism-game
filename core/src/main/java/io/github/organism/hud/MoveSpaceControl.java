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

    private static final float DRIFT_SPEED = 0.5f;


    public MoveSpaceControl(PlayerHud ph, float r) {
        hud = ph;
        radius = r;
        setup();
    }

    public void setup(){

        planchetteRadius = radius / 10;
        cursorRadius = radius / 10;

        availableRadius = radius - planchetteRadius;

        // Center coord is now set dynamically in drawAt()
        centerCoord = new Vector2(radius, radius);

        // Initialize cursor and planchette at center (0, 0)
        cursorCoord = new Vector2(0f, 0f);
        planchetteFromCenterVector = new Vector2(0f, 0f);
        planchetteMovementVector = new Vector2(0f, 0f);
        
        // Initialize decision start position
        decisionStartPlanchette = new Vector2(0f, 0f);
    }

    public Vector2 decisionStartPlanchette = new Vector2();

    // In setCursor() (called at decision time):
    public void setCursor(Vector2 targetXY) {
        decisionStartPlanchette.set(planchetteFromCenterVector);

        if (targetXY.len() > availableRadius) {
            cursorCoord.set(targetXY).setLength(availableRadius);
        } else {
            cursorCoord.set(targetXY);
        }
    }


    /**
     * Calculate precise planchette position for game logic.
     * Models constant-speed travel toward cursor target.
     * @param elapsedSinceDecision Seconds since DECISION phase started
     * @return Position in normalized space (unit circle) for dot-product scoring
     */
    public Vector2 getLogicalPlanchettePosition(float elapsedSinceDecision) {
        // Vector from start position to cursor target
        float deltaX = cursorCoord.x - decisionStartPlanchette.x;
        float deltaY = cursorCoord.y - decisionStartPlanchette.y;

        // Distance to travel
        float distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // If already at target (or very close), return cursor
        if (distance < 0.001f) {
            return cursorCoord.cpy();
        }

        // Unit direction vector toward cursor
        float dirX = deltaX / distance;
        float dirY = deltaY / distance;

        // How far have we traveled at constant speed?
        float traveled = Math.min(distance, DRIFT_SPEED * elapsedSinceDecision);

        // Position = start + direction * traveled
        float x = decisionStartPlanchette.x + dirX * traveled;
        float y = decisionStartPlanchette.y + dirY * traveled;

        return new Vector2(x, y);
    }


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
        float scaledActiveRadius = scaledRadius - scaledCursorRadius;

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
        hud.game.shapeRenderer.setColor(hud.game.foregroundColor);

        // Normalize cursor position for rendering
        float cursorLen = cursorCoord.len();
        float normalizedCursorX = cursorLen > 0 ? cursorCoord.x / availableRadius : 0;
        float normalizedCursorY = cursorLen > 0 ? cursorCoord.y / availableRadius : 0;

        hud.game.shapeRenderer.circle(
            drawX + normalizedCursorX * scaledActiveRadius,
            drawY + normalizedCursorY * scaledActiveRadius,
            scaledCursorRadius
        );
        hud.game.shapeRenderer.end();

        // Planchette (filled)
        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        
        // Normalize planchette position for rendering
        float planchetteLen = planchetteFromCenterVector.len();
        float normalizedPlanchetteX = planchetteLen > 0 ? planchetteFromCenterVector.x / availableRadius : 0;
        float normalizedPlanchetteY = planchetteLen > 0 ? planchetteFromCenterVector.y / availableRadius : 0;
        
        hud.game.shapeRenderer.circle(
            drawX + normalizedPlanchetteX * scaledActiveRadius,
            drawY + normalizedPlanchetteY * scaledActiveRadius,
            scaledPlanchetteRadius
        );
        hud.game.shapeRenderer.end();
    }

    public void render(float delta) {
        update(delta);
        drawAt(centerCoord.x, centerCoord.y, 1.0f);  // Full scale at main HUD position
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
        // Scale input for responsiveness (pixels per frame)
        float inputSpeed = 2.5f;
        
        // Only move if there's actual input
        if (inputDelta.len() > 0.01f) {
            cursorCoord.add(inputDelta.cpy().scl(inputSpeed));
            
            // Clamp to available radius
            if (cursorCoord.len() > availableRadius) {
                cursorCoord.setLength(availableRadius);
            }
        }
    }

}
