package io.github.organism.hud;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;

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
    public MoveSpaceControl moveSpaceControl;
    boolean isPlayerTwo;

    Screen screen;

    float energyBarValue;
    float incomeBarValue;
    float spendBarValue;
    int [] resourceCounts;
    int [] allyResourceCounts;
    int maxResourceCount;

    public PlayerHud(OrganismGame g, GameSession sec, Screen scr, boolean p2){

        game = g;
        gameSession = sec;
        isPlayerTwo = p2;
        screen = scr;

        incomeBarValue = 0f;
        spendBarValue = 0f;
        energyBarValue = 0f;

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
        if (isPlayerTwo) {
            x = OrganismGame.VIRTUAL_WIDTH;
        }

    }

    private void setupIncomeDisplay() {
    }

    private void setupResourceBar(float w, float h, float y) {
        resourceBar = new ResourceBar(game, this, w, h, y);
    }

    private void setupMoveSpaceDisplay(float radius) {
        moveSpaceControl = new MoveSpaceControl(this, radius);
    }

    private void setupEnergyBar(float w, float h, float y) {
        energyBar = new EnergyBar(game, this, w, h, y);
    }

    public void render(){
        resourceBar.render();
        energyBar.render();
        moveSpaceControl.render();
    }

    public Vector2 getPlayerInputVector() {
        HudInputProcessor hudInputProcessor = (HudInputProcessor) gameSession.getInputProcessor();
        return hudInputProcessor.getInputVectorFromKeys(isPlayerTwo);
    }

    public Vector2 getBotInputVector() {
        return moveSpaceControl.getCursorAsVector();
    }

    public Vector2 getPlanchetteVector() {
        return moveSpaceControl.getPlanchetteFromCenterVector();
    }

    public void setIncome(float i) {
        incomeBarValue = i;
    }

    public void setEnergy(float e) {
        energyBarValue = e;
    }
    public void setSpend(float s) {
        spendBarValue = s;
    }

    public void setResources(int[] resources) {
        resourceCounts = resources;
        maxResourceCount = 0;
        for (int i=0; i<3; i++){
            if (resources[i] > maxResourceCount) {
                maxResourceCount = resources[i];
            }
        }
        if (maxResourceCount > resourceBar.currentMaxCols) {
            resourceBar.currentMaxCols = resourceBar.currentMaxCols * 2;
            resourceBar.calculateResourceDisplayCoords();
        }

        if (maxResourceCount > (2 * resourceBar.currentMaxCols)) {
            resourceBar.currentMaxCols = resourceBar.currentMaxCols / 2;
            resourceBar.calculateResourceDisplayCoords();
        }
    }

    public void setBotCursorVector(Vector2 cursorVector) {
        moveSpaceControl.setCursorVector(cursorVector);
    }

    public void setBotInputVector(Vector2 vector) {
        moveSpaceControl.setCursorVector(vector);
    }
}
