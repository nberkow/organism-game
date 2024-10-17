package io.github.organism;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Hexel implements Comparable<Hexel>{

    Integer i, j, k;

    Float x, y;
    Double resources;

    HashMap<String, Double> assimilation_by_player = new HashMap<String, Double>();

    Double unassimilated = 1d;

    public String toString() {
        return String.format(Locale.US, "%d %d %d\t%f\t%f", i, j, k, x, y);
    }

    public void update_assimilation(String player_name, double delta){
        assimilation_by_player.merge(player_name, delta, Double::sum);
        unassimilated -= delta;
    }

    @Override
    public int compareTo(Hexel h) {
        return unassimilated.compareTo(h.unassimilated);
    }
}




