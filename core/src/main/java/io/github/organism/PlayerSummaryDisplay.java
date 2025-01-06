package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PlayerSummaryDisplay {

    float x, y;
    Player player;

    GameBoard game_board;

    final float ENERGY_BAR_HEIGHT = 8;

    final float ACTION_RADIUS = ENERGY_BAR_HEIGHT / 2;

    float energy_bar_width;

    final float NAME_HEIGHT = 50;

    final float ENERGY_BAR_Y = 28;
    final float ACTION_BAR_Y= 22;

    PlayerSummaryDisplay(GameBoard gb, Player p, float x, float y){
        this.x = x;
        this.y = y;
        player = p;
        game_board = gb;
        energy_bar_width = ACTION_RADIUS * (game_board.MAX_QUEUED_ACTIONS - 1);
    }

    public void render(){
        draw_name();
        draw_energy_bar();
        draw_action_queue();
    }

    public void draw_name(){

        Gdx.gl.glEnable(GL20.GL_BLEND);   // Ensure blending is enabled
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);  // Set blend function
        game_board.game.font.getData().setScale(1f);
        game_board.game.font.setColor(player.get_color());

        game_board.batch.begin();
        game_board.game.font.draw(game_board.batch, player.get_player_name(), x, NAME_HEIGHT + y);
        game_board.batch.end();
        game_board.game.font.setColor(game_board.game.foreground_color);
    }
    public void draw_energy_bar(){
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game_board.shape_renderer.setColor(game_board.game.foreground_color);
        game_board.shape_renderer.rect(
            x, y + ENERGY_BAR_Y,
            (float) (energy_bar_width * (player.get_organism().energy / 100f)),
            ENERGY_BAR_HEIGHT);
        game_board.shape_renderer.end();
    }

    public void draw_action_queue(){


        Color color;
        Color outline_color;
        for (int i=0; i<player.get_move_queue().size(); i++){
            Integer state = player.get_move_queue().get(i);
            color = game_board.game.expand_color;
            outline_color = game_board.game.expand_color;
            if (state == 0) {
                color = game_board.game.player_colors[(player.get_index()+2) % 3];
                outline_color = game_board.game.exterminate_color;
            }
            if (state == 2) {
                color = game_board.game.player_colors[(player.get_index()+1) % 3];
                outline_color = game_board.game.exterminate_color;
            }
            game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
            game_board.shape_renderer.setColor(color);
            game_board.shape_renderer.circle(
                x + ACTION_RADIUS * (i) * 2 + (ACTION_RADIUS / 2),
                y + ACTION_BAR_Y,
                ACTION_RADIUS * 0.5f);
            game_board.shape_renderer.end();

            game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
            game_board.shape_renderer.setColor(outline_color);
            game_board.shape_renderer.circle(
                x + ACTION_RADIUS * (i) * 2 + (ACTION_RADIUS / 2),
                y + ACTION_BAR_Y,
                ACTION_RADIUS * 0.9f);
            game_board.shape_renderer.end();
        }

    }
}
