package io.github.organism.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.OrganismGame;
import io.github.organism.SettingsManager;

public class EnergyBar {

    OrganismGame game;
    PlayerHud hud;

    float x;
    float y;

    float barHeight;
    float barWidth;
    float individualBarHeight;
    float gapWidth;
    float [] fillWidth;
    public EnergyBar(OrganismGame g, PlayerHud ph, float width, float height, float y){
        game = g;
        hud = ph;
        barWidth = width;
        barHeight = height;
        gapWidth = OrganismGame.VIRTUAL_HEIGHT * 0.005f;
        fillWidth = new float[3];

        individualBarHeight = (barHeight - (gapWidth * 6))/3;

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

        fillWidth[0] = hud.incomeBarValue * (barWidth - (gapWidth * 4));
        fillWidth[1] = hud.energyBarValue * (barWidth - (gapWidth * 4));
         fillWidth[2] = hud.spendBarValue * (barWidth - (gapWidth * 4));

        for (int i=0; i<3; i++){
            if (hud.player2) {
                shift = -barWidth + (barWidth - fillWidth[i]);
            }

            game.shapeRenderer.setColor(game.energyBarColors[i]);
            game.shapeRenderer.rect(
                x + gapWidth * 2 + shift,
                y + gapWidth * (2 + i) + (individualBarHeight * i),
                fillWidth[i],
                individualBarHeight);
        }
        game.shapeRenderer.end();
    }
}
