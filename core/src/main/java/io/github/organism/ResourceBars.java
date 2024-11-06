package io.github.organism;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ResourceBars {

    final float MARGIN = 5;
    final float BAR_HEIGHT = 15 + MARGIN * 2;
    final float BAR_WIDTH = 90 + MARGIN * 2;

    final float RADIUS = 7;
    GameBoard game_board;
    PlayerHud hud;
    Player player;

    float inset;
    float x, y;

    public  ResourceBars(GameBoard gb, PlayerHud ph, Player p){
        game_board = gb;
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
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        int res;
        for (int r=0; r<3; r++){
            res = r;
            if (hud.parity == -1){
                res = 2-r;
            }
            game_board.shape_renderer.setColor(game_board.resource_colors_dark[res]);
            game_board.shape_renderer.rect(
                x + inset,
                y + inset + BAR_HEIGHT * res,
                BAR_WIDTH,
                BAR_HEIGHT
            );

            float spacing = BAR_WIDTH / (7);

            game_board.shape_renderer.setColor(game_board.background_color);
            int space_n;
            for (int s=0; s<6; s++){
                space_n = s;
                if (hud.parity == -1){
                    space_n = 5-s;
                }
                game_board.shape_renderer.circle(
                    x + inset + (RADIUS * 2) + spacing * space_n,
                    y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                    RADIUS
                );
            }

            game_board.shape_renderer.setColor(game_board.resource_colors_bright[res]);
            int val = Math.min(6, player.get_organism().resources[res]);

            game_board.shape_renderer.circle(
                x + inset + (RADIUS * 2),
                y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                RADIUS / 2
            );


            for (int s=0; s<val; s++){
                space_n = s;
                if (hud.parity == -1){
                    space_n = 5-s;
                }
                game_board.shape_renderer.circle(
                    x + inset + (RADIUS * 2) + spacing * space_n,
                    y + inset + BAR_HEIGHT * res + BAR_HEIGHT/2,
                    RADIUS
                );
            }
        }
        game_board.shape_renderer.end();
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        game_board.shape_renderer.setColor(game_board.foreground_color);

        float border_x = x;
        if (hud.parity == -1){
            border_x = x - BAR_WIDTH - inset * 2;
        }
        game_board.shape_renderer.rect(
            border_x,
            y,
            BAR_WIDTH * 2 + inset * 2,
            BAR_HEIGHT * 3 + inset * 3
        );
        float font_x = border_x + (BAR_WIDTH * 1.3f);
        if (hud.parity == -1){
            font_x = border_x + hud.HUD_WIDTH - (BAR_WIDTH * 1.3f);
        }
        game_board.shape_renderer.end();
        game_board.batch.begin();
        game_board.font.draw(
            game_board.batch,
            "" + player.get_organism().income,
            font_x,
            y + BAR_HEIGHT * 2.5f);
        game_board.batch.end();
    }
}
