package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class WinRecordGraph {
    float graph_x;
    float graph_y;
    float graph_w;
    float graph_h;

    OrganismGame game;

    Simulation simulation;

    BitmapFont font;
    float name_spacing;

    public  WinRecordGraph(OrganismGame g, Simulation s, float x, float y, float w, float h){
        game = g;
        simulation = s;
        name_spacing = game.VIRTUAL_HEIGHT * .01f;
        font = game.fonts.get(16);

        graph_x = x;
        graph_y = y;
        graph_w = w;
        graph_h = h;
    }

    public void render() {

        /* test rect
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.rect(
            graph_x,
            graph_y,
            graph_w,
            graph_h
        );
        game.shape_renderer.end();

         */

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foreground_color);
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

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.backgroundColor);
        game.shapeRenderer.rect(
            plot_area_x,
            plot_area_y,
            plot_area_w,
            plot_area_h
        );

        int iterations = simulation.iterations;
        float x_step = plot_area_w/(iterations - 1);

        int y_max = (int) Math.log10(iterations) * 4;
        float y_step = plot_area_h/y_max;

        HashMap<Integer, ArrayList<Point>> living_players = new HashMap<>();

        for (Point p : simulation.winRecords.keySet()){
            Color color = simulation.tournament_player_colors.get(p);
            ArrayList<Point> recs = simulation.winRecords.get(p);

            for (int i=recs.size()-1; i>=1; i--) {
                Point start_rec = recs.get(i);
                Point end_rec = recs.get(i-1);

                int start_x = simulation.winRecordTurns.get(p).get(i-1);
                int start_y = start_rec.x - start_rec.y;

                int end_x = start_x + 1;
                int end_y = end_rec.x - end_rec.y;

                if (end_y >= 0) {
                    game.shapeRenderer.setColor(color);
                    game.shapeRenderer.line(
                        (start_x * x_step) + plot_area_x,
                        (start_y * y_step) + plot_area_y,
                        (end_x * x_step) + plot_area_x,
                        (end_y * y_step) + plot_area_y
                    );

                    if (i == 1 & end_y > 0) {
                        if (!living_players.containsKey(end_y)){
                            living_players.put(end_y, new ArrayList<>());
                        }
                        living_players.get(end_y).add(p);
                    }
                }
            }
        }

        for (ArrayList<Point> redraw_list : living_players.values()){
            for (Point p : redraw_list) {
                Color color = simulation.tournament_player_colors.get(p);
                ArrayList<Point> recs = simulation.winRecords.get(p);
                for (int i=recs.size()-1; i>=1; i--) {
                    Point start_rec = recs.get(i);
                    Point end_rec = recs.get(i-1);

                    int start_x = simulation.winRecordTurns.get(p).get(i-1);
                    int start_y = start_rec.x - start_rec.y;

                    int end_x = start_x + 1;
                    int end_y = end_rec.x - end_rec.y;

                    if (end_y >= 0) {
                        game.shapeRenderer.setColor(color);
                        game.shapeRenderer.line(
                            (start_x * x_step) + plot_area_x,
                            (start_y * y_step) + plot_area_y,
                            (end_x * x_step) + plot_area_x,
                            (end_y * y_step) + plot_area_y
                        );
                    }
                }
            }
        }


        game.shapeRenderer.end();

        ArrayList<Integer> win_margins = new ArrayList<>(living_players.keySet());
        win_margins.sort(Comparator.reverseOrder());

        float text_x = plot_area_x + margin;
        float text_y = plot_area_y + plot_area_h;

        game.batch.begin();

        for (Integer i : win_margins) {
            for (Point p : living_players.get(i)){
                Color color = simulation.tournament_player_colors.get(p);
                font.setColor(color);
                String player_name = simulation.player_names.get(p);
                Point rec = simulation.winRecords.get(p).get(0);
                GlyphLayout layout = new GlyphLayout(font, player_name + "  (" + rec.x + " - " + rec.y + ")");
                text_y -= (layout.height + name_spacing);
                font.draw(game.batch, layout, text_x, text_y);
            }
        }
        game.batch.end();
    }
}
