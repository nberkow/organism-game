package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnergyBar {

    final float MARGIN = 45f;
    final float BORDER_WIDTH = 4f;

    final float GAP_WIDTH = 2f;
    final float BAR_HEIGHT = 20;

    String player_name;

    float center_x;

    GameBoard game_board;
    float y_height;
    float x_width;
    public EnergyBar(GameBoard gb, String n, float c_x){
        game_board = gb;
        player_name = n;
        center_x = c_x;
        y_height =  game_board.main.VIRTUAL_HEIGHT / game_board.ENERGY_BAR_HEIGHT;
        x_width = game_board.main.VIRTUAL_WIDTH / 2f;
    }


    public void render(){

        /*
        discrete bars
         */

    }
}
