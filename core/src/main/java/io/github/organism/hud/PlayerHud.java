package io.github.organism.hud;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;

import java.awt.Component;

import io.github.organism.GameSession;
import io.github.organism.OrganismGame;
import io.github.organism.player.Player;

public class PlayerHud {

    float x;
    final float HUD_WIDTH = .45f;
    final float HUD_HEIGHT = .2f;
    OrganismGame game;
    GameSession gameSession;
    EnergyBar energyBar;
    ResourceBar resourceBar;
    public MoveSpaceControl moveSpaceControl;

    Player player;
    boolean isPlayerOne;
    boolean isPlayerTwo;

    Screen screen;

    float energyBarValue;
    float incomeBarValue;
    float spendBarValue;
    int [] resourceCounts;
    int [] allyResourceCounts;
    int maxResourceCount;

    public PlayerHud(OrganismGame g, GameSession sec, Screen scr, boolean p1, boolean p2){

        game = g;
        gameSession = sec;
        isPlayerOne = p1;
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



    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void updateVisuals(float delta) {
        moveSpaceControl.update(delta);
    }

    /** Delegate cursor setting */
    public void setCursor(Vector2 targetXY) {
        moveSpaceControl.setCursor(targetXY);
    }

    /** Delegate logical position for game logic */
    public Vector2 getLogicalPlanchettePosition(float elapsed) {
        return moveSpaceControl.getLogicalPlanchettePosition(elapsed);
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

    public void setBotInputVector(Vector2 vector) {
        moveSpaceControl.setCursorVector(vector);
    }

    // In PlayerHud.java:

    /** Set cursor target (works for human or bot) */
    public void setTarget(Vector2 targetXY) {
        moveSpaceControl.setTarget(targetXY);
    }



    /** Render full-size HUD (for human players) */
    public void render() {
        resourceBar.render();
        energyBar.render();
        // need to find these center coords once we do the human controls
        //moveSpaceControl.drawAt(centerCoord.x, centerCoord.y, 1.0f);
    }

    /** For human players: accumulate input every frame */
    public void accumulateHumanInput() {
        Vector2 input = getPlayerInputVector();  // From HudInputProcessor
        moveSpaceControl.accumulateInput(input);
    }

    public MoveSpaceControl getMoveSpaceControl() {
        return moveSpaceControl;
    }
}
