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
    float lastEnergyValue;
    float energySpentThisTurn;
    int [] resourceCounts;
    int [] allyResourceCounts;
    int maxResourceCount;
    boolean [] isResourceLeader;

    public PlayerHud(OrganismGame g, GameSession sec, Screen scr, boolean p1, boolean p2){

        game = g;
        gameSession = sec;
        isPlayerOne = p1;
        isPlayerTwo = p2;
        screen = scr;

        incomeBarValue = 0f;
        spendBarValue = 0f;
        energyBarValue = 0f;
        lastEnergyValue = 0f;
        energySpentThisTurn = 0f;

        resourceCounts = new int[3];
        allyResourceCounts = new int[3];
        isResourceLeader = new boolean[3];

        float moveSpaceRadius = OrganismGame.VIRTUAL_WIDTH * 0.075f;
        float barWidth = (OrganismGame.VIRTUAL_WIDTH - moveSpaceRadius)/2 * .7f;
        float barHeight = moveSpaceRadius/1.5f;

        setupMoveSpaceDisplay(moveSpaceRadius);
        setupEnergyBar(barWidth, barHeight, 0);
        setupResourceBar(barWidth, barHeight, barHeight/2 + barHeight);
        setupIncomeDisplay();

        // Position HUD - player 1 on left, player 2 on right
        if (p1) {
            x = moveSpaceRadius;
        } else if (p2) {
            x = OrganismGame.VIRTUAL_WIDTH - moveSpaceRadius - barWidth - moveSpaceRadius * 1.2f;
        } else {
            x = moveSpaceRadius * 1.5f;
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
        // Normalize income to 0-1 range based on MAX_ENERGY
        incomeBarValue = i / io.github.organism.SettingsManager.MAX_ENERGY;
    }

    public void setEnergy(float e) {
        // Track spending by comparing to last value
        if (e < lastEnergyValue) {
            energySpentThisTurn = lastEnergyValue - e;
            // Keep spend bar visible for a bit longer
            spendBarValue = energySpentThisTurn / io.github.organism.SettingsManager.MAX_ENERGY;
        } else if (e > lastEnergyValue) {
            // Energy increased (income), but keep showing spend for a moment
            // Gradually fade the spend indicator
            spendBarValue *= 0.95f;
            if (spendBarValue < 0.01f) {
                spendBarValue = 0f;
                energySpentThisTurn = 0f;
            }
        }
        lastEnergyValue = e;
        // Normalize energy to 0-1 range based on MAX_ENERGY
        energyBarValue = e / io.github.organism.SettingsManager.MAX_ENERGY;
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

    public void setResourceLeadership(boolean[] leadership) {
        isResourceLeader = leadership;
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
        float moveSpaceRadius = moveSpaceControl.radius;
        float hudBaseY = 0;

        // Calculate positions for HUD elements - player 1 on left, player 2 on right
        float moveSpaceX;
        if (isPlayerOne) {
            moveSpaceX = moveSpaceRadius;
        } else if (isPlayerTwo) {
            moveSpaceX = OrganismGame.VIRTUAL_WIDTH - moveSpaceRadius;
        } else {
            moveSpaceX = moveSpaceRadius;
        }

        float moveSpaceY = hudBaseY + moveSpaceRadius;

        // Draw bars using their existing render methods
        energyBar.render();
        resourceBar.render();
        moveSpaceControl.drawAt(moveSpaceX, moveSpaceY, 1.0f);
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
