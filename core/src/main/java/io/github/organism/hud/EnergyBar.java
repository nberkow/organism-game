package io.github.organism.hud;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sun.org.apache.xpath.internal.operations.Or;

import io.github.organism.OrganismGame;
import io.github.organism.SettingsManager;

public class EnergyBar {

    OrganismGame game;
    PlayerHud hud;

    float x;
    float y;

    float barHeight;
    float barWidth;
    float gapWidth;
    public EnergyBar(OrganismGame g, PlayerHud ph, float width, float height, float y){
        game = g;
        hud = ph;
        barWidth = width;
        barHeight = height;
        gapWidth = barHeight * .05f;

        this.y = y;
        x = hud.x + hud.moveSpaceDisplay.radius * 1.7f;

        if (hud.player2){
            x = OrganismGame.VIRTUAL_WIDTH - x;
        }
    }


    public void render(){

        float shift = 0;
        if (hud.player2) {
            shift = -barWidth;
        }

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(
            x + shift,
            y,
            barWidth,
            barHeight);

        game.shapeRenderer.setColor(game.backgroundColor);
        game.shapeRenderer.rect(
            x + gapWidth + shift,
            y + gapWidth,
            barWidth - (gapWidth * 2),
            barHeight - (gapWidth * 2));

        float percentFilled = hud.energyBarValue / SettingsManager.MAX_ENERGY;
        float fillWidth = (barWidth - (gapWidth * 4)) * percentFilled;

        if (hud.player2) {
            shift = -barWidth + (barWidth - fillWidth);
        }

        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(
            x + gapWidth * 2 + shift,
            y + gapWidth * 2,
            fillWidth,
            barHeight - (gapWidth * 4));

        game.shapeRenderer.end();
    }
}
