package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;

public class PlayerHud {

    final float BUTTON_SIDE_DIST = 50;
    float x, y;
    float HUD_Y = 0;

    float SIDE_BUFFER = 10;
    final float HUD_WIDTH = 400;
    final float HUD_HEIGHT = 180;
    final float BUTTONS_X  = 60;
    final float BUTTONS_Y  = 25;

    final float ENERGYBAR_Y  = y + HUD_HEIGHT * .4f;


    Player player;
    GameBoard game_board;
    GameplayButtons game_buttons;
    EnergyBar energy_bar;

    ActionQueueBar action_queue_bar;

    ResourceBars resource_bars;
    float side;
    int parity;

    public PlayerHud(GameBoard gb, Player p, boolean player2){
        game_board = gb;

        parity = 1;
        side = 0;

        if (player2) {
            parity = -1;
            side = game_board.main.VIRTUAL_WIDTH - HUD_WIDTH;
        }
        x = side + SIDE_BUFFER * parity;
        y = HUD_Y + SIDE_BUFFER;
        player = p;

        game_buttons = new GameplayButtons(game_board, this);
        energy_bar = new EnergyBar(game_board, this,player, HUD_WIDTH * 1.1f, HUD_HEIGHT / 10);
        action_queue_bar = new ActionQueueBar(game_board, this, player);
        resource_bars = new ResourceBars(game_board, this, player);
    }

    public void render(){

        /* debug rect
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game_board.shape_renderer.setColor(Color.LIGHT_GRAY);
        game_board.shape_renderer.rect(
            x,
            y,
            HUD_WIDTH,
            HUD_HEIGHT);
        game_board.shape_renderer.end();
        */


        energy_bar.render();
        game_buttons.render();
        resource_bars.render();
        action_queue_bar.render();



    }

}
