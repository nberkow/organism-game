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

        float left_start = MARGIN + center_x / 2;

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        game_board.shape_renderer.setColor(game_board.foreground_color);
        game_board.shape_renderer.rect(left_start, y_height, x_width - (MARGIN * 2), BAR_HEIGHT);

        game_board.shape_renderer.setColor(game_board.background_color);
        game_board.shape_renderer.rect(
            left_start + BORDER_WIDTH,
            y_height + BORDER_WIDTH,
            x_width - ((MARGIN + BORDER_WIDTH) * 2),
            BAR_HEIGHT - BORDER_WIDTH * 2);

        game_board.shape_renderer.setColor(game_board.foreground_color);

        double energy_store = game_board.players.get(player_name).get_organism().energy_store;
        game_board.shape_renderer.rect(
            left_start + BORDER_WIDTH + GAP_WIDTH,
            y_height + BORDER_WIDTH + GAP_WIDTH,
            (float) ( (x_width - ((MARGIN + BORDER_WIDTH + GAP_WIDTH) * 2)) * (energy_store / game_board.MAX_ENERGY)),
            BAR_HEIGHT - (BORDER_WIDTH + GAP_WIDTH) * 2);

        game_board.shape_renderer.end();

    }
}
