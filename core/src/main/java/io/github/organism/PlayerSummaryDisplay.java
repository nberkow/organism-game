package io.github.organism;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.player.Player;

public class PlayerSummaryDisplay {


    float x, y;
    Player player;

    GameBoard gameBoard;

    final float ENERGY_BAR_HEIGHT = 8;

    final float INCOME_BAR_HEIGHT = 8;

    final float ACTION_RADIUS = ENERGY_BAR_HEIGHT / 2;

    float energyBarWidth;

    final float NAME_HEIGHT = 50;

    final float ENERGY_BAR_Y = 28;
    final float INCOME_BAR_Y= 22;

    final float ACTION_BAR_Y= 18;
    BitmapFont font;

    PlayerSummaryDisplay(GameBoard gb, Player p, float x, float y){
        this.x = x;
        this.y = y;
        player = p;
        gameBoard = gb;
        energyBarWidth = OrganismGame.VIRTUAL_WIDTH / 6f;
        font = gameBoard.game.fonts.get(16);
    }

    public void render(){
        drawName();
        drawEnergyBar();
    }

    public void drawName(){

        if (gameBoard.game.batch == null) {
            return;
        }

        font.setColor(player.getColor());

        gameBoard.game.batch.begin();
        font.draw(gameBoard.game.batch, player.getPlayerName(), x, NAME_HEIGHT + y);
        gameBoard.game.batch.end();
        font.setColor(gameBoard.game.foregroundColor);
    }
    public void drawEnergyBar(){
        if (gameBoard.game.shapeRenderer == null) {
            return;
        }

        gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameBoard.game.shapeRenderer.setColor(gameBoard.game.foregroundColor);
        gameBoard.game.shapeRenderer.rect(
            x, y + ENERGY_BAR_Y,
            (float) (energyBarWidth * (player.getOrganism().energy / 100f)),
            ENERGY_BAR_HEIGHT);
        gameBoard.game.shapeRenderer.end();
    }

    public void draw_income_bar(){
        gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameBoard.game.shapeRenderer.setColor(gameBoard.game.foregroundColor);
        gameBoard.game.shapeRenderer.rect(
            x, y + INCOME_BAR_Y,
            (float) (energyBarWidth * (player.getOrganism().energy / 100f)),
            ENERGY_BAR_HEIGHT);
        gameBoard.game.shapeRenderer.end();
    }

    public void drawActionHistory(){

        /*
        if (gameBoard.game.shapeRenderer == null){
            return;
        }

        Color color;
        for (int i=0; i<player.get_move_queue().size(); i++){
            Integer state = player.get_move_queue().get(i);
            color = gameBoard.game.action_colors[state];

            gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            gameBoard.game.shapeRenderer.setColor(color);
            gameBoard.game.shapeRenderer.circle(
                x + ACTION_RADIUS * (i) * 2 + (ACTION_RADIUS / 2),
                y + ACTION_BAR_Y,
                ACTION_RADIUS * 0.9f);
            gameBoard.game.shapeRenderer.end();
        }
        */
    }
}
