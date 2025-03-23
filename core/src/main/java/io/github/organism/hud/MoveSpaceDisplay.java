package io.github.organism.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.DoublePair;
import io.github.organism.FloatPair;
import io.github.organism.OrganismGame;
import io.github.organism.Pair;
import io.github.organism.Util;

public class MoveSpaceDisplay {

    final double INPUT_MAGNITUDE = 0.5d;
    public float cursorR;
    public float cursorTheta;
    public float planchetteR;
    public float planchetteTheta;

    public float radius;
    public float aggressionRadius;
    public float cursorRadius;
    public float planchetteRadius;
    public FloatPair<Float> centerCoord;
    public FloatPair<Float> cursorCoord;
    public FloatPair<Float> planchetteCoord;


    PlayerHud hud;

    public MoveSpaceDisplay(PlayerHud ph) {
        hud = ph;
        setup();
    }

    public void setup(){
        radius = OrganismGame.VIRTUAL_WIDTH * 0.075f;
        planchetteRadius = radius / 10;
        cursorRadius = radius / 10;
        aggressionRadius = radius * .65f;

        cursorR = 0f;
        cursorTheta = 0f;
        cursorCoord = new FloatPair<>(0f, 0f);

        planchetteR = 0f;
        planchetteTheta = 0f;
        planchetteCoord = new FloatPair<>(0f, 0f);

        centerCoord = new FloatPair<>(radius, radius);
        if (hud.player2) {
            centerCoord.x = OrganismGame.VIRTUAL_WIDTH - radius;
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
    }

    private void updateCursor() {
        DoublePair<Double> polar = hud.getTheta();
        FloatPair<Float> coord = Util.polarToVisualXY(polar.r, polar.t);
        cursorCoord.x += coord.x;
        cursorCoord.y += coord.y;
    }


    public void draw() {
        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        hud.game.shapeRenderer.setColor(hud.game.foreground_color);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, radius);

        hud.game.shapeRenderer.setColor(Color.RED);
        hud.game.shapeRenderer.circle(centerCoord.x, centerCoord.y, aggressionRadius);

        hud.game.shapeRenderer.setColor(hud.game.foreground_color);
        hud.game.shapeRenderer.circle(cursorCoord.x + centerCoord.x, cursorCoord.y + centerCoord.y, cursorRadius);

        hud.game.shapeRenderer.end();

        hud.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        hud.game.shapeRenderer.setColor(hud.game.foreground_color);
        hud.game.shapeRenderer.circle(planchetteCoord.x + centerCoord.x, planchetteCoord.y + centerCoord.y, planchetteRadius);

        hud.game.shapeRenderer.end();
    }
}
