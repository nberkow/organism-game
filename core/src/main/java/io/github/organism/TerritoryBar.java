package io.github.organism;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;

public class TerritoryBar {

    float x_pos = 28;
    float y_pos = 28;

    float victory_threshold = 2/3f;
    float bar_width = 40;
    float bar_spacing = 5;
    float max_height = 200;

    HashMap<String, Float> heights;

    OrganismGame game;
    public TerritoryBar(OrganismGame g) {
        game = g;
        heights = new HashMap<>();
    }

    public void render(GameBoard gb){
        logic(gb);
        draw(gb);
    }

    public void logic(GameBoard gb){
        float total_territory = gb.universe_map.vertex_grid.get_unmasked_vertices();
        for (String p : gb.all_player_names) {
            float player_territory = gb.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            heights.put(p, max_height * player_territory / total_territory);
        }
    }

    public void draw(GameBoard gb) {

        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shape_renderer.setColor(Color.GREEN);
        float t_width = bar_width * 3 + bar_spacing * 5;
        game.shape_renderer.rect(x_pos-bar_spacing, victory_threshold * max_height, t_width, 2);


        float x = x_pos;
        for (String p : gb.all_player_names) {
            Player player = gb.players.get(p);
            game.shape_renderer.setColor(player.get_color());
            game.shape_renderer.rect(x, y_pos, bar_width, heights.get(p));
            x += bar_width + bar_spacing;
        }
        game.shape_renderer.end();
        game.shape_renderer.setColor(game.foreground_color);

    }


}
