package io.github.organism;

import com.badlogic.gdx.graphics.Color;

public class MapHex implements MapElement {

    final float RESOURCE_JITTER = 0.3f;

    final float RESOURCE_RADIUS = 5;
    GridPosition pos;
    MapVertex [] vertex_list;

    int [] resources;

    public Player player;

    public final char type = 'H';

    public MapHex(GridPosition p){
        pos = p;
        vertex_list = new MapVertex [] {null, null, null, null, null, null};
        resources = new int[pos.grid.game_board.config.resource_types];
        resources[0] = 1;
        resources[1] = 0;
        resources[2] = 2;
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

        float [] j = {0f, 0f, RESOURCE_JITTER};

        int n=0;
        for (int resource : resources){

            for (int r=0; r<resource;r++){
                float i_f = pos.i + j[n];
                float j_f = pos.j + j[(n + 1) % 3];
                float k_f = pos.k + j[(n + 2) % 3];

                float x = (float) ((j_f * Math.pow(3f, 0.5f) / 2f) - (k_f * Math.pow(3f, 0.5f) / 2f));
                float y = i_f - j_f/2f - k_f/2f;

                Color c = pos.grid.game_board.resource_colors[r];

                pos.grid.game_board.shape_renderer.setColor(c);
                pos.grid.game_board.shape_renderer.circle(
                    x * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_x,
                    y * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_y,
                    RESOURCE_RADIUS);
                n++;
            }

        }
    }

    /**
     * @return
     */

    public int [] get_resources() {
        return resources;
    }


}
