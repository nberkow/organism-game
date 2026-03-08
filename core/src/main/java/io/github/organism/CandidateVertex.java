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
        vector = new Vector2(target.x - source.x, target.y - source.y);
        gameBoard = target.pos.grid.gameBoard;
        blinkCircleRadius = OrganismGame.VIRTUAL_WIDTH * 0.005f;
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
        float scale =  0; //(float) ((this.planchetteAgreement + candidateVertex.planchetteAgreement)/2);

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

    public void render(float probability) {
        if (gameBoard == null) {
            return;
        }

        // Draw circle at target vertex with radius based on probability
        // Normalize probability to reasonable radius range
        float normalizedProb = Math.max(0.1f, Math.min(1.0f, probability));
        float radius = blinkCircleRadius * (0.5f + normalizedProb * 1.5f);

        float targetX = (target.x * gameBoard.hexSideLen) + gameBoard.centerX;
        float targetY = (target.y * gameBoard.hexSideLen) + gameBoard.centerY;

        // Draw filled circle
        gameBoard.game.shapeRenderer.circle(targetX, targetY, radius, 16);
    }
}
