package io.github.organism.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import io.github.organism.GameBoard;
import io.github.organism.player.Player;

public class TriangularGrid implements Iterable<GridPosition> {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, GridPosition>>> grid;

    int size = 0;
    GameBoard gameBoard;

    public TriangularGrid(GameBoard gb) {
        gameBoard = gb;
        grid = new HashMap<>();
    }

    public void addPos(GridPosition p) {

        if (grid.containsKey(p.i) && grid.get(p.i).containsKey(p.j) &&  grid.get(p.i).get(p.j).containsKey(p.k)){
            throw new RuntimeException("grid location " + p.i + " " + p.j + " " + p.k + " is not vacant!");
        }

        if (!grid.containsKey(p.i)) {
            grid.put(p.i, new HashMap<>());
        }

        if (!grid.get(p.i).containsKey(p.j)) {
            grid.get(p.i).put(p.j, new HashMap<>());
        }

        grid.get(p.i).get(p.j).put(p.k, p);
        size += 1;
    }

    public GridPosition getPos(int i, int j, int k) {

        if (this.contains_position(i, j, k)) {
            return grid.get(i).get(j).get(k);
        }
        return null;
    }

    public void maskPos(int i, int j, int k) {
        MapElement p = grid.get(i).get(j).get(k).content;
        if (p instanceof MapHex) {
            ((MapHex) p).masked = true;
        }
        if (p instanceof MapVertex) {
            ((MapVertex) p).masked = true;
        }
    }

    public void remove_pos(GridPosition p) {
        grid.get(p.i).get(p.j).remove(p.k);
        size -= 1;
    }

    public boolean contains_position(int i, int j, int k) {

        /*
        Check if a hex exists at the coordinates and is not null
         */

        if (!grid.containsKey(i)) {
            return false;
        }

        if (!grid.get(i).containsKey(j)) {
            return false;
        }

        if (!grid.get(i).get(j).containsKey(k)) {
            return false;
        }

        return grid.get(i).get(j).get(k) != null;
    }

    public boolean contains_hex(GridPosition p) {
        return contains_position(p.i, p.j, p.k);
    }

    public HashSet<MapHex> get_shared_hexes(MapVertex v1, MapVertex v2){
        HashSet<MapHex> shared_hexes = new HashSet<>();
        for (MapHex h : v1.adjacentHexes) {
            if (v2.adjacentHexes.contains(h) && !h.masked) {
                shared_hexes.add(h);
            }
        }
        return shared_hexes;
    }
    public ArrayList<MapVertex> getExternalVertexLayer(Player player){
        HashSet<MapVertex> unique_vertices = new HashSet<>();

        for (GridPosition pos : this) {
            if (!(pos.content instanceof MapVertex)){
                throw new RuntimeException("getExternalVertexLayer() can only be used on vertex grids");
            }
            MapVertex vertex = (MapVertex) pos.content;
            for (MapVertex neighbor : vertex.adjacentVertices){
                if (neighbor.player != player && !neighbor.masked && !get_shared_hexes(vertex, neighbor).isEmpty()){
                    unique_vertices.add(neighbor);
                }
            }
        }
        return new ArrayList<>(unique_vertices);
    }

    public ArrayList<MapHex> getExternalHexLayer(Player player){
        HashSet<MapHex> unique_hexes = new HashSet<>();

        for (GridPosition pos : this) {
            if (!(pos.content instanceof MapHex)){
                throw new RuntimeException("get_external_hex_layer() can only be used on hex grids");
            }
            MapHex hex = (MapHex) pos.content;
            for (MapVertex vertex : hex.vertexList){
                for (MapHex neighbor : vertex.adjacentHexes){
                    if (neighbor.player != player && !neighbor.masked){
                        unique_hexes.add(neighbor);
                    }
                }
            }
        }
        return new ArrayList<>(unique_hexes);
    }

    public int getUnmaskedVertices(){
        int c = 0;
        for (GridPosition pos : this) {
            if (!pos.content.getMasked()) {
                c++;
            }
        }
        return c;
    }

    @Override
    public Iterator<GridPosition> iterator() {
        return new Iterator<GridPosition>() {
            private final Iterator<Integer> iIterator = grid.keySet().iterator();
            private Iterator<Integer> jIterator = null;
            private Iterator<Integer> kIterator = null;

            private Integer currentI = null;
            private Integer currentJ = null;

            @Override
            public boolean hasNext() {
                // Ensure that we move through all levels, i, j, and k
                while ((kIterator == null || !kIterator.hasNext()) &&
                    (jIterator == null || !jIterator.hasNext())) {
                    if (!iIterator.hasNext()) {
                        return false;
                    }
                    currentI = iIterator.next();
                    jIterator = grid.get(currentI).keySet().iterator();
                }

                // If we're here, kIterator might need to be re-initialized
                while (kIterator == null || !kIterator.hasNext()) {
                    if (!jIterator.hasNext()) {
                        // Move to the next i level if needed
                        if (!iIterator.hasNext()) {
                            return false;
                        }
                        currentI = iIterator.next();
                        jIterator = grid.get(currentI).keySet().iterator();
                    }
                    currentJ = jIterator.next();
                    kIterator = grid.get(currentI).get(currentJ).keySet().iterator();
                }

                return kIterator.hasNext(); // Ensure kIterator has more elements.
            }

            @Override
            public GridPosition next() {
                // Ensure `hasNext` is true before moving forward
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }

                // Now safely fetch the next element from the kIterator
                Integer currentK = kIterator.next();
                return grid.get(currentI).get(currentJ).get(currentK);
            }
        };
    }

    public void dispose() {
        grid.clear();
        gameBoard = null;
    }

    public int countResources() {
        int res = 0;
        for (GridPosition pos : this) {
            if (pos.content instanceof MapHex) {
                MapHex h = (MapHex) pos.content;
                res += h.totalResources;
            }
        }
        return res;
    }
}
