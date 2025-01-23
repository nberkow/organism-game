package io.github.organism;
import static java.lang.System.exit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TriangularGrid implements Iterable<GridPosition> {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, GridPosition>>> grid;

    int size = 0;
    GameBoard game_board;

    TriangularGrid(GameBoard gb) {
        game_board = gb;
        grid = new HashMap<>();
    }

    public void add_pos(GridPosition p) {

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

    public GridPosition get_pos(int i, int j, int k) {

        if (this.contains_position(i, j, k)) {
            return grid.get(i).get(j).get(k);
        }
        return null;
    }

    public void mask_pos(int i, int j, int k) {
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
        for (MapHex h : v1.adjacent_hexes) {
            if (v2.adjacent_hexes.contains(h) && !h.masked) {
                shared_hexes.add(h);
            }
        }
        return shared_hexes;
    }
    public ArrayList<MapVertex> get_external_vertex_layer(Player player){
        HashSet<MapVertex> unique_vertices = new HashSet<>();

        for (GridPosition pos : this) {
            if (!(pos.content instanceof MapVertex)){
                throw new RuntimeException("get_external_vertex_layer() can only be used on vertex grids");
            }
            MapVertex vertex = (MapVertex) pos.content;
            for (MapVertex neighbor : vertex.adjacent_vertices){
                if (neighbor.player != player && !neighbor.masked && !get_shared_hexes(vertex, neighbor).isEmpty()){
                    unique_vertices.add(neighbor);
                }
            }
        }
        return new ArrayList<>(unique_vertices);
    }

    public ArrayList<MapHex> get_external_hex_layer(Player player){
        HashSet<MapHex> unique_hexes = new HashSet<>();

        for (GridPosition pos : this) {
            if (!(pos.content instanceof MapHex)){
                throw new RuntimeException("get_external_hex_layer() can only be used on hex grids");
            }
            MapHex hex = (MapHex) pos.content;
            for (MapVertex vertex : hex.vertex_list){
                for (MapHex neighbor : vertex.adjacent_hexes){
                    if (neighbor.player != player && !neighbor.masked){
                        unique_hexes.add(neighbor);
                    }
                }
            }
        }
        return new ArrayList<>(unique_hexes);
    }

    public int get_unmasked_vertices(){
        int c = 0;
        for (GridPosition pos : this) {
            if (!pos.content.get_masked()) {
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
        game_board = null;
    }
}
