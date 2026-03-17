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

        // Energy bar (current energy level)
        fillWidth[1] = hud.energyBarValue * (barWidth - (gapWidth * 4));
        // Income bar (energy + income projection)
        fillWidth[0] = fillWidth[1] + hud.incomeBarValue * (barWidth - (gapWidth * 4));
        // Spend bar (energy that was just spent)
        fillWidth[2] = hud.spendBarValue * (barWidth - (gapWidth * 4));

        // Draw income bar (green) first, then energy bar (yellow) on top
        for (int i=0; i<2; i++){
            game.shapeRenderer.setColor(game.energyBarColors[i]);
            game.shapeRenderer.rect(
                renderX + gapWidth * 2,
                y + gapWidth * 2,
                fillWidth[i],
                barHeight - gapWidth * 4);
        }
        
        // Draw spend bar (red) overlaid on the energy bar to show what was spent
        if (hud.spendBarValue > 0.001f) {
            game.shapeRenderer.setColor(1f, 0f, 0f, 0.7f); // Semi-transparent red
            // Draw from the right edge of current energy, extending right by spend amount
            float spendBarX = renderX + gapWidth * 2 + fillWidth[1];
            game.shapeRenderer.rect(
                spendBarX,
                y + gapWidth * 2,
                fillWidth[2],
                barHeight - gapWidth * 4);
        }
        game.shapeRenderer.end();
    }
}
