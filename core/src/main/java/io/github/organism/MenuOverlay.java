package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
    boolean showNewTournamentMenu;
    int selectedPlayerCount; // 0, 1, or 2

    // Main menu button bounds
    float newTournamentButtonY;
    float exitButtonY;

    // New tournament submenu bounds
    float player0ButtonX;
    float player1ButtonX;
    float player2ButtonX;
    float playerButtonY;

    float mapSettingsButtonY;
    float labSettingsButtonY;

    // Coming soon message
    boolean showComingSoonMessage;
    float comingSoonMessageTime;
    static final float COMING_SOON_DURATION = 2.0f;

    public MenuOverlay(OrganismGame g, GameScreen screen) {
        game = g;
        gameScreen = screen;
        visible = false;
        showNewTournamentMenu = false;
        selectedPlayerCount = 1; // Default to 1 player
        showComingSoonMessage = false;
        comingSoonMessageTime = 0;

        // Center overlay
        overlayW = OrganismGame.VIRTUAL_WIDTH * 0.4f;
        overlayH = OrganismGame.VIRTUAL_HEIGHT * 0.9f;
        overlayX = (OrganismGame.VIRTUAL_WIDTH - overlayW) / 2f;
        overlayY = (OrganismGame.VIRTUAL_HEIGHT - overlayH) / 2f;

        buttonWidth = overlayW * 0.7f;
        buttonHeight = 40;
        buttonSpacing = 60;

        font = game.fonts.get(16);

        setupButtons();
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    private void setupButtons() {
        float centerX = overlayX + overlayW / 2f;
        float startY = overlayY + overlayH - 100;

        // Main menu buttons
        newTournamentButtonY = startY;
        exitButtonY = startY - buttonSpacing;

        // New tournament submenu - player selection radio buttons
        playerButtonY = overlayY + overlayH - 150;
        float playerButtonSpacing = 100;
        player0ButtonX = centerX - playerButtonSpacing;
        player1ButtonX = centerX;
        player2ButtonX = centerX + playerButtonSpacing;

        // Settings buttons below player selection
        mapSettingsButtonY = playerButtonY - 120;
        labSettingsButtonY = mapSettingsButtonY - buttonSpacing;
    }

    public void toggle() {
        visible = !visible;
        showNewTournamentMenu = false;
        showComingSoonMessage = false;
    }

    public void handleClick(float x, float y) {
        if (!visible) return;

        if (showNewTournamentMenu) {
            // Check player count radio buttons (60x60 squares)
            float radioSize = 60;
            if (y >= playerButtonY && y <= playerButtonY + radioSize) {
                if (Math.abs(x - player0ButtonX) < radioSize / 2f) {
                    selectedPlayerCount = 0;
                } else if (Math.abs(x - player1ButtonX) < radioSize / 2f) {
                    selectedPlayerCount = 1;
                } else if (Math.abs(x - player2ButtonX) < radioSize / 2f) {
                    selectedPlayerCount = 2;
                }
            }

            // Check settings buttons
            float centerX = overlayX + overlayW / 2f;
            float buttonLeft = centerX - buttonWidth / 2f;
            float buttonRight = centerX + buttonWidth / 2f;

            if (x >= buttonLeft && x <= buttonRight) {
                if (y >= mapSettingsButtonY && y <= mapSettingsButtonY + buttonHeight) {
                    // Go to map settings
                    visible = false;
                    showNewTournamentMenu = false;
                    game.setScreen(game.mapSettingsScreen);
                    Gdx.input.setInputProcessor(game.mapSettingsScreen.inputProcessor);
                } else if (y >= labSettingsButtonY && y <= labSettingsButtonY + buttonHeight) {
                    // Lab settings - coming soon
                    // TODO: This will be re-implemented as AI controls
                    // LabScreen labScreen; // Placeholder for future AI controls screen
                    showComingSoonMessage = true;
                    comingSoonMessageTime = 0;
                }
            }

            // Check start tournament button (at bottom)
            float startButtonY = overlayY + 60;
            if (x >= buttonLeft && x <= buttonRight) {
                if (y >= startButtonY && y <= startButtonY + buttonHeight) {
                    restartWithPlayers(selectedPlayerCount);
                }
            }

        } else {
            // Main menu
            float centerX = overlayX + overlayW / 2f;
            float buttonLeft = centerX - buttonWidth / 2f;
            float buttonRight = centerX + buttonWidth / 2f;

            if (x >= buttonLeft && x <= buttonRight) {
                if (y >= newTournamentButtonY && y <= newTournamentButtonY + buttonHeight) {
                    // Show new tournament menu
                    showNewTournamentMenu = true;
                } else if (y >= exitButtonY && y <= exitButtonY + buttonHeight) {
                    // Exit to desktop
                    Gdx.app.exit();
                }
            }
        }
    }

    private void restartWithPlayers(int playerCount) {
        visible = false;
        showNewTournamentMenu = false;

        if (game.arcadeLoop != null) {
            game.arcadeLoop.setup(playerCount);
        }
    }

    public void render() {
        if (!visible) return;

        // Update coming soon message timer
        if (showComingSoonMessage) {
            comingSoonMessageTime += Gdx.graphics.getDeltaTime();
            if (comingSoonMessageTime >= COMING_SOON_DURATION) {
                showComingSoonMessage = false;
            }
        }

        // Draw semi-transparent background
        Gdx.gl.glEnable(GL20.GL_BLEND);
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

        if (showNewTournamentMenu) {
            renderNewTournamentMenu();
        } else {
            renderMainMenu();
        }

        // Draw coming soon message if active
        if (showComingSoonMessage) {
            renderComingSoonMessage();
        }
    }

    private void renderMainMenu() {
        float centerX = overlayX + overlayW / 2f;

        // Draw title
        game.batch.begin();
        String title = "MENU";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, title);
        font.draw(game.batch, title, centerX - layout.width / 2f, overlayY + overlayH - 40);
        game.batch.end();

        // Draw buttons
        drawButton("New Tournament", centerX, newTournamentButtonY);
        drawButton("Exit to Desktop", centerX, exitButtonY);
    }

    private void renderNewTournamentMenu() {
        float centerX = overlayX + overlayW / 2f;

        // Draw title
        game.batch.begin();
        String title = "New Tournament";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, title);
        font.draw(game.batch, title, centerX - layout.width / 2f, overlayY + overlayH - 40);

        // Draw "Human Players:" label
        String label = "Human Players:";
        layout.setText(font, label);
        font.draw(game.batch, label, centerX - layout.width / 2f, playerButtonY + 90);
        game.batch.end();

        // Draw player count radio buttons
        drawRadioButton("0", player0ButtonX, playerButtonY, selectedPlayerCount == 0);
        drawRadioButton("1", player1ButtonX, playerButtonY, selectedPlayerCount == 1);
        drawRadioButton("2", player2ButtonX, playerButtonY, selectedPlayerCount == 2);

        // Draw settings buttons
        drawButton("Map Settings", centerX, mapSettingsButtonY);
        drawButton("Lab Settings", centerX, labSettingsButtonY);

        // Draw start button at bottom
        float startButtonY = overlayY + 60;
        drawButton("Start Tournament", centerX, startButtonY);
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
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, text);
        font.draw(game.batch, text, centerX - layout.width / 2f, y + buttonHeight / 2f + layout.height / 2f);
        game.batch.end();
    }

    private void drawRadioButton(String text, float centerX, float y, boolean selected) {
        float size = 60;

        // Draw button box (filled if selected)
        if (selected) {
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.setColor(game.foregroundColor);
            game.shapeRenderer.rect(centerX - size / 2f, y, size, size);
            game.shapeRenderer.end();
        }

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(centerX - size / 2f, y, size, size);
        game.shapeRenderer.end();

        // Draw text (inverted color if selected)
        game.batch.begin();
        if (selected) {
            font.setColor(Color.BLACK);
        } else {
            font.setColor(game.backgroundColor);
        }
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, text);
        font.draw(game.batch, text, centerX - layout.width / 2f, y + size / 2f + layout.height / 2f);
        font.setColor(Color.WHITE); // Reset to default
        game.batch.end();
    }

    private void renderComingSoonMessage() {
        float centerX = OrganismGame.VIRTUAL_WIDTH / 2f;
        float centerY = OrganismGame.VIRTUAL_HEIGHT / 2f;
        float msgW = 300;
        float msgH = 100;

        // Draw message box
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.95f);
        game.shapeRenderer.rect(centerX - msgW / 2f, centerY - msgH / 2f, msgW, msgH);
        game.shapeRenderer.end();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        game.shapeRenderer.setColor(game.foregroundColor);
        game.shapeRenderer.rect(centerX - msgW / 2f, centerY - msgH / 2f, msgW, msgH);
        game.shapeRenderer.end();

        // Draw text
        game.batch.begin();
        String msg = "Coming Soon";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, msg);
        font.draw(game.batch, msg, centerX - layout.width / 2f, centerY + layout.height / 2f);
        game.batch.end();
    }
}
