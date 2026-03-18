package io.github.organism.hud;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.OrganismGame;

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
        // Position is set dynamically in render based on hud.x
        x = 0;
    }


    public void render(){
        // Calculate x position based on hud.x (which is set for player positioning)
        float renderX = hud.x/2 + hud.moveSpaceControl.radius;

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(
            renderX,
            y,
            barWidth,
            barHeight);

        game.shapeRenderer.setColor(game.backgroundColor);
        game.shapeRenderer.rect(
            renderX + gapWidth,
            y + gapWidth,
            barWidth - (gapWidth * 2),
            barHeight - (gapWidth * 2));

        // Calculate bar widths
        float maxBarWidth = barWidth - (gapWidth * 4);
        
        // Energy bar (current energy level) - yellow
        fillWidth[1] = hud.energyBarValue * maxBarWidth;
        
        // Income bar (energy + income projection) - green, extends beyond energy
        fillWidth[0] = (hud.energyBarValue + hud.incomeBarValue) * maxBarWidth;
        
        // Spend bar (energy that was just spent) - red
        fillWidth[2] = hud.spendBarValue * maxBarWidth;

        // Draw income bar (green) first - shows energy + income
        if (fillWidth[0] > fillWidth[1]) {
            game.shapeRenderer.setColor(game.energyBarColors[0]); // Green
            game.shapeRenderer.rect(
                renderX + gapWidth * 2,
                y + gapWidth * 2,
                fillWidth[0],
                barHeight - gapWidth * 4);
        }
        
        // Draw energy bar (yellow) on top
        game.shapeRenderer.setColor(game.energyBarColors[1]); // Yellow
        game.shapeRenderer.rect(
            renderX + gapWidth * 2,
            y + gapWidth * 2,
            fillWidth[1],
            barHeight - gapWidth * 4);
        
        // Draw spend bar (red) - shows what was just spent
        // Position it at the current energy level, extending right
        if (hud.spendBarValue > 0.001f) {
            game.shapeRenderer.setColor(1f, 0f, 0f, 0.8f); // Red
            game.shapeRenderer.rect(
                renderX + gapWidth * 2 + fillWidth[1],
                y + gapWidth * 2,
                fillWidth[2],
                barHeight - gapWidth * 4);
        }
        game.shapeRenderer.end();
        
        // Draw max energy text
        game.batch.begin();
        game.fonts.get(12).setColor(game.foregroundColor);
        String maxEnergyText = String.format("%.0f", io.github.organism.SettingsManager.MAX_ENERGY);
        game.fonts.get(12).draw(
            game.batch,
            maxEnergyText,
            renderX + barWidth - 30,
            y - 5
        );
        game.batch.end();
    }
}
