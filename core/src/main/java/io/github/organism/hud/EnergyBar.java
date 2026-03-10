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
        float renderX = hud.x + hud.moveSpaceControl.radius * 1.2f;

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

        // Draw income bar (green) first, then energy bar (red) on top
        for (int i=0; i<2; i++){
            game.shapeRenderer.setColor(game.energyBarColors[i]);
            game.shapeRenderer.rect(
                renderX + gapWidth * 2,
                y + gapWidth * 2,
                fillWidth[i],
                barHeight - gapWidth * 4);
        }
        game.shapeRenderer.end();
    }
}
