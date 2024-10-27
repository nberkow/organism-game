package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GridWindow {


    GameBoard game_board;
    Integer dimension;

    Float hex_side_len;
    Float hex_spacing;

    Float center_x;
    Float center_y;

    Float small_circle_radius;

    GridWindow(GameBoard gb, Integer dim) {
        game_board = gb;
        dimension = dim;
        hex_side_len = 60.0f/game_board.radius; // starting default
        hex_spacing = 130F/game_board.radius; // starting default
        center_x = game_board.main.VIRTUAL_WIDTH / 2f;
        center_y = game_board.main.VIRTUAL_HEIGHT / game_board.GRID_WINDOW_HEIGHT;
        small_circle_radius = 100f;
    }

    public void render(){
        for (Hexel h : game_board.universal_grid.map_grid) {
            draw_hex(h);
        }
    }

    private void draw_hex(Hexel h){

        // coordinates for six small circles
        float t = (float) Math.pow(3, 0.5) / 4;
        float s = 1/2f;
        float[] hex_x = {0, -t, t, -t, t, 0};
        float[] hex_y = {s, -s/2, -s/2, s/2, s/2, -s};

        float [] small_circle_energy = {(float) h.resources, 0};
        if (h.resources > game_board.HEX_MAX_ENERGY / 2f){
            small_circle_energy[0] = game_board.HEX_MAX_ENERGY / 2f;
            small_circle_energy[1] = (float) (h.resources - small_circle_energy[0]);
        }

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game_board.shape_renderer.setColor(game_board.resource_color);
        for (int i=0; i < 6; i++){
            game_board.shape_renderer.circle(
                h.x * hex_spacing + center_x + hex_x[i] * hex_side_len * 2.5f,
                h.y * hex_spacing + center_y + hex_y[i] * hex_side_len * 2.5f,
                (float) Math.log(small_circle_radius * small_circle_energy[i /3]/game_board.HEX_MAX_ENERGY));
        }
        game_board.shape_renderer.end();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        for (String p : h.assimilation_by_player.keySet()){
            float assimilation = h.assimilation_by_player.get(p).floatValue();
            Color player_color = game_board.players.get(p).get_organism().color;
            game_board.shape_renderer.setColor(player_color);
            game_board.shape_renderer.circle(h.x * hex_spacing + center_x, h.y * hex_spacing + center_y, (float) (hex_side_len * Math.pow(assimilation, 0.25f)));

        }
        game_board.shape_renderer.end();
    }

    public void dispose() {
    }
}
