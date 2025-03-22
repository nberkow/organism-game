package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Locale;

public class TutorialOverlayHandler {

    private final BitmapFont headerFont;

    private final BitmapFont bodyFont;
    public boolean listening;
    Tutorial tutorial;
    OrganismGame game;
    boolean show;
    ArrayList<TutorialOverlay> overlays;

    int currentOverlay;
    private static class TutorialOverlay {
        float x;
        float y;
        float w;
        float h;
        String stepName;
        String heading;
        String text;

        public TutorialOverlay(float x, float y, float w, float h, String head, String s, String t){
            text = s;
            heading = head;
            stepName = t;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
    public TutorialOverlayHandler(OrganismGame g, Tutorial t) {
        game = g;
        tutorial = t;
        currentOverlay = 0;
        show = true;
        listening = true;
        headerFont = game.fonts.get(32);
        bodyFont = game.fonts.get(16);
        setup();
    }

    public void setup(){
        overlays = new ArrayList<>();

        float w = OrganismGame.VIRTUAL_WIDTH * 0.7f;
        float h = OrganismGame.VIRTUAL_HEIGHT * 0.45f;
        overlays.add(
            new TutorialOverlay(
                (OrganismGame.VIRTUAL_WIDTH - w) / 2,
                (OrganismGame.VIRTUAL_HEIGHT - h) / 2,
                w,
                h,
                "Welcome to the Organism Game!",
                "The goal of the game is simple: make as much of yourself as possible and don't go extinct!\n" +
                    "To thrive, your organism will have to EXTRACT resources to get energy. You can spend energy to EXPAND and claim more territory.\n" +
                    "Let's go over the basic controls.",
                "intro"
            )
        );


        w = PlayerHud.HUD_WIDTH * 1.2f;
        h = PlayerHud.HUD_HEIGHT * .8f;
        float x = OrganismGame.VIRTUAL_WIDTH - w * 1.01f;
        float y = OrganismGame.VIRTUAL_HEIGHT * .05f;
        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Controls",
                "EXTRACT: permanently destroy one resource to create energy. " +
                    "Click the center button or press S.\n",
                "extract"
            )
        );

        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Controls",
                    "Expand: Spend energy to claim nearby vertexes. " +
                    "Click the left or right button or press A or D",
                "expand"
            )
        );

        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Controls",
                "Try using EXTRACT and EXPAND to grow your organism's territory now.",
                "sandbox1"
            )
        );

        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Move Queue",
                "Your actions won't execute immediately. Every turn each player performs one action.\n" +
                    "Adding actions between turns will add them to your Action Queue. This is visible to other players, so be careful about revealing your plans.",
                "queue"
            )
        );

        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Income",
                "The amount of energy you gain from EXTRACTING depends on the resources you control.\n" +
                "Each resource has a base value of " + SettingsManager.BASE_RESOURCE_VALUE + " energy. There are three types and it pays to diversify.",
                "income1"
            )
        );


        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Income",
                "When you have more than one type of resource, their total value is multiplied. Your income maxes out when all three resources are at 6.\n" +
                "You can still extract a small amount of energy even if you own no resources",
                "income2"
            )
        );

        overlays.add(
            new TutorialOverlay(
                x,
                y,
                w,
                h,
                "Opponents",
                "Let's add some opponents. You can see their energy levels and the moves they have queued at the top left",
                "income2"
            )
        );
    }

    public void setupDiplomacy(){

    }

    public void advance(){
        currentOverlay++;
        if (currentOverlay == 3) {
            listening = false;
        }
    }


    public void render(){
        logic();
        draw();
    }

    public void logic(){
        if (tutorial.screen.inputProcessor.tutorialAdvance){
            advance();
            tutorial.screen.inputProcessor.tutorialAdvance = false;
        }
    }

    public void draw() {
        if (show) {
            TutorialOverlay overlay = overlays.get(currentOverlay);

            // Draw background
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            game.shapeRenderer.setColor(game.backgroundColor);
            game.shapeRenderer.rect(overlay.x, overlay.y, overlay.w, overlay.h);
            game.shapeRenderer.end();

            // Draw border
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            game.shapeRenderer.setColor(game.foregroundColor);
            game.shapeRenderer.rect(overlay.x, overlay.y, overlay.w, overlay.h);
            game.shapeRenderer.end();

            // Draw text
            game.batch.begin();

            // Draw header text
            GlyphLayout headLayout = new GlyphLayout(headerFont, overlay.heading);
            float headX = overlay.x + (overlay.w - headLayout.width) / 2; // Center horizontally
            float headY = overlay.y + overlay.h - headLayout.height - 10; // 10 pixels padding from top
            headerFont.setColor(game.foregroundColor);
            headerFont.draw(game.batch, headLayout, headX, headY);

            // Draw body text
            bodyFont.setColor(Color.LIGHT_GRAY); // Set color for the body text
            float lineHeight = bodyFont.getLineHeight();
            float textX = overlay.x + (overlay.w - overlay.w * 0.9f) / 2; // Same left boundary as header
            float textY = headY - headLayout.height * 3; // Start below the header with 20 pixels padding

            // Split the text into paragraphs based on explicit newlines
            String[] paragraphs = overlay.text.split("\n");
            for (String paragraph : paragraphs) {
                GlyphLayout paragraphLayout = new GlyphLayout(
                    bodyFont,
                    paragraph,
                    Color.LIGHT_GRAY,
                    overlay.w * 0.9f,
                    0,
                    true
                );

                Array<GlyphLayout.GlyphRun> runs = paragraphLayout.runs;
                int runIndex = 0;
                while (runIndex < runs.size) {
                    StringBuilder lineText = new StringBuilder();
                    float lineWidth = 0;

                    while (runIndex < runs.size) {
                        GlyphLayout.GlyphRun run = runs.get(runIndex);
                        for (BitmapFont.Glyph glyph : run.glyphs) {
                            lineText.append((char) glyph.id);
                        }
                        lineWidth += run.width;
                        runIndex++;
                        if (runIndex >= runs.size || runs.get(runIndex).x != run.x) {
                            break;
                        }
                    }

                    float lineX = textX + (overlay.w * 0.9f - lineWidth) / 2;
                    bodyFont.draw(game.batch, lineText.toString(), lineX, textY);
                    textY -= lineHeight;
                }

                textY -= lineHeight * 0.5f;
            }

            game.batch.end();
        }
    }
}
