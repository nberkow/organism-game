package io.github.organism;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.organism.map.MapVertex;

public class ExpandEdge {
    MapVertex source;
    MapVertex target;
    GameBoard gameBoard;
    Float percentProgress;
    Float circleRadius;

    public ExpandEdge(MapVertex s, MapVertex t) {
        source = s;
        target = t;
        percentProgress = 0f;
        gameBoard = target.pos.grid.gameBoard;
        circleRadius = 2f;
    }

    public void render(){

        float tX = (target.x * percentProgress) + (source.x * (1-percentProgress));
        float tY = (target.y * percentProgress) + (source.y * (1-percentProgress));

        target.pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        target.pos.grid.gameBoard.game.shapeRenderer.setColor(source.player.getColor());
        target.pos.grid.gameBoard.game.shapeRenderer.line(
            source.x * gameBoard.hexSideLen + gameBoard.centerX,
            source.y * gameBoard.hexSideLen + gameBoard.centerY,
            tX * gameBoard.hexSideLen + gameBoard.centerX,
            tY * gameBoard.hexSideLen + gameBoard.centerY
        );
        target.pos.grid.gameBoard.game.shapeRenderer.end();

        target.pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        target.pos.grid.gameBoard.game.shapeRenderer.circle(
            tX * gameBoard.hexSideLen + gameBoard.centerX,
            tY * gameBoard.hexSideLen + gameBoard.centerY,
             circleRadius
        );
        target.pos.grid.gameBoard.game.shapeRenderer.end();


    }

    public float getPlanchetteAgreement(FloatPair<Float> planchetteXY) {
        float a = planchetteXY.a + (target.x - source.x);
        float b = planchetteXY.b + (target.y - source.y);
        return (float) Math.pow(Math.pow(a, 2) + Math.pow(b, 2), 0.5);
    }
}
