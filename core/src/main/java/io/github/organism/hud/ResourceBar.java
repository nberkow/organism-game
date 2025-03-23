package io.github.organism.hud;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.OrganismGame;
import io.github.organism.Player;

public class ResourceBar {

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

    public ResourceBar(OrganismGame g, PlayerHud ph, Player p){
        game = g;
        font = game.fonts.get(32);
        hud = ph;
        player = p;
        inset = 2;
        y = hud.ENERGY_BAR_Y * 1.4f;
        x = hud.x;
        if (hud.player2){
            x = hud.x + hud.HUD_WIDTH - BAR_WIDTH;
        }
    }

    public void render(){
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int res;
        for (int r=0; r<3; r++){
            res = r;
            if (hud.player2){
                res = 2-r;
            }
            game.shapeRenderer.setColor(game.resourceColorsDark[res]);
            game.shapeRenderer.rect(
                x + inset,
                y + inset + BAR_HEIGHT * res,
                BAR_WIDTH,
                BAR_HEIGHT
            );

            float spacing = BAR_WIDTH / (7);

            game.shapeRenderer.setColor(game.backgroundColor);
            int space_n;
            for (int s=0; s<6; s++){
                space_n = s;
                if (hud.player2){
                    space_n = 5-s;
                }
                game.shapeRenderer.circle(
                    x + inset + (RADIUS * 2) + spacing * space_n,
                    y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                    RADIUS
                );
            }

            game.shapeRenderer.setColor(game.resourceColorsBright[res]);
            int val = Math.min(6, player.getOrganism().resources[res]);

            game.shapeRenderer.circle(
                x + inset + (RADIUS * 2),
                y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                RADIUS / 2
            );


            for (int s=0; s<val; s++){
                space_n = s;
                if (hud.player2){
                    space_n = 5-s;
                }
                game.shapeRenderer.circle(
                    x + inset + (RADIUS * 2) + spacing * space_n,
                    y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                    RADIUS
                );
            }
        }
        game.shapeRenderer.end();
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foreground_color);

        float border_x = x;
        if (hud.player2){
            border_x = x - BAR_WIDTH - inset * 2;
        }
        game.shapeRenderer.rect(
            border_x,
            y,
            BAR_WIDTH * 2 + inset * 2,
            BAR_HEIGHT * 3 + inset * 3
        );
        float font_x = border_x + (BAR_WIDTH * 1.3f);
        if (hud.player2){
            font_x = border_x + hud.HUD_WIDTH - (BAR_WIDTH * 1.3f);
        }
        game.shapeRenderer.end();
        game.batch.begin();
        font.draw(
            game.batch,
            "" + player.getOrganism().income,
            font_x,
            y + BAR_HEIGHT * 2.5f);
        game.batch.end();
    }
}
