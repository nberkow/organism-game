package io.github.organism;

import com.badlogic.gdx.utils.Null;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public class HexSet implements Iterable<Hexel> {
    HashMap<Integer, HashMap<Integer, HashMap<Integer, Hexel>>> hex_grid;

    HexSet(){
        hex_grid = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Hexel>>>();
    }

    public void add_hex(Hexel h){
        if (!hex_grid.containsKey(h.i)){
            hex_grid.put(h.i, new HashMap<>());
        }

        if (!hex_grid.get(h.i).containsKey(h.j)){
            hex_grid.get(h.i).put(h.j, new HashMap<>());
        }

        hex_grid.get(h.i).get(h.j).put(h.k, h);
    }

    public Hexel get_hex(int i, int j, int k){
        return hex_grid.get(i).get(j).get(k);
    }

    public ArrayList<Hexel> dump_hex_list(){

        ArrayList<Hexel> hex_list = new ArrayList<>();

        for (int i : hex_grid.keySet()){
            for (int j : hex_grid.get(i).keySet()){
                for (int k : hex_grid.get(i).get(j).keySet()){
                    hex_list.add(hex_grid.get(i).get(j).get(k));
                }
            }
        }
        return hex_list;
    }

    public void remove_hex(Hexel h) {
        hex_grid.get(h.i).get(h.j).remove(h.k);
    }

    public boolean contains_hex_at(int i, int j, int k){

        /*
        Check if a hex exists at the coordinates and is not null
         */

        if (!hex_grid.containsKey(i)){
            return false;
        }

        if (!hex_grid.get(i).containsKey(j)){
            return false;
        }

        if (!hex_grid.get(i).get(j).containsKey(k)){
            return false;
        }

        return hex_grid.get(i).get(j).get(k) != null;

    }

    public boolean contains_hex(Hexel h){
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
                // Check if kIterator has more elements or if we need to move to the next j or i level.
                if (kIterator != null && kIterator.hasNext()) {
                    return true;
                }
                if (jIterator != null && jIterator.hasNext()) {
                    return true;
                }
                return iIterator.hasNext();  // Check if i level has more elements.
            }

            @Override
            public Hexel next() {
                // Move through the i, j, and k levels.
                if (kIterator == null || !kIterator.hasNext()) {
                    if (jIterator == null || !jIterator.hasNext()) {
                        // Move to the next i level.
                        if (!iIterator.hasNext()) {
                            throw new java.util.NoSuchElementException();
                        }
                        currentI = iIterator.next();
                        jIterator = hex_grid.get(currentI).keySet().iterator();
                    }
                    // Move to the next j level.
                    currentJ = jIterator.next();
                    kIterator = hex_grid.get(currentI).get(currentJ).keySet().iterator();
                }

                // Move to the next k level and return the corresponding Hexel.
                Integer currentK = kIterator.next();
                return hex_grid.get(currentI).get(currentJ).get(currentK);
            }
        };
    }
}
