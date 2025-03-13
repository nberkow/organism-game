package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PlayerSummaryDisplay {


    float x, y;
    Player player;

    GameBoard game_board;

    final float ENERGY_BAR_HEIGHT = 8;

    final float INCOME_BAR_HEIGHT = 8;

    final float ACTION_RADIUS = ENERGY_BAR_HEIGHT / 2;

    float energy_bar_width;

    final float NAME_HEIGHT = 50;

    final float ENERGY_BAR_Y = 28;
    final float INCOME_BAR_Y= 22;

    final float ACTION_BAR_Y= 18;

    BitmapFont font;

    PlayerSummaryDisplay(GameBoard gb, Player p, float x, float y){
        this.x = x;
        this.y = y;
        player = p;
        game_board = gb;
        energy_bar_width = ACTION_RADIUS * (game_board.MAX_QUEUED_ACTIONS - 1);
        font = game_board.game.fonts.get(16);
    }

    public void render(){
        draw_name();
        draw_energy_bar();
        draw_action_queue();
    }

    public void draw_name(){

        if (game_board.game.batch == null) {
            return;
        }

        font.setColor(player.get_color());

        game_board.game.batch.begin();
        font.draw(game_board.game.batch, player.getPlayerName(), x, NAME_HEIGHT + y);
        game_board.game.batch.end();
        font.setColor(game_board.game.foreground_color);
    }
    public void draw_energy_bar(){
        if (game_board.game.shapeRenderer == null) {
            return;
        }

        game_board.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game_board.game.shapeRenderer.setColor(game_board.game.foreground_color);
        game_board.game.shapeRenderer.rect(
            x, y + ENERGY_BAR_Y,
            (float) (energy_bar_width * (player.getOrganism().energy / 100f)),
            ENERGY_BAR_HEIGHT);
        game_board.game.shapeRenderer.end();
    }

    public void draw_income_bar(){
        game_board.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game_board.game.shapeRenderer.setColor(game_board.game.foreground_color);
        game_board.game.shapeRenderer.rect(
            x, y + INCOME_BAR_Y,
            (float) (energy_bar_width * (player.getOrganism().energy / 100f)),
            ENERGY_BAR_HEIGHT);
        game_board.game.shapeRenderer.end();
    }

    public void draw_action_queue(){

        if (game_board.game.shapeRenderer == null){
            return;
        }

        Color color;
        for (int i=0; i<player.get_move_queue().size(); i++){
            Integer state = player.get_move_queue().get(i);
            color = game_board.game.action_colors[state];

            game_board.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game_board.game.shapeRenderer.setColor(color);
            game_board.game.shapeRenderer.circle(
                x + ACTION_RADIUS * (i) * 2 + (ACTION_RADIUS / 2),
                y + ACTION_BAR_Y,
                ACTION_RADIUS * 0.9f);
            game_board.game.shapeRenderer.end();
        }

    }
}
