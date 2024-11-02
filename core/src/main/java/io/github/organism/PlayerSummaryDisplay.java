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
        game_board.font.getData().setScale(1f);

        game_board.batch.begin();
        game_board.font.draw(game_board.batch, player.get_player_name(), x, NAME_HEIGHT + y);
        game_board.batch.end();
    }
    public void draw_energy_bar(){
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        game_board.shape_renderer.setColor(game_board.foreground_color);
        game_board.shape_renderer.rect(
            x, y + ENERGY_BAR_Y,
            (float) (energy_bar_width),
            ENERGY_BAR_HEIGHT);
        game_board.shape_renderer.end();
    }

    public void draw_action_queue(){
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0; i<player.get_move_queue().size(); i++){
            Integer state = player.get_move_queue().get(i);
            game_board.shape_renderer.setColor(game_board.colors[state ]);
            game_board.shape_renderer.circle(
                x + ACTION_RADIUS * (i) * 2 + (ACTION_RADIUS / 2),
                y + ACTION_BAR_Y,
                ACTION_RADIUS * 0.9f);
        }
        game_board.shape_renderer.end();
    }
}
