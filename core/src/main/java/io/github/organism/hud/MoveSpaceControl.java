package io.github.organism.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.organism.OrganismGame;

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
    float aggressionDisplayRadius;

    public MoveSpaceControl(PlayerHud ph, float r) {
        hud = ph;
        radius = r;
        setup();
    }

    public void setup(){


        planchetteRadius = radius / 10;
        cursorRadius = radius / 10;
        aggressionRadius = radius * .65f;
        aggressionDisplayRadius = aggressionRadius + planchetteRadius;
        availableRadius = radius - planchetteRadius;

        centerCoord = new Vector2(radius, radius);
        if (hud.isPlayerTwo) {
            centerCoord.x = OrganismGame.VIRTUAL_WIDTH - radius;
        }

        cursorCoord = new Vector2(0f, 0f);
        planchetteFromCenterVector = new Vector2(0f, 0f);
        planchetteMovementVector = new Vector2(0f, 0f);

    }

    public void render() {
        logic();
        draw();
    }

    public void logic() {
        updateCursor();
        updatePlanchette();
    }

    private void updatePlanchette() {

        // move planchette along current vector
        planchetteFromCenterVector.add(planchetteMovementVector);

        // adjust the vector to point at the cursor
        float deltaX = cursorCoord.x - planchetteFromCenterVector.x;
        float deltaY = cursorCoord.y - planchetteFromCenterVector.y;
        Vector2 cursorDistanceVector = new Vector2(deltaX, deltaY);
        cursorDistanceVector.clamp(0f, radius * .001f);

        float newX = (cursorDistanceVector.x * 5 + planchetteMovementVector.x)/2;
        float newY = (cursorDistanceVector.y * 5 + planchetteMovementVector.y)/2;

        planchetteMovementVector = new Vector2(newX, newY);

    }


    private void updateCursor() {
        cursorCoord.add(hud.getInputVector()).clamp(0f, radius-cursorRadius);

    }

    public void draw() {

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        hud.game.shapeRenderer.setColor(hud.game.backgroundColor);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, radius * 1.05f);
        hud.game.shapeRenderer.end();

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, radius);

        hud.game.shapeRenderer.setColor(Color.RED);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, aggressionDisplayRadius);

        if (cursorCoord.len() < aggressionRadius) {
            hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        }
        else {
            hud.game.shapeRenderer.setColor(Color.RED);
        }
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
}
