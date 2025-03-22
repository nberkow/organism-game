package io.github.organism;

import com.badlogic.gdx.Screen;

public class PlayerHud {

    final float BUTTON_SIDE_DIST = 50;
    float x, y;
    float HUD_Y = 0;
    float SIDE_BUFFER = 10;
    static final float HUD_WIDTH = 400;
    static final float HUD_HEIGHT = 180;
    final float BUTTONS_X  = 60;
    final float BUTTONS_Y  = 25;
    final float ENERGY_BAR_Y = y + HUD_HEIGHT * .4f;
    Player player;

    OrganismGame game;

    Screen screen;
    GameplayButtons gameButtons;
    EnergyBar energy_bar;

    ActionQueueBar action_queue_bar;

    ResourceBars resource_bars;
    float side;
    int parity;

    public PlayerHud(OrganismGame g, Screen scr, Player p, boolean player2){

        game = g;
        screen = scr;
        player = p;

        parity = 1;
        side = 0;

        if (player2) {
            parity = -1;
            side = OrganismGame.VIRTUAL_WIDTH - HUD_WIDTH;
        }

        x = side + SIDE_BUFFER * parity;
        y = HUD_Y + SIDE_BUFFER;

        gameButtons = new GameplayButtons(game, this);
        energy_bar = new EnergyBar(game, this, player, HUD_WIDTH * 1.1f, HUD_HEIGHT / 10);
        action_queue_bar = new ActionQueueBar(game, this);
        resource_bars = new ResourceBars(game, this, player);
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

        player.getOrganism().updateResources();
        player.getOrganism().update_income();

        energy_bar.render();
        gameButtons.render();
        resource_bars.render();
        action_queue_bar.render();
    }

}
