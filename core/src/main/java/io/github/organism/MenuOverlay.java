package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MenuOverlay {

    OrganismGame game;
    GameScreen gameScreen;

    public boolean visible;

    float overlayX;
    float overlayY;
    float overlayW;
    float overlayH;

    float buttonWidth;
    float buttonHeight;
    float buttonSpacing;

    BitmapFont font;

    // Menu states
    boolean showPlayerSelection;

    // Button bounds
    float resumeButtonY;
    float restartButtonY;
    float exitButtonY;

    float player0ButtonX;
    float player1ButtonX;
    float player2ButtonX;
    float playerButtonY;

    public MenuOverlay(OrganismGame g, GameScreen screen) {
        game = g;
        gameScreen = screen;
        visible = false;
        showPlayerSelection = false;

        // Center overlay
        overlayW = OrganismGame.VIRTUAL_WIDTH * 0.4f;
        overlayH = OrganismGame.VIRTUAL_HEIGHT * 0.6f;
        overlayX = (OrganismGame.VIRTUAL_WIDTH - overlayW) / 2f;
        overlayY = (OrganismGame.VIRTUAL_HEIGHT - overlayH) / 2f;

        buttonWidth = overlayW * 0.7f;
        buttonHeight = 40;
        buttonSpacing = 60;

        font = game.fonts.get(32);

        setupButtons();
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    private void setupButtons() {
        float centerX = overlayX + overlayW / 2f;
        float startY = overlayY + overlayH - 100;

        resumeButtonY = startY;
        restartButtonY = startY - buttonSpacing;
        exitButtonY = startY - buttonSpacing * 2;

        // Player selection buttons
        playerButtonY = overlayY + overlayH / 2f;
        float playerButtonSpacing = 100;
        player0ButtonX = centerX - playerButtonSpacing;
        player1ButtonX = centerX;
        player2ButtonX = centerX + playerButtonSpacing;
    }

    public void toggle() {
        visible = !visible;
        showPlayerSelection = false;
    }

    public void handleClick(float x, float y) {
        if (!visible) return;

        if (showPlayerSelection) {
            // Check player count buttons
            if (y >= playerButtonY && y <= playerButtonY + buttonHeight) {
                if (Math.abs(x - player0ButtonX) < 40) {
                    restartWithPlayers(0);
                } else if (Math.abs(x - player1ButtonX) < 40) {
                    restartWithPlayers(1);
                } else if (Math.abs(x - player2ButtonX) < 40) {
                    restartWithPlayers(2);
                }
            }
        } else {
            float centerX = overlayX + overlayW / 2f;
            float buttonLeft = centerX - buttonWidth / 2f;
            float buttonRight = centerX + buttonWidth / 2f;

            if (x >= buttonLeft && x <= buttonRight) {
                if (y >= resumeButtonY && y <= resumeButtonY + buttonHeight) {
                    // Resume
                    toggle();
                } else if (y >= restartButtonY && y <= restartButtonY + buttonHeight) {
                    // Show player selection
                    showPlayerSelection = true;
                } else if (y >= exitButtonY && y <= exitButtonY + buttonHeight) {
                    // Exit to title (not implemented yet)
                    System.out.println("Exit to title - not implemented");
                }
            }
        }
    }

    private void restartWithPlayers(int playerCount) {
        visible = false;
        showPlayerSelection = false;

        if (game.arcadeLoop != null) {
            game.arcadeLoop.setup(playerCount);
        }
    }

    public void render() {
        if (!visible) return;

        // Draw semi-transparent background
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0, 0, 0, 0.7f);
        game.shapeRenderer.rect(0, 0, OrganismGame.VIRTUAL_WIDTH, OrganismGame.VIRTUAL_HEIGHT);
        game.shapeRenderer.end();

        // Draw overlay box
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.95f);
        game.shapeRenderer.rect(overlayX, overlayY, overlayW, overlayH);
        game.shapeRenderer.end();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(overlayX, overlayY, overlayW, overlayH);
        game.shapeRenderer.end();

        if (showPlayerSelection) {
            renderPlayerSelection();
        } else {
            renderMainMenu();
        }
    }

    private void renderMainMenu() {
        float centerX = overlayX + overlayW / 2f;

        // Draw title
        game.batch.begin();
        String title = "PAUSED";
        float titleWidth = font.getRegion().getRegionWidth() * title.length() * 0.5f;
        font.draw(game.batch, title, centerX - titleWidth / 2f, overlayY + overlayH - 40);
        game.batch.end();

        // Draw buttons
        drawButton("Resume Game", centerX, resumeButtonY);
        drawButton("Restart Tournament", centerX, restartButtonY);
        drawButton("Exit to Title", centerX, exitButtonY);
    }

    private void renderPlayerSelection() {
        float centerX = overlayX + overlayW / 2f;

        // Draw title
        game.batch.begin();
        String title = "Select Player Count";
        float titleWidth = font.getRegion().getRegionWidth() * title.length() * 0.4f;
        font.draw(game.batch, title, centerX - titleWidth / 2f, overlayY + overlayH - 40);
        game.batch.end();

        // Draw player count buttons
        drawPlayerButton("0", player0ButtonX, playerButtonY);
        drawPlayerButton("1", player1ButtonX, playerButtonY);
        drawPlayerButton("2", player2ButtonX, playerButtonY);
    }

    private void drawButton(String text, float centerX, float y) {
        float buttonLeft = centerX - buttonWidth / 2f;

        // Draw button box
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(buttonLeft, y, buttonWidth, buttonHeight);
        game.shapeRenderer.end();

        // Draw text
        game.batch.begin();
        float textWidth = font.getRegion().getRegionWidth() * text.length() * 0.3f;
        font.draw(game.batch, text, centerX - textWidth / 2f, y + buttonHeight - 10);
        game.batch.end();
    }

    private void drawPlayerButton(String text, float centerX, float y) {
        float size = 60;

        // Draw button box
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(centerX - size / 2f, y, size, size);
        game.shapeRenderer.end();

        // Draw text
        game.batch.begin();
        float textWidth = font.getRegion().getRegionWidth() * text.length() * 0.5f;
        font.draw(game.batch, text, centerX - textWidth / 2f, y + size / 2f + 10);
        game.batch.end();
    }
}
