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
        planchetteAgreement = planchetteFromCenter.dot(vector);
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

        //FIXME randomness turned off
        float scale = 0; //(float) ((this.planchetteAgreement + candidateVertex.planchetteAgreement)/2);

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

    public void render() {

        /*
        target.pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        target.pos.grid.gameBoard.game.shapeRenderer.setColor(Color.MAGENTA);
        target.pos.grid.gameBoard.game.shapeRenderer.line(
            (target.x * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerX,
            (target.y * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerY,
            (target.x * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerX + planchetteAgreement.x,
            (target.y * target.pos.grid.gameBoard.hexSideLen) + target.pos.grid.gameBoard.centerY + planchetteAgreement.y
        );


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

        target.pos.grid.gameBoard.game.shapeRenderer.end();*/
    }
}
