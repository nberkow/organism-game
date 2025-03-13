package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.util.HashMap;

public class ModelPoolDisplay {
    OrganismGame game;
    Simulation simulation;

    float x_pos;
    float y_pos;
    float y_spacing;

    float box_height;
    float box_width;
    float padding;
    float bar_height;
    float bar_max_width;


    public ModelPoolDisplay(OrganismGame g, Simulation s){
        game = g;
        simulation = s;

        x_pos = game.VIRTUAL_WIDTH * 0.8f;
        y_pos = game.VIRTUAL_HEIGHT * 0.1f;
        box_height = game.VIRTUAL_HEIGHT / 2f;
        box_width = game.VIRTUAL_WIDTH / 6f;
        padding = game.VIRTUAL_WIDTH * .005f;
        bar_max_width = box_width - (padding * 2);
    }

    public void set_xy(float x, float y){
        x_pos = x;
        y_pos = y;
    }

    public void render() {

        if (game.shapeRenderer == null) {
            return;
        }

        int max_win_margin = 0;
        int min_win_margin = Integer.MAX_VALUE;

        int max_count = 0;
        HashMap<Integer, Integer> wins_hist = new HashMap<>();

        for (Point p : simulation.winRecords.keySet()) {
            Point rec = simulation.winRecords.get(p).get(0);
            int win_margin = (rec.x - rec.y);

            if (win_margin > max_win_margin){
                max_win_margin = win_margin;
            }

            if (win_margin < min_win_margin){
                min_win_margin = win_margin;
            }

            if (!wins_hist.containsKey(win_margin)){
                wins_hist.put(win_margin, 0);
            }

            int count = wins_hist.get(win_margin);
            if (count + 1 > max_count) {
                max_count = count + 1;
            }

            wins_hist.put(win_margin, count + 1);
        }


        bar_height = ((box_height - (padding * 2)) / (1 + max_win_margin - min_win_margin)) - padding;

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(x_pos, y_pos, box_width, box_height);
        game.shapeRenderer.end();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float bar_width;
        for (int w=min_win_margin; w<=max_win_margin; w++) {

            bar_width = 0;
            if (wins_hist.containsKey(w)) {
                bar_width = bar_max_width * ((float) wins_hist.get(w) / max_count);
            }

            game.shapeRenderer.rect(
                x_pos + padding,
                y_pos + padding + ((bar_height + padding) * (w - min_win_margin)),
                bar_width,
                bar_height
            );

        }
        game.shapeRenderer.end();
    }

    public void dispose() {
        game = null;
        simulation = null;
    }
}
