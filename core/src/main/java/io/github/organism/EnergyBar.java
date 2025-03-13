package io.github.organism;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnergyBar {


    final int MAX_ENERGY = 100;
    final float BORDER_WIDTH = 2f;

    final float TICK_SPACING = 1f;
    final float GAP_WIDTH = 2f;

    OrganismGame game;
    Player player;

    PlayerHud hud;

    float x;
    float y;

    float y_height;
    float x_width;

    float small_bar_x;
    float small_bar_y;
    float small_bar_width;
    float small_bar_height;
    BitmapFont font;
    public EnergyBar(OrganismGame g, PlayerHud ph, Player p, float width, float height){
        game = g;
        player = p;
        hud = ph;
        y_height =  height;
        x_width = width;

        y = hud.ENERGYBAR_Y;
        x = hud.x;
        if (hud.parity == -1){
            x = hud.x - (x_width - hud.HUD_WIDTH);
        }

        small_bar_x = width + GAP_WIDTH + BORDER_WIDTH;
        small_bar_y = height + GAP_WIDTH + BORDER_WIDTH;
        small_bar_width = (width - (GAP_WIDTH * 2) - (BORDER_WIDTH * 2) + TICK_SPACING) / MAX_ENERGY - TICK_SPACING;
        small_bar_height = height - (GAP_WIDTH * 2) - (BORDER_WIDTH * 2);

        font = game.fonts.get(32);

    }


    public void render(){

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(game.foreground_color);
        game.shapeRenderer.rect(
            x,
            y,
            x_width,
            y_height);
        game.shapeRenderer.setColor(game.backgroundColor);
        game.shapeRenderer.rect(
            x + GAP_WIDTH,
            y + GAP_WIDTH,
            x_width - (GAP_WIDTH * 2),
            y_height - (GAP_WIDTH * 2));


        float first_x = x + GAP_WIDTH * 2;
        if (hud.parity == -1){
            first_x = x_width + (x - GAP_WIDTH * 2) - small_bar_width;
        }

        game.shapeRenderer.setColor(game.foreground_color);
        for (int i = 0; i<player.getOrganism().energy; i++) {
            game.shapeRenderer.rect(
                first_x + ((small_bar_width + TICK_SPACING) * i * hud.parity),
                y + GAP_WIDTH * 2,
                small_bar_width,
                y_height - (GAP_WIDTH * 4));
        }

        game.shapeRenderer.end();
    }
}
