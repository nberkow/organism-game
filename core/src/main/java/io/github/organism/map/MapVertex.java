package io.github.organism.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.github.organism.FloatPair;
import io.github.organism.OrganismGame;
import io.github.organism.player.Player;

public class MapVertex implements MapElement{
    public GridPosition pos;

    public float x;
    public float y;
    public HashSet<MapHex> adjacentHexes;
    public Set<MapVertex> adjacentVertices;

    public HashMap<String, FloatPair<Float>> vectors;
    public HashMap<String, FloatPair<Float>> vectorEndpoints;

    public float circleRadius = 5;
    public Color circleColor;
    public boolean showCircle = false;

    public float baseVectorLength;

    public Player player;
    public boolean masked = false;
    public boolean showVectors = false;


    public MapVertex(GridPosition p) {
        pos = p;
        adjacentHexes = new HashSet<>();
        adjacentVertices = new HashSet<>();

        x = (float) ((pos.j * Math.pow(3f, 0.5f) / 2f) - (pos.k * Math.pow(3f, 0.5f) / 2f));
        y = pos.i - pos.j / 2f - pos.k / 2f;

        vectors = new HashMap<>();
        vectorEndpoints = new HashMap<>();
        baseVectorLength = OrganismGame.VIRTUAL_WIDTH * 0.05f;

        circleColor = Color.GREEN;
    }



    @Override
    public Player getPlayer() {
        return player;
    }


    public void calculateVectorEndpointsXY() {
        for (String l : vectors.keySet()) {
            FloatPair<Float> vector = vectors.get(l);
            // Just store the endpoint coordinates directly
            vectorEndpoints.put(l, new FloatPair<>(
                x + vector.x,  // x component is already scaled
                y + vector.y   // y component is already scaled
            ));
        }
    }

    /**
     *
     */
    @Override
    public void render(){
        pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        pos.grid.gameBoard.game.shapeRenderer.setColor(Color.RED);
        calculateVectorEndpointsXY();

        if (showVectors) {
            for (String l : vectors.keySet()) {
                FloatPair<Float> endPoint = vectorEndpoints.get(l);
                pos.grid.gameBoard.game.shapeRenderer.line(
                    x * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                    y * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY,
                    endPoint.x * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                    endPoint.y * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY
                );
            }
        }

        pos.grid.gameBoard.game.shapeRenderer.setColor(circleColor);
        if (showCircle) {
            pos.grid.gameBoard.game.shapeRenderer.circle(
                x * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                y * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY,
                circleRadius
            );
        }

        pos.grid.gameBoard.game.shapeRenderer.end();

    }

    public void addVector(String label, FloatPair<Float> vector){
        vectors.put(label, vector);
    }

    public void calculateDirectionVectors(Player p) {

        float sumX = 0;
        float sumY = 0;

        for (MapVertex neighbor : adjacentVertices) {

            if (neighbor.player != player) {
                float x1 = (float) ((neighbor.pos.j * Math.sqrt(3) / 2f) - (neighbor.pos.k * Math.sqrt(3) / 2f));
                float y1 = neighbor.pos.i - neighbor.pos.j / 2f - neighbor.pos.k / 2f;

                float dx = x1 - x;
                float dy = y1 - y;

                float length = (float) Math.sqrt(dx * dx + dy * dy);
                if (length > 0) {
                    sumX += (dx / length);
                    sumY += (dy / length);
                }
            }
        }

        vectors.put("base_direction", new FloatPair<>(sumX, sumY));
    }

    public void clearVectors(){
        vectors = new HashMap<>();
    }

    @Override
    public boolean getMasked() {
        return masked;
    }

    public ArrayList<MapHex> get_opponent_hexes(Player player) {
        ArrayList<MapHex> opponent_hexes = new ArrayList<>();
        for (MapHex hex : adjacentHexes){
            if (hex.player != null && hex.player != player){
                opponent_hexes.add(hex);
            }
        }
        return opponent_hexes;
    }

}
