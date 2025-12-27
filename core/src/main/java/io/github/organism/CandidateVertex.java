package io.github.organism;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

import io.github.organism.map.MapVertex;

public class CandidateVertex implements Comparable<CandidateVertex>{

    MapVertex source;
    MapVertex target;
    Vector2 vector;
    GameBoard gameBoard;
    Float blinkCircleRadius;
    public double planchetteAgreement;

    public Vector2 planchetteFromCenter;


    public CandidateVertex(MapVertex s, MapVertex t) {
        source = s;
        target = t;
        vector = new Vector2(target.x - source.x, target.y - source.y);
        gameBoard = target.pos.grid.gameBoard;
        blinkCircleRadius = OrganismGame.VIRTUAL_WIDTH * 0.005f;
    }

    public void calculatePlanchetteAgreement(Vector2 planchetteFromCenter) {
        planchetteAgreement = Math.abs(planchetteFromCenter.angleRad(vector));
        this.planchetteFromCenter = planchetteFromCenter;
    }

    public double getPlanchetteAgreement() {
        return planchetteAgreement;
    }

    /**
     * @param candidateVertex
     * @return
     */
    @Override
    public int compareTo(CandidateVertex candidateVertex) {
        /*
        This powers a stochastic sort by planchetteAgreement (lower = more concordant).
        Using this comparison operator give list that is roughly sorted, but with some randomness.
         */

        if (this.planchetteAgreement == candidateVertex.planchetteAgreement){
            return 0;
        }

        float r = com.badlogic.gdx.math.MathUtils.random();
        if (this.planchetteAgreement - candidateVertex.planchetteAgreement > r){
            return 1;
        } else {
            return -1;
        }
    }

    public void render() {

        target.pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        target.pos.grid.gameBoard.game.shapeRenderer.circle(
            target.x * target.pos.grid.gameBoard.hexSideLen + target.pos.grid.gameBoard.centerX,
            target.y * target.pos.grid.gameBoard.hexSideLen + target.pos.grid.gameBoard.centerY,
            (float) (blinkCircleRadius));

        target.pos.grid.gameBoard.game.shapeRenderer.setColor(Color.CYAN);
        target.pos.grid.gameBoard.game.shapeRenderer.line(
            (source.x * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerX,
            (source.y * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerY,
            (target.x * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerX,
            (target.y * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerY
        );

        target.pos.grid.gameBoard.game.shapeRenderer.setColor(Color.GREEN);
        target.pos.grid.gameBoard.game.shapeRenderer.line(
            (source.x * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerX,
            (source.y * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerY,
            (source.x * target.pos.grid.gameBoard.hexSideLen) + planchetteFromCenter.x + target.pos.grid.gameBoard.centerX,
            (source.y * target.pos.grid.gameBoard.hexSideLen) + planchetteFromCenter.y + target.pos.grid.gameBoard.centerY
        );

        target.pos.grid.gameBoard.game.shapeRenderer.end();
    }
}
