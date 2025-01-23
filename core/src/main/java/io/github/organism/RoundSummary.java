package io.github.organism;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class RoundSummary {


    int [] winner_id;
    OrganismGame game;
    BitmapFont font;
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
    float standings_vspace;

    ArrayList<GlyphLayout> standings_layouts_players;
    ArrayList<GlyphLayout> standings_layouts_wins;

    ArrayList<int []> standings_ids;
    HashMap<Integer, ArrayList<int []>> players_by_win;
    public RoundSummary(OrganismGame g, Simulation sim){
        game = g;
        simulation = sim;
        font = game.font;

        box_x = game.VIRTUAL_WIDTH / 4f;
        box_y = game.VIRTUAL_WIDTH * 3 / 4f;

        box_width = game.VIRTUAL_WIDTH / 2f;
        box_height = game.VIRTUAL_WIDTH / 2f;

        text_center_x = game.VIRTUAL_WIDTH / 2f;
        text_center_y = game.VIRTUAL_WIDTH / 1.5f;

        standings_y = game.VIRTUAL_HEIGHT / 1.7f;

        margin = game.VIRTUAL_WIDTH / 40f;
        standings_width = game.VIRTUAL_WIDTH / 5f;
        standings_vspace = game.VIRTUAL_WIDTH / 20f;


    }

    public void set_winner(int[] w) {
        winner_id = w;
        text = simulation.player_names.get(w) + " wins!";

        layout = new GlyphLayout(font, text);

        font_x = text_center_x - (layout.width / 2);
        font_y = text_center_y + (layout.height / 2);

        format_standings();
    }

    public void format_standings(){
        players_by_win = new HashMap<>();

        standings_layouts_players = new ArrayList<>();
        standings_layouts_wins = new ArrayList<>();
        standings_ids = new ArrayList<>();

        for (int [] p : simulation.player_names.keySet()){
            Integer wins = simulation.win_records.get(p);
            if (!players_by_win.containsKey(wins)) {
                players_by_win.put(wins, new ArrayList<>());
            }
            players_by_win.get(wins).add(p);
        }

        ArrayList<Integer> unique_wins = new ArrayList<>(players_by_win.keySet()) ;
        unique_wins.sort(Comparator.reverseOrder());

        for (Integer w : unique_wins){
            for (int [] p : players_by_win.get(w)){
                standings_ids.add(p);
                String name = simulation.player_names.get(p);
                standings_layouts_players.add(new GlyphLayout(font, name));
                standings_layouts_wins.add(new GlyphLayout(font, "" + w));
            }
        }
    }

    public void render(){
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.setColor(game.background_color);
        game.shape_renderer.rect(box_x, box_y, box_width, box_height);
        game.shape_renderer.end();

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game.shape_renderer.setColor(game.foreground_color);
        game.shape_renderer.rect(
            box_x + margin,
            box_y + margin,
            box_width - (margin * 2),
            box_height - (margin * 2));
        game.shape_renderer.end();


        game.batch.begin();
        font.setColor(simulation.player_colors.get(winner_id));
        game.font.getData().setScale(4f);
        font.draw(game.batch, layout, font_x, font_y);

        game.font.getData().setScale(2f);

        float standings_x = (game.VIRTUAL_WIDTH / 2f) - (standings_width / 2f);

        for (int i=0; i<standings_layouts_players.size(); i++){
            int [] p = standings_ids.get(i);
            font.setColor(game.foreground_color);
            if (p == winner_id) {
                font.setColor(simulation.player_colors.get(winner_id));
            }

            font.draw(game.batch,
                standings_layouts_players.get(i),
                standings_x,
                standings_y - (standings_vspace * i));

            font.draw(game.batch,
                standings_layouts_wins.get(i),
                standings_x + standings_width,
                standings_y - (standings_vspace * i));

        }

        game.batch.end();
    }
}
