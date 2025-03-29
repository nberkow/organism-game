package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class TimeIndicator {

    OrganismGame game;
    GameSession gameSession;

    ArrayList<FloatPair<Float>> points;
    double sideLength;
    float period;
    float turn;
    float holdColorTime;
    float x;
    float y;
    public TimeIndicator(OrganismGame g, GameSession session, float x, float y, double s){
        game = g;
        gameSession = session;
        this.x = x;
        this.y = y;
        sideLength = s;

        turn = 0;
        period = 1;
        calculatePoints();
    }


    public void calculatePoints(){
        points = new ArrayList<>();
        double theta;
        for (int i = 0; i < 6; i++) {
            theta = i * Math.PI / 3;
            FloatPair<Float> coord = Util.polarToVisualXY(sideLength, theta);
            points.add(coord);
        }
    }

    public void advanceTurn(){
        turn = (turn + 1) % 6;
    }

    public void render(){

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i=0; i<6; i++){
            int j = (i+1) % 6;
            game.shapeRenderer.rectLine(
                x + points.get(i).a,
                y + points.get(i).b,
                x + points.get(j).a,
                y + points.get(j).b,
                1);
        }

        float [] cVal = new float[3];


        for (int p=1; p<turn+1; p++){
            cVal[0] = p/6f;
            cVal[1] = (cVal[0] + (1/3f)) % 1;
            cVal[2] = (cVal[1] + (1/3f)) % 1;
            Color c = new Color(cVal[0] * 0.25f , cVal[1] * 0.25f, cVal[2] * 0.25f, 1);
            game.shapeRenderer.setColor(c);
            for (int i=0; i<6; i++){
                int j = (i+1) % 6;
                game.shapeRenderer.rectLine(
                    x + points.get(i).a * p/6,
                    y + points.get(i).b * p/6,
                    x + points.get(j).a * p/6,
                    y + points.get(j).b * p/6,
                    1f);
            }
        }
        game.shapeRenderer.end();
    }
}
