package io.github.organism;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.organism.player.Player;

public class PlayerSummaryDisplay {


    float x, y;
    Player player;

    GameBoard gameBoard;

    final float CONTROL_CIRCLE_RADIUS = 20;
    final float ENERGY_BAR_HEIGHT = 6;
    final float RESOURCE_BAR_HEIGHT = 5;
    final float BAR_SPACING = 3;

    final float INCOME_BAR_Y = 60;

    float energyBarWidth;
    float resourceBarWidth;

    final float NAME_HEIGHT = 50;

    BitmapFont font;

    PlayerSummaryDisplay(GameBoard gb, Player p, float x, float y){
        this.x = x;
        this.y = y;
        player = p;
        gameBoard = gb;
        energyBarWidth = OrganismGame.VIRTUAL_WIDTH / 8f;
        resourceBarWidth = OrganismGame.VIRTUAL_WIDTH / 10f;
        font = gameBoard.game.fonts.get(16);
    }

    public void render(){
        drawName();
        drawControlCircle();
        drawEnergyBar();
        drawResourceBars();
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
    public void drawControlCircle(){
        if (gameBoard.game.shapeRenderer == null) {
            return;
        }

        float circleX = x + CONTROL_CIRCLE_RADIUS + 5;
        float circleY = y + 15;

        // Draw circle outline
        gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        gameBoard.game.shapeRenderer.setColor(player.getColor());
        gameBoard.game.shapeRenderer.circle(circleX, circleY, CONTROL_CIRCLE_RADIUS);
        gameBoard.game.shapeRenderer.end();

        // Draw planchette and cursor if player has a hud
        if (player.getClass().getSimpleName().equals("BotPlayer")) {
            io.github.organism.player.BotPlayer bot = (io.github.organism.player.BotPlayer) player;
            if (bot.hud != null && bot.hud.moveSpaceControl != null) {
                com.badlogic.gdx.math.Vector2 planchette = bot.hud.getPlanchetteVector();
                com.badlogic.gdx.math.Vector2 cursor = bot.hud.getBotInputVector();

                gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                // Draw cursor (smaller, lighter)
                gameBoard.game.shapeRenderer.setColor(player.getColor().r, player.getColor().g, player.getColor().b, 0.4f);
                gameBoard.game.shapeRenderer.circle(
                    circleX + cursor.x * CONTROL_CIRCLE_RADIUS * 0.7f,
                    circleY + cursor.y * CONTROL_CIRCLE_RADIUS * 0.7f,
                    2
                );

                // Draw planchette (larger, solid)
                gameBoard.game.shapeRenderer.setColor(player.getColor());
                gameBoard.game.shapeRenderer.circle(
                    circleX + planchette.x * CONTROL_CIRCLE_RADIUS * 0.7f,
                    circleY + planchette.y * CONTROL_CIRCLE_RADIUS * 0.7f,
                    4
                );

                gameBoard.game.shapeRenderer.end();
            }
        }
    }

    public void drawEnergyBar(){
        if (gameBoard.game.shapeRenderer == null) {
            return;
        }

        float barX = x + (CONTROL_CIRCLE_RADIUS * 2) + 15;
        float barY = y + 25;

        gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameBoard.game.shapeRenderer.setColor(gameBoard.game.foregroundColor);
        float energyFraction = Math.min(1.0f, player.getOrganism().energy / 100f);
        gameBoard.game.shapeRenderer.rect(
            barX, barY,
            energyBarWidth * energyFraction,
            ENERGY_BAR_HEIGHT);
        gameBoard.game.shapeRenderer.end();

        // Draw outline
        gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        gameBoard.game.shapeRenderer.setColor(player.getColor());
        gameBoard.game.shapeRenderer.rect(barX, barY, energyBarWidth, ENERGY_BAR_HEIGHT);
        gameBoard.game.shapeRenderer.end();
    }

    public void drawResourceBars(){
        if (gameBoard.game.shapeRenderer == null) {
            return;
        }

        float barX = x + (CONTROL_CIRCLE_RADIUS * 2) + 15;
        float barY = y + 25 - ENERGY_BAR_HEIGHT - BAR_SPACING;

        int[] resources = player.getOrganism().resources;
        int maxResources = 0;
        for (int r : resources) {
            if (r > maxResources) maxResources = r;
        }
        if (maxResources == 0) maxResources = 1; // Avoid division by zero

        for (int i = 0; i < 3; i++) {
            float currentBarY = barY - (i * (RESOURCE_BAR_HEIGHT + BAR_SPACING));
            float fillWidth = resourceBarWidth * (resources[i] / (float) maxResources);

            // Draw filled portion
            gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            gameBoard.game.shapeRenderer.setColor(gameBoard.game.resourceColorsDark[i]);
            gameBoard.game.shapeRenderer.rect(barX, currentBarY, fillWidth, RESOURCE_BAR_HEIGHT);
            gameBoard.game.shapeRenderer.end();

            // Draw outline
            gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            gameBoard.game.shapeRenderer.setColor(gameBoard.game.resourceColorsBright[i]);
            gameBoard.game.shapeRenderer.rect(barX, currentBarY, resourceBarWidth, RESOURCE_BAR_HEIGHT);
            gameBoard.game.shapeRenderer.end();

            // Draw leader indicator (gold outline)
            if (gameBoard.resourceLeaders[i] == player) {
                gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                gameBoard.game.shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.GOLD);
                gameBoard.game.shapeRenderer.rect(
                    barX - 2, currentBarY - 2,
                    resourceBarWidth + 4, RESOURCE_BAR_HEIGHT + 4
                );
                gameBoard.game.shapeRenderer.end();
            }
        }
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
