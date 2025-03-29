package io.github.organism.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.DoublePair;
import io.github.organism.FloatPair;
import io.github.organism.OrganismGame;
import io.github.organism.Util;

public class MoveSpaceControl {

    public float radius;
    public float aggressionRadius;
    public float cursorRadius;
    public float planchetteRadius;
    public FloatPair<Float> centerCoord;
    public FloatPair<Float> cursorCoord;
    public FloatPair<Float> planchetteCoord;

    public FloatPair<Float> cursorPolar;
    public FloatPair<Float> planchettePolar;
    public DoublePair<Double> planchetteMovementVector;
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

        cursorCoord = new FloatPair<>(0f, 0f);
        cursorPolar = new FloatPair<>(0f, 0f);

        planchetteCoord = new FloatPair<>(0f, 0f);
        planchettePolar = new FloatPair<>(0f, 0f);


        planchetteMovementVector = new DoublePair<>(0d, 0d);

        centerCoord = new FloatPair<>(radius, radius);
        if (hud.player2) {
            centerCoord.a = OrganismGame.VIRTUAL_WIDTH - radius;
        }

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
        FloatPair<Float> planchetteMoveXY = Util.polarToVisualXY(planchetteMovementVector.r, planchetteMovementVector.t);
        planchetteCoord.a += planchetteMoveXY.a;
        planchetteCoord.b += planchetteMoveXY.b;
        planchettePolar = Util.xyToPolarFloat(planchetteCoord.a, planchetteCoord.b);

        // adjust the vector to point at the cursor

        // current distances from cursor
        float deltaX = cursorCoord.a - planchetteCoord.a;
        float deltaY = cursorCoord.b - planchetteCoord.b;
        FloatPair<Float> cursorDistanceVector = Util.xyToPolarFloat(deltaX, deltaY);
        FloatPair<Float> adjustedXY = Util.polarToVisualXY(0.1d, (double) cursorDistanceVector.b);

        float newX = (adjustedXY.a * 5 + planchetteMoveXY.a)/2;
        float newY = (adjustedXY.b * 5 + planchetteMoveXY.b)/2;

        planchetteMovementVector = Util.xyToPolarDouble((double) newX, (double) newY);

    }


    private void updateCursor() {
        DoublePair<Double> polar = hud.getTheta();
        FloatPair<Float> coord = Util.polarToVisualXY(polar.r, polar.t);
        cursorPolar = Util.xyToPolarFloat(cursorCoord.a + coord.a, cursorCoord.b + coord.b);

        float v = Math.min(cursorPolar.a, availableRadius);
        if (cursorPolar.a == 0) {
            cursorCoord = new FloatPair<>(0f, 0f);
        } else {
            cursorCoord = Util.polarToVisualXY((double) v, (double) cursorPolar.b);
        }

    }

    public void draw() {

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        hud.game.shapeRenderer.setColor(hud.game.backgroundColor);
        hud.game.shapeRenderer.circle(centerCoord.a, centerCoord.b, radius * 1.05f);
        hud.game.shapeRenderer.end();

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        hud.game.shapeRenderer.circle(centerCoord.a, centerCoord.b, radius);

        hud.game.shapeRenderer.setColor(Color.RED);
        hud.game.shapeRenderer.circle(centerCoord.a, centerCoord.b, aggressionDisplayRadius);

        if (cursorPolar.a < aggressionRadius) {
            hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        }
        else {
            hud.game.shapeRenderer.setColor(Color.RED);
        }
        hud.game.shapeRenderer.circle(cursorCoord.a + centerCoord.a, cursorCoord.b + centerCoord.b, cursorRadius);

        hud.game.shapeRenderer.end();

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (planchettePolar.a < aggressionRadius) {
            hud.game.shapeRenderer.setColor(hud.game.foregroundColor);
        }
        else {
            hud.game.shapeRenderer.setColor(Color.RED);
        }

        hud.game.shapeRenderer.circle(planchetteCoord.a + centerCoord.a, planchetteCoord.b + centerCoord.b, planchetteRadius);
        hud.game.shapeRenderer.end();
    }
}
