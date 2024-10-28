package io.github.organism;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class TriangularGrid implements Iterable<GridPosition> {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, GridPosition>>> grid;

    int size = 0;
    GameBoard game_board;

    TriangularGrid(GameBoard gb) {
        game_board = gb;
        grid = new HashMap<>();
    }

    public void add_pos(GridPosition p) {
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

    public void remove_hex(GridPosition p) {
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
}
