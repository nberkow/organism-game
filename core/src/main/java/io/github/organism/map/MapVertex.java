package io.github.organism.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.github.organism.ExpandEdge;
import io.github.organism.FloatPair;
import io.github.organism.OrganismGame;
import io.github.organism.player.Player;

public class MapVertex implements MapElement{
    public GridPosition pos;

    public float x;
    public float y;
    public HashSet<MapHex> adjacentHexes;
    public Set<MapVertex> adjacentVertices;

    public float baseVectorLength;

    public Player player;
    public boolean masked = false;

    public ArrayList<ExpandEdge> expandEdges;
    public MapVertex(GridPosition p) {
        pos = p;
        x = (float) ((pos.j * Math.pow(3f, 0.5f) / 2f) - (pos.k * Math.pow(3f, 0.5f) / 2f));
        y = pos.i - pos.j / 2f - pos.k / 2f;

        adjacentHexes = new HashSet<>();
        adjacentVertices = new HashSet<>();

        baseVectorLength = OrganismGame.VIRTUAL_WIDTH * 0.05f;
        expandEdges = new ArrayList<>();

    }



    @Override
    public Player getPlayer() {
        return player;
    }



    /**
     *
     */
    @Override
    public void render(){

    }

    @Override
    public boolean getMasked() {
        return masked;
    }
}
