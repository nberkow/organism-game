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
        vector.set(target.x - centroid.x, target.y - centroid.y);
        // Normalize the vector for direction calculation
        if (vector.len() > 0.001f) {
            vector.nor();
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
     * The totalProbability parameter ensures all circles sum to a fixed total area.
     */
    public void render(float probability, float totalProbability) {
        if (gameBoard == null || totalProbability <= 0) {
            return;
        }

        // Fixed total area for all candidate circles combined
        float TOTAL_AREA = 3000f;
        
        // Calculate this circle's area as a fraction of total
        float thisArea = TOTAL_AREA * (probability / totalProbability);
        
        // Convert area to radius: area = π * r²  =>  r = sqrt(area / π)
        float radius = (float) Math.sqrt(thisArea / Math.PI);
        
        // Clamp to reasonable min/max for visibility
        // Minimum of 3f ensures all candidates are visible
        radius = Math.max(3f, Math.min(radius, 50f));

        float targetX = (target.x * gameBoard.hexSideLen) + gameBoard.centerX;
        float targetY = (target.y * gameBoard.hexSideLen) + gameBoard.centerY;

        // Draw filled circle
        gameBoard.game.shapeRenderer.circle(targetX, targetY, radius, 16);
    }
}
