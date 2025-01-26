package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

public class ModelPoolDisplay {
    OrganismGame game;
    Simulation simulation;

    float x_pos;
    float y_pos;
    float y_spacing;

    float box_height;
    float box_width;
    float margin;
    float bar_height;
    float bar_max_width;


    public ModelPoolDisplay(OrganismGame g, Simulation s){
        game = g;
        simulation = s;

        x_pos = game.VIRTUAL_WIDTH * 0.8f;
        y_pos = game.VIRTUAL_HEIGHT * 0.1f;
        box_height = game.VIRTUAL_HEIGHT / 2f;
        box_width = game.VIRTUAL_WIDTH / 6f;
        margin = game.VIRTUAL_WIDTH * .005f;
        bar_max_width = box_width - (margin * 2);
    }

    public void set_xy(float x, float y){
        x_pos = x;
        y_pos = y;
    }

    public void render() {

        float max_wins = 0;
        int max_count = 0;
        HashMap<Integer, Integer> wins_hist = new HashMap<>();

        for (Point p : simulation.win_records.keySet()) {
            int wins = simulation.win_records.get(p);

            if (wins > max_wins){
                max_wins = wins;
            }

            if (!wins_hist.containsKey(wins)){
                wins_hist.put(wins, 0);
            }

            int count = wins_hist.get(wins);
            if (count + 1 > max_count) {
                max_count = count + 1;
            }

            wins_hist.put(wins, count + 1);
        }


        bar_height = ((box_height - (margin * 2)) / Math.max(10, max_wins)) - margin;

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game.shape_renderer.setColor(Color.DARK_GRAY);
        game.shape_renderer.rect(x_pos, y_pos, box_width, box_height);
        game.shape_renderer.end();

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        float bar_width;
        for (int w=0; w<=max_wins; w++) {

            bar_width = 0;
            if (wins_hist.containsKey(w)) {
                bar_width = bar_max_width * ((float) wins_hist.get(w) / max_count);
            }

            game.shape_renderer.rect(
                x_pos + margin,
                y_pos + margin + ((bar_height + margin) * w),
                bar_width,
                bar_height
            );

        }
        game.shape_renderer.end();
    }

    public void dispose() {
        game = null;
        simulation = null;
    }
}
