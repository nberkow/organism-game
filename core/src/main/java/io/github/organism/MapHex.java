package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class MapHex implements MapElement {

    final float RESOURCE_JITTER = 0.4f;

    final float RESOURCE_RADIUS = .15f;
    GridPosition pos;
    MapVertex [] vertex_list;

    int [] resources;
    int total_resources;;

    public Player player;

    public final char type = 'H';

    public MapHex(GridPosition p){
        pos = p;
        vertex_list = new MapVertex [] {null, null, null, null, null, null};
        resources = new int[3];
        total_resources = 0;
    }

    @Override
    public char get_type() {
        return type;
    }

    @Override
    public Player get_player() {
        return player;
    }

    @Override
    public void render() {

        render_resources();
        render_players();


    }

    public void render_players() {

        Color c;
        pos.grid.game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);

        for (int v=0; v<6; v++){
            MapVertex v1 = vertex_list[v];
            float x1 = (float) ((v1.pos.j * Math.pow(3f, 0.5f) / 2f) - (v1.pos.k * Math.pow(3f, 0.5f) / 2f));
            float y1 = v1.pos.i - v1.pos.j/2f - v1.pos.k/2f;

            MapVertex v2 = vertex_list[(v+1) % 6];
            float x2 = (float) ((v2.pos.j * Math.pow(3f, 0.5f) / 2f) - (v2.pos.k * Math.pow(3f, 0.5f) / 2f));
            float y2 = v2.pos.i - v2.pos.j/2f - v2.pos.k/2f;

            c = Color.DARK_GRAY;
            if(v1.player != null && v2.player == v1.player){
                c = v2.player.get_organism().color;
            }
            pos.grid.game_board.shape_renderer.setColor(c);

            pos.grid.game_board.shape_renderer.line(
                x1 * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_x,
                y1 * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_y,
                x2 * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_x,
                y2 * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_y);
        }
        pos.grid.game_board.shape_renderer.end();
    }

    public void render_resources(){
        float [] j = {0f, 0f, RESOURCE_JITTER};

        int n=0;
        pos.grid.game_board.shape_renderer.end();
        pos.grid.game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int r=0; r<total_resources; r++){
            float i_f = pos.i + j[n];
            float j_f = pos.j + j[(n + 1) % 3];
            float k_f = pos.k + j[(n + 2) % 3];

            float x = (float) ((j_f * Math.pow(3f, 0.5f) / 2f) - (k_f * Math.pow(3f, 0.5f) / 2f));
            float y = i_f - j_f/2f - k_f/2f;

            Color c = pos.grid.game_board.resource_colors[resources[r]];


            pos.grid.game_board.shape_renderer.setColor(c);
            pos.grid.game_board.shape_renderer.circle(
                x * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_x,
                y * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_y,
                RESOURCE_RADIUS * pos.grid.game_board.hex_side_len);
            n++;
        }
        pos.grid.game_board.shape_renderer.end();
    }

    public void add_resource(int resource_type) {
        resources[total_resources] = resource_type;
        total_resources += 1;
    }

    public int [] get_resources() {
        return resources;
    }


}
