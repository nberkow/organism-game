package io.github.organism;

import com.badlogic.gdx.utils.Null;

import java.util.ArrayList;
import java.util.HashMap;

public class HexSet{
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
}
