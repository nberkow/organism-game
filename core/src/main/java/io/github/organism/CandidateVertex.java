package io.github.organism;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import io.github.organism.hud.DebugLogger;
import io.github.organism.map.MapVertex;

public class CandidateVertex implements Comparable<CandidateVertex>{

    MapVertex source;
    MapVertex target;
    Vector2 vector;
    GameBoard gameBoard;
    Float blinkCircleRadius;
    public float planchetteAgreement;
    public Vector2 planchetteFromCenter;
    private float lastRenderedRadius = 0f;
    private float renderPersistenceTimer = 0f;
    private static final float MIN_RENDER_TIME = 0.3f; // Minimum time to show a circle


    public CandidateVertex(MapVertex s, MapVertex t) {
        source = s;
        target = t;
        // Vector will be calculated from centroid, not from source
        vector = new Vector2(0, 0);
        gameBoard = target.pos.grid.gameBoard;
        blinkCircleRadius = OrganismGame.VIRTUAL_WIDTH * 0.005f;

        // Initialize with minimum render time so circles appear immediately
        renderPersistenceTimer = MIN_RENDER_TIME * 2; // Longer initial time
        lastRenderedRadius = 15f; // Start with a more visible radius
    }

    /**
     * Update the vector from the organism's centroid to this target vertex.
     * Should be called each round to reflect the current territory shape.
     */
    public void updateVectorFromCentroid(Vector2 centroid) {
        // Calculate direction from centroid to target
        float dx = target.x - centroid.x;
        float dy = target.y - centroid.y;
        vector.set(dx, dy);

        // Normalize the vector for direction calculation
        float len = vector.len();
        if (len > 0.001f) {
            vector.nor();
        } else {
            // If at centroid, use a default direction
            vector.set(1, 0);
        }
    }

    public void calculatePlanchetteAgreement(Vector2 planchetteFromCenter) {
        // Calculate dot product: positive = same direction, negative = opposite
        float rawAgreement = planchetteFromCenter.dot(vector);

        // Store the raw agreement (can be negative)
        planchetteAgreement = rawAgreement;
        this.planchetteFromCenter = planchetteFromCenter;

        // Debug output for first few turns
        if (gameBoard != null && gameBoard.game.arcadeLoop != null &&
            gameBoard.game.arcadeLoop.currentIteration <= 2) {
            DebugLogger.getInstance().logf(
                "    CandidateVertex.calculatePlanchetteAgreement: pos=(%.1f,%.1f), " +
                "vector=(%.3f,%.3f), planchette=(%.3f,%.3f), agreement=%.3f",
                target.x, target.y, vector.x, vector.y,
                planchetteFromCenter.x, planchetteFromCenter.y, rawAgreement
            );
        }
    }

    public double getPlanchetteAgreement() {
        return planchetteAgreement;
    }


    /**
     * @param candidateVertex
     * @returns
     */
    @Override
    public int compareTo(CandidateVertex candidateVertex) {
        /*
        This powers a stochastic sort by planchetteAgreement (higher = more concordant).
        Using this comparison operator give list that is roughly sorted, but with some randomness.
         */

        //FIXME randomness turned off (does this need randomness or is it shuffled elsewhere?)
        float scale =  (float) ((this.planchetteAgreement + candidateVertex.planchetteAgreement)/2);

        float r1 = com.badlogic.gdx.math.MathUtils.random() * scale;
        float r2 = com.badlogic.gdx.math.MathUtils.random() * scale;

        if (this.planchetteAgreement - candidateVertex.planchetteAgreement > r1) {
            return -1;
        }

        if (candidateVertex.planchetteAgreement - this.planchetteAgreement > r2 ){
            return 1;
        }

        return 0;

    }

    /**
     * Render this candidate vertex with a circle whose area is proportional to its probability.
     * Uses the pre-calculated planchette agreement from the expand() method.
     */
    public void render(Vector2 currentPlanchette) {
        if (gameBoard == null) {
            return;
        }

        // ALLOW rendering even if claimed - this is for animation!
        // The vertex may have been claimed but we still want to show the circle
        boolean wasClaimed = (target.getPlayer() != null);

        // Use the agreement that was calculated during expand()
        // DO NOT recalculate - use the stored value from decision time
        float agreement = planchetteAgreement;

        // Don't render circles for vertices with very low agreement
        // These are essentially random/unbiased and shouldn't show visual feedback
        if (agreement < 0.05f && !wasClaimed) {
            return; // Skip rendering for low-agreement unclaimed vertices
        }

        // Normalize agreement to 0-1 range for radius calculation
        // Negative agreements become 0, positive scale up
        float normalizedAgreement = Math.max(0f, agreement);

        // Base radius on normalized agreement
        // Higher agreement = larger circle
        float minRadius = gameBoard.hexSideLen / 10;
        float maxRadius = gameBoard.hexSideLen;
        float radius = minRadius + (maxRadius - minRadius) * normalizedAgreement;

        // If vertex was claimed, show it briefly then fade
        if (wasClaimed) {
            // Decay the persistence timer faster for claimed vertices
            renderPersistenceTimer -= 0.05f;
            if (renderPersistenceTimer <= 0) {
                return; // Stop rendering claimed vertices after timer expires
            }
            // Use a bright radius for claimed vertices
            radius = Math.max(radius, 15f);
        } else {
            // Persistence logic for unclaimed: keep showing circles for minimum time
            if (radius > lastRenderedRadius * 0.9f) {
                // Radius increased or stayed similar - reset timer
                lastRenderedRadius = radius;
                renderPersistenceTimer = MIN_RENDER_TIME;
            } else if (renderPersistenceTimer > 0) {
                // Use previous radius while timer is active
                radius = lastRenderedRadius;
                renderPersistenceTimer -= 0.016f; // Approximate frame time
            } else {
                // Timer expired, use new smaller radius
                lastRenderedRadius = radius;
            }
        }

        // Debug output for first few turns - only log if we're actually rendering
        if (gameBoard.game.arcadeLoop != null && gameBoard.game.arcadeLoop.currentIteration <= 2) {
            DebugLogger.getInstance().logf(
                "  CandidateVertex render: agreement=%.3f, normalized=%.3f, radius=%.1f, claimed=%b, pos=(%.1f,%.1f)",
                agreement, normalizedAgreement, radius, wasClaimed, target.x, target.y
            );
        }

        float targetX = (target.x * gameBoard.hexSideLen) + gameBoard.centerX;
        float targetY = (target.y * gameBoard.hexSideLen) + gameBoard.centerY;

        // Draw filled circle
        gameBoard.game.shapeRenderer.circle(targetX, targetY, radius, 16);
    }
}
