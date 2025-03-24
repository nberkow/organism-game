package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class InputHistogram {
    OrganismGame game;
    Simulation simulation;
    int n_inputs;
    ArrayList<HashMap<Float, Integer>> observations;
    ArrayList<Integer> total_observations;
    BitmapFont font;

    float graph_x;
    float graph_y;
    float graph_w;
    float graph_h;

    ArrayList<Float> means;
    ArrayList<Float> min_vals;
    ArrayList<Float> max_vals;

    ArrayList<ArrayList<Float>> hist_bins;
    ArrayList<ArrayList<Integer>> hist_heights;

    int update_cadence = 100;
    int update_counter = 0;
    int current_visible_hist = 0;

    public InputHistogram(OrganismGame g, Simulation sim, int n_i, float x, float y, float w, float h){
        game = g;
        simulation = sim;
        n_inputs = n_i;
        font = game.fonts.get(16);

        total_observations = new ArrayList<>();

        observations = new ArrayList<>();
        hist_bins = new ArrayList<>();
        hist_heights = new ArrayList<>();

        means = new ArrayList<>();
        min_vals = new ArrayList<>();
        max_vals = new ArrayList<>();

        for (int i=0; i<n_inputs; i++){
            total_observations.add(0);
            observations.add(
                new HashMap<>()
            );
            means.add(null);
            min_vals.add(null);
            max_vals.add(null);
        }
        graph_x = x;
        graph_y = y;
        graph_w = w;
        graph_h = h;
    }

    public void update_inputs(){
        for (Player p : simulation.currentGame.players.values()){
            add_inputs(p.gather_inputs());
        }

        update_counter++;
        if (update_counter == update_cadence) {
            update_counter = 0;
            update_stats();
            update_histograms();
        }
    }


    public void add_inputs(float [] inputs) {
        for (int i=0; i<inputs.length; i++){
            Float input = inputs[i];
            if (!observations.get(i).containsKey(input)){
                observations.get(i).put(input,0);
            }
            observations.get(i).compute(input, (k, a) -> a + 1);
            int t = total_observations.get(i);
            total_observations.set(i, t+1);
        }
    }

    public void update_stats(){
        for (int i=0; i<n_inputs; i++) {
            float sum = 0;
            float count = 0;
            float max = 0;
            float min = Float.MAX_VALUE;
            HashMap<Float, Integer> counts = observations.get(i);

            for (Float value : counts.keySet()){
                int c = counts.get(value);
                sum += value * c;
                count += c;

                if (value < min) {
                    min = value;
                }

                if (value > max) {
                    max = value;
                }
            }

            means.set(i, sum/count);
            max_vals.set(i, max);
            min_vals.set(i, min);
        }
    }

    public void update_histograms(){

        hist_heights = new ArrayList<>();
        hist_bins = new ArrayList<>();

        for (int i=0; i<n_inputs; i++) {

            HashMap<Float, Integer> counts = observations.get(i);
            ArrayList<Float> values = new ArrayList<>(counts.keySet());
            values.sort(Comparator.reverseOrder());

            int n = values.size();
            float max = values.get(0);
            float min = values.get(n-1);

            /*
            get the total number of bins to space out existing values evenly
             */

            float min_dist = Float.MAX_VALUE;
            int total_bins = 1;
            float bin_step = max;

            if (values.size() > 1) {
                for (int j = 1; j < n; j++) {
                    float dist = values.get(j-1) - values.get(j);
                    if (dist < min_dist) {
                        min_dist = dist;
                    }
                }
                total_bins = (int) Math.min((max-min)/min_dist, 100) + 1;
                bin_step = (max-min)/(total_bins - 1);
            }

            /*
            build the histogram
             */
            ArrayList<Integer> heights = new ArrayList<>();
            ArrayList<Float> bins = new ArrayList<>();

            for (int b=0; b<total_bins; b+=1){
                heights.add(0);
                bins.add(min + (b * bin_step));
            }

            /*
            System.out.println( "--- " + i + " ---");
            for (float v : values) {
                System.out.println("v " + v + " : " + counts.get(v) + "\n");
            }*/

            for (float v : values) {
                int bin = (int) Math.floor(((v-min) * (1-1e-10)) / bin_step);

                /*
                System.out.println( "-------\n" +
                    "total_bins " + total_bins + "\n" +
                    "bin " + bin + "\n" +
                    "binstep " + bin_step + "\n" +
                        "min dist " + min_dist + "\n" +
                    "min " + min + "\n" +
                    "max " + max
                );*/

                int h = heights.get(bin);
                heights.set(bin, h + counts.get(v));
            }

            hist_bins.add(bins);
            hist_heights.add(heights);

        }
    }

    public void render() {


        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(game.backgroundColor);
        game.shapeRenderer.rect(
            graph_x,
            graph_y,
            graph_w,
            graph_h
        );
        game.shapeRenderer.end();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        game.shapeRenderer.rect(
            graph_x,
            graph_y,
            graph_w,
            graph_h
        );
        game.shapeRenderer.end();

        float margin = game.VIRTUAL_WIDTH * .02f;
        float plot_area_w = graph_w - (margin * 2);
        float plot_area_h = graph_h - (margin * 2);
        float plot_area_x = graph_x + margin;
        float plot_area_y = graph_y + margin;

        ArrayList<Float> bins = hist_bins.get(current_visible_hist);
        ArrayList<Integer> heights = hist_heights.get(current_visible_hist);

        float bar_width = plot_area_w / bins.size();
        int max_count = Collections.max(heights);
        float scale = plot_area_h * 0.9f / max_count;

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(Color.DARK_GRAY);

        for (int i=0; i<bins.size(); i++) {
            game.shapeRenderer.rect(
                plot_area_x + (bar_width * i),
                plot_area_y,
                bar_width,
                heights.get(i) * scale);
        }
        game.shapeRenderer.end();


        game.batch.begin();

        Color color = game.foregroundColor;
        font.setColor(color);
        String info =
            "input: " + current_visible_hist + "\n" +
            "mean: " + means.get(current_visible_hist) + "\n" +
            "min: " + min_vals.get(current_visible_hist) + "\n" +
            "max: " + max_vals.get(current_visible_hist) + "\n" +
            "n: " + total_observations.get(current_visible_hist) + "\n";
        GlyphLayout layout = new GlyphLayout(font, info);
        font.draw(game.batch, layout, plot_area_x, plot_area_y + plot_area_h);

        game.batch.end();

    }

    public void next() {
        current_visible_hist = (current_visible_hist + 1) % means.size();
    }

    public void last() {
        current_visible_hist = (current_visible_hist + means.size() - 1) % means.size();
    }
}
