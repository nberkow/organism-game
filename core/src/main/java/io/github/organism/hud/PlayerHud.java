package io.github.organism.hud;

import com.badlogic.gdx.Screen;

import io.github.organism.DoublePair;
import io.github.organism.FloatPair;
import io.github.organism.GameSession;
import io.github.organism.OrganismGame;

public class PlayerHud {

    float x;
    final float HUD_WIDTH = .45f;
    final float HUD_HEIGHT = .2f;
    OrganismGame game;
    GameSession gameSession;
    EnergyBar energyBar;
    ResourceBar resourceBar;
    MoveSpaceControl moveSpaceDisplay;
    boolean player2;

    Screen screen;

    float energyBarValue;
    int [] resourceCounts;
    int [] allyResourceCounts;

    public PlayerHud(OrganismGame g, GameSession sec, Screen scr, boolean p2){

        game = g;
        gameSession = sec;
        player2 = p2;
        screen = scr;

        energyBarValue = 0;
        resourceCounts = new int[3];
        allyResourceCounts = new int[3];

        float moveSpaceRadius = OrganismGame.VIRTUAL_WIDTH * 0.075f;
        float barWidth = (OrganismGame.VIRTUAL_WIDTH - moveSpaceRadius)/2 * .7f;
        float barHeight = moveSpaceRadius/1.5f;

        setupMoveSpaceDisplay(moveSpaceRadius);
        setupEnergyBar(barWidth, barHeight, barHeight/2);
        setupResourceBar(barWidth, barHeight, barHeight/2 + barHeight);
        setupIncomeDisplay();

        x = 0;
        if (player2) {
            x = OrganismGame.VIRTUAL_WIDTH;
        }

    }

    private void setupIncomeDisplay() {
    }

    private void setupResourceBar(float w, float h, float y) {
        resourceBar = new ResourceBar(game, this, w, h, y);
    }

    private void setupMoveSpaceDisplay(float radius) {
        moveSpaceDisplay = new MoveSpaceControl(this, radius);
    }

    private void setupEnergyBar(float w, float h, float y) {
        energyBar = new EnergyBar(game, this, w, h, y);
    }

    public void render(){
        resourceBar.render();
        energyBar.render();
        moveSpaceDisplay.render();
    }

    public DoublePair<Double> getTheta() {
        HudInputProcessor hudInputProcessor = (HudInputProcessor) gameSession.getInputProcessor();
        return hudInputProcessor.getThetaFromKeys(player2);
    }

    public FloatPair<Float> getPlanchettePolar() {
        return new FloatPair<>(
            moveSpaceDisplay.planchettePolar.a / moveSpaceDisplay.radius,
            moveSpaceDisplay.planchettePolar.b
        );

    }
}
