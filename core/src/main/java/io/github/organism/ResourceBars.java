package io.github.organism;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ResourceBars {

    final float MARGIN = 5;
    final float BAR_HEIGHT = 15 + MARGIN * 2;
    final float BAR_WIDTH = 90 + MARGIN * 2;

    final float RADIUS = 7;
    OrganismGame game;
    PlayerHud hud;
    Player player;

    float inset;
    float x, y;

    BitmapFont font;

    public  ResourceBars(OrganismGame g, PlayerHud ph, Player p){
        game = g;
        font = game.fonts.get(32);
        hud = ph;
        player = p;
        inset = 2;
        y = hud.ENERGYBAR_Y * 1.4f;
        x = hud.x;
        if (hud.parity == -1){
            x = hud.x + hud.HUD_WIDTH - BAR_WIDTH;
        }
    }

    public void render(){
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        int res;
        for (int r=0; r<3; r++){
            res = r;
            if (hud.parity == -1){
                res = 2-r;
            }
            game.shape_renderer.setColor(game.resource_colors_dark[res]);
            game.shape_renderer.rect(
                x + inset,
                y + inset + BAR_HEIGHT * res,
                BAR_WIDTH,
                BAR_HEIGHT
            );

            float spacing = BAR_WIDTH / (7);

            game.shape_renderer.setColor(game.background_color);
            int space_n;
            for (int s=0; s<6; s++){
                space_n = s;
                if (hud.parity == -1){
                    space_n = 5-s;
                }
                game.shape_renderer.circle(
                    x + inset + (RADIUS * 2) + spacing * space_n,
                    y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                    RADIUS
                );
            }

            game.shape_renderer.setColor(game.resource_colors_bright[res]);
            int val = Math.min(6, player.get_organism().resources[res]);

            game.shape_renderer.circle(
                x + inset + (RADIUS * 2),
                y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                RADIUS / 2
            );


            for (int s=0; s<val; s++){
                space_n = s;
                if (hud.parity == -1){
                    space_n = 5-s;
                }
                game.shape_renderer.circle(
                    x + inset + (RADIUS * 2) + spacing * space_n,
                    y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                    RADIUS
                );
            }
        }
        game.shape_renderer.end();
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game.shape_renderer.setColor(game.foreground_color);

        float border_x = x;
        if (hud.parity == -1){
            border_x = x - BAR_WIDTH - inset * 2;
        }
        game.shape_renderer.rect(
            border_x,
            y,
            BAR_WIDTH * 2 + inset * 2,
            BAR_HEIGHT * 3 + inset * 3
        );
        float font_x = border_x + (BAR_WIDTH * 1.3f);
        if (hud.parity == -1){
            font_x = border_x + hud.HUD_WIDTH - (BAR_WIDTH * 1.3f);
        }
        game.shape_renderer.end();
        game.batch.begin();
        font.draw(
            game.batch,
            "" + player.get_organism().income,
            font_x,
            y + BAR_HEIGHT * 2.5f);
        game.batch.end();
    }
}
