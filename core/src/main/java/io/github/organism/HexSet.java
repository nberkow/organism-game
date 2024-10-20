package io.github.organism;

import com.badlogic.gdx.utils.Null;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public class HexSet implements Iterable<Hexel> {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, Hexel>>> hex_grid;

    int size = 0;

    HexSet() {
        hex_grid = new HashMap<>();
    }

    public void add_hex(Hexel h) {
        if (!hex_grid.containsKey(h.i)) {
            hex_grid.put(h.i, new HashMap<>());
        }

        if (!hex_grid.get(h.i).containsKey(h.j)) {
            hex_grid.get(h.i).put(h.j, new HashMap<>());
        }

        hex_grid.get(h.i).get(h.j).put(h.k, h);
        size += 1;
    }

    public Hexel get_hex(int i, int j, int k) {

        if (this.contains_hex_at(i, j, k)) {
            return hex_grid.get(i).get(j).get(k);
        }
        return null;
    }

    public LinkedList<Hexel> dump_hex_list() {

        LinkedList<Hexel> hex_list = new LinkedList<>();

        for (int i : hex_grid.keySet()) {
            for (int j : hex_grid.get(i).keySet()) {
                for (int k : hex_grid.get(i).get(j).keySet()) {
                    hex_list.add(hex_grid.get(i).get(j).get(k));
                }
            }
        }
        return hex_list;
    }

    public void remove_hex(Hexel h) {
        hex_grid.get(h.i).get(h.j).remove(h.k);
        size -= 1;
    }

    public boolean contains_hex_at(int i, int j, int k) {

        /*
        Check if a hex exists at the coordinates and is not null
         */

        if (!hex_grid.containsKey(i)) {
            return false;
        }

        if (!hex_grid.get(i).containsKey(j)) {
            return false;
        }

        if (!hex_grid.get(i).get(j).containsKey(k)) {
            return false;
        }

        return hex_grid.get(i).get(j).get(k) != null;
    }

    public boolean contains_hex(Hexel h) {
        return contains_hex_at(h.i, h.j, h.k);
    }

    @Override
    public Iterator<Hexel> iterator() {
        return new Iterator<Hexel>() {
            private final Iterator<Integer> iIterator = hex_grid.keySet().iterator();
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
                    jIterator = hex_grid.get(currentI).keySet().iterator();
                }

                // If we're here, kIterator might need to be re-initialized
                while (kIterator == null || !kIterator.hasNext()) {
                    if (!jIterator.hasNext()) {
                        // Move to the next i level if needed
                        if (!iIterator.hasNext()) {
                            return false;
                        }
                        currentI = iIterator.next();
                        jIterator = hex_grid.get(currentI).keySet().iterator();
                    }
                    currentJ = jIterator.next();
                    kIterator = hex_grid.get(currentI).get(currentJ).keySet().iterator();
                }

                return kIterator.hasNext(); // Ensure kIterator has more elements.
            }

            @Override
            public Hexel next() {
                // Ensure `hasNext` is true before moving forward
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }

                // Now safely fetch the next element from the kIterator
                Integer currentK = kIterator.next();
                return hex_grid.get(currentI).get(currentJ).get(currentK);
            }
        };
    }
}
