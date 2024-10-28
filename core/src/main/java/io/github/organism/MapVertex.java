package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;

public class MapVertex implements MapElement{

    GridPosition pos;
    ArrayList<MapHex> adjacent_hexes;

    public Player player;
    public final char type = 'V';

    public MapVertex(GridPosition p) {
        pos = p;
        adjacent_hexes = new ArrayList<>();
    }

    @Override
    public char get_type() {
        return type;
    }

    @Override
    public Player get_player() {
        return player;
    }

    /**
     *
     */
    @Override
    public void render() {
        float x = (float) ((pos.j * Math.pow(3f, 0.5f) / 2f) - (pos.k * Math.pow(3f, 0.5f) / 2f));
        float y = pos.i - pos.j/2f - pos.k/2f;

        int r = 2;

        Color c = Color.DARK_GRAY;

        pos.grid.game_board.shape_renderer.setColor(c);
        pos.grid.game_board.shape_renderer.circle(
            x * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_x,
            y * pos.grid.game_board.hex_side_len + pos.grid.game_board.center_y,
            3);
    }

}
