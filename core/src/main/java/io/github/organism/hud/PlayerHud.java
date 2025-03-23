package io.github.organism.hud;

import com.badlogic.gdx.Screen;

import io.github.organism.DoublePair;
import io.github.organism.GameSession;
import io.github.organism.OrganismGame;
import io.github.organism.Pair;

public class PlayerHud {

    float x, y;
    float HUD_Y = 0;
    float SIDE_BUFFER = 10;
    final float HUD_WIDTH = .45f;
    final float HUD_HEIGHT = .2f;
    final float ENERGY_BAR_Y = y + HUD_HEIGHT * .4f;
    OrganismGame game;
    GameSession gameSession;
    EnergyBar energy_bar;
    ResourceBar resource_bars;
    MoveSpaceDisplay moveSpaceDisplay;
    boolean player2;

    float energyBarValue;
    int [] resourceCounts;
    int [] allyResourceCounts;

    public PlayerHud(OrganismGame g, GameSession sec, Screen scr, boolean p2){

        game = g;
        gameSession = sec;
        player2 = p2;

        energyBarValue = 50;
        resourceCounts = new int[]{10, 2, 5};
        allyResourceCounts = new int[]{2, 5, 5};

        setupEnergyBar();
        setupMoveSpaceDisplay();
        setupResourceBar();
        setupIncomeDisplay();

    }

    private void setupIncomeDisplay() {
    }

    private void setupResourceBar() {
    }

    private void setupMoveSpaceDisplay() {
        moveSpaceDisplay = new MoveSpaceDisplay(this);
    }

    private void setupEnergyBar() {
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

        moveSpaceDisplay.render();

    }

    public DoublePair<Double> getTheta() {
        HudInputProcessor hudInputProcessor = (HudInputProcessor) gameSession.getInputProcessor();
        return hudInputProcessor.getThetaFromKeys(player2);
    }
}
