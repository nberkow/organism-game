package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RoundSummary {


    Point winner_id;
    OrganismGame game;
    BitmapFont font16;
    BitmapFont font32;
    GlyphLayout layout;

    Simulation simulation;
    float font_x;
    float font_y;
    String text;
    float box_x;
    float box_y;
    float box_width;
    float box_height;
    float margin;
    float text_center_x;
    float text_center_y;

    float standings_y;

    float standings_width;
    float standings_v_space;

    ArrayList<String> standings_players;
    ArrayList<String> standings_wins;

    ArrayList<Point> standings_ids;
    HashMap<Integer, ArrayList<Point>> players_by_win;
    public RoundSummary(OrganismGame g, Simulation sim){
        game = g;
        simulation = sim;
        font32 = game.fonts.get(32);
        font16= game.fonts.get(16);

        box_width = game.VIRTUAL_WIDTH / 1.5f;
        box_height = game.VIRTUAL_WIDTH / 2.5f;

        box_x = (game.VIRTUAL_WIDTH  - box_width)/2;
        box_y = (game.VIRTUAL_HEIGHT - box_height)/2;

        text_center_x = game.VIRTUAL_WIDTH / 2f;
        text_center_y = game.VIRTUAL_HEIGHT * .75f;

        standings_y = game.VIRTUAL_HEIGHT / 1.7f;

        margin = game.VIRTUAL_WIDTH / 40f;
        standings_width = box_width / 3f;
        standings_v_space = box_height / 20f;

    }

    public void set_winner(Point w) {
        winner_id = w;
        text = simulation.player_names.get(winner_id) + " wins!";

        format_standings();
    }

    public void format_standings(){
        players_by_win = new HashMap<>();

        standings_players = new ArrayList<>();
        standings_wins = new ArrayList<>();
        standings_ids = new ArrayList<>();

        for (Point p : simulation.player_names.keySet()){
            Point rec = simulation.win_records.get(p);
            int win_margin = (rec.x - rec.y) * (rec.x + rec.y);
            if (!players_by_win.containsKey(win_margin)) {
                players_by_win.put(win_margin, new ArrayList<>());
            }
            players_by_win.get(win_margin).add(p);
        }

        ArrayList<Integer> unique_win_margins = new ArrayList<>(players_by_win.keySet()) ;
        unique_win_margins.sort(Comparator.reverseOrder());
        int lines_to_print = 10;

        ArrayList<Point> tied_players;
        for (Integer w : unique_win_margins) {
            tied_players = players_by_win.get(w);
            if (lines_to_print >= tied_players.size()) {

                for (Point  p : tied_players) {
                    standings_ids.add(p);
                    String name = simulation.player_names.get(p);
                    Point rec = simulation.win_records.get(p);
                    standings_players.add(name);
                    standings_wins.add(rec.x + "/" + (rec.x + rec.y));
                    lines_to_print--;
                }

            } else lines_to_print = 0;
        }
    }

    public void render(){
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.setColor(game.background_color);
        game.shape_renderer.rect(box_x, box_y, box_width, box_height);
        game.shape_renderer.end();

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game.shape_renderer.setColor(Color.DARK_GRAY);
        game.shape_renderer.rect(
            box_x + margin,
            box_y + margin,
            box_width - (margin * 2),
            box_height - (margin * 2));
        game.shape_renderer.end();

        game.batch.begin();
        font32.setColor(simulation.tournament_player_colors.get(winner_id));
        layout = new GlyphLayout(font32, text);
        font_x = text_center_x - (layout.width / 2);
        font_y = text_center_y;
        font32.draw(game.batch, layout, font_x, font_y);

        float standings_x = (game.VIRTUAL_WIDTH / 2f) - (standings_width / 2f);

        for (int i=0; i<standings_players.size(); i++){
            Point p = standings_ids.get(i);

            font16.setColor(simulation.tournament_player_colors.get(p));

            font16.draw(game.batch,
                standings_players.get(i),
                standings_x,
                standings_y - (standings_v_space * i));

            font16.draw(game.batch,
                standings_wins.get(i),
                standings_x + standings_width,
                standings_y - (standings_v_space * i));
        }

        game.batch.end();
    }
}
