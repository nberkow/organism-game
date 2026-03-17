package io.github.organism;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

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
        planchetteAgreement = Math.max(Float.MIN_VALUE, planchetteFromCenter.dot(vector));
        this.planchetteFromCenter = planchetteFromCenter;
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
        
        // Don't render if target has been claimed
        if (target.getPlayer() != null) {
            return;
        }

        // Use the agreement that was calculated during expand()
        // This ensures rendering matches the selection logic
        float agreement = Math.max(0.01f, planchetteAgreement);
        
        // Fixed total area for all candidate circles combined
        float TOTAL_AREA = 3000f;
        
        // Calculate this circle's area based on agreement
        // Use a simple linear relationship for now
        float thisArea = TOTAL_AREA * agreement / 6f; // Divide by ~6 for typical number of candidates
        
        // Convert area to radius: area = π * r²  =>  r = sqrt(area / π)
        float radius = (float) Math.sqrt(thisArea / Math.PI);
        
        // Clamp to reasonable min/max for visibility
        radius = Math.max(3f, Math.min(radius, 50f));
        
        // Persistence logic: keep showing circles for minimum time
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

        float targetX = (target.x * gameBoard.hexSideLen) + gameBoard.centerX;
        float targetY = (target.y * gameBoard.hexSideLen) + gameBoard.centerY;

        // Draw filled circle
        gameBoard.game.shapeRenderer.circle(targetX, targetY, radius, 16);
    }
}
