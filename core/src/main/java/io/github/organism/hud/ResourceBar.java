package io.github.organism.hud;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

import io.github.organism.FloatPair;
import io.github.organism.OrganismGame;

public class ResourceBar {

    final float BASE_DOT_RADIUS = 4;
    float dotScale = 1;

    OrganismGame game;
    PlayerHud hud;

    float x;
    float y;

    float barHeight;
    float barWidth;

    int defaultMaxCols = 25;

    float [] dotHeights;

    float trioBoxWidth;

    ArrayList<ArrayList<FloatPair<Float>>> coords;
    private int trioCols;

    public ResourceBar(OrganismGame g, PlayerHud ph, float width, float height, float y){
        game = g;
        hud = ph;
        barWidth = width;
        barHeight = height;
        trioCols = 0;

        // there will be three rows of dots, on for each resource
        this.y = y;
        dotHeights = new float[3];
        float spacing = height/4;
        for (int i=0; i<3; i++){
            dotHeights[i] = y + spacing * (i + 1);
        }

        // x depends on the player's side
        x = hud.x + hud.moveSpaceDisplay.radius * 1.7f + spacing * 2;
        if (hud.player2){
            x = OrganismGame.VIRTUAL_WIDTH - x;
        }

        calculateResourceDisplayCoords();
    }

    public void calculateResourceDisplayCoords(){

        int allCols = 0;
        trioCols = Integer.MAX_VALUE;
        coords = new ArrayList<>();

        for (int i=0; i<3; i++){
            int res = hud.resourceCounts[i] + hud.allyResourceCounts[i];
            if (res < trioCols){
                trioCols = res;
            }
            if (res > allCols) {
                allCols = res;
            }
        }

        // calculate column spacing
        int cols = Math.max(allCols, defaultMaxCols);
        float spacing = barWidth / (cols + 1);
        if (hud.player2){
            spacing = -spacing;
        }

        for (int i=0; i<3; i++) {
            ArrayList<FloatPair<Float>> row = new ArrayList<>();
            int dots = hud.resourceCounts[i] + hud.allyResourceCounts[i];
            for (int j=0; j<dots; j++){
                float dotX = x + spacing * (j+1);
                row.add(new FloatPair<>(
                    dotX,
                    dotHeights[i]
                ));
            }
            coords.add(row);
        }
    }


    public void render(){


        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0; i < 3; i++) {

            ArrayList<FloatPair<Float>> row = coords.get(i);

            game.shapeRenderer.setColor(game.resourceColorsDark[i]);
            for (int j=0; j<hud.resourceCounts[i]; j++){
                FloatPair<Float> c = row.get(j);
                game.shapeRenderer.circle(
                    c.a, c.b, BASE_DOT_RADIUS * dotScale
                );
            }

        }

        game.shapeRenderer.end();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int i=0; i < 3; i++) {
            game.shapeRenderer.setColor(game.resourceColorsBright[i]);
            ArrayList<FloatPair<Float>> row = coords.get(i);
            for (int j=0; j<hud.allyResourceCounts[i] + hud.resourceCounts[i]; j++){
                FloatPair<Float> c = row.get(j);
                game.shapeRenderer.circle(
                    c.a, c.b, BASE_DOT_RADIUS * dotScale
                );
            }
        }

        game.shapeRenderer.end();
    }
}
