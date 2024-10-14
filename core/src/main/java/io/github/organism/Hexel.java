package io.github.organism;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Hexel implements Comparable<Hexel>{

    Integer i, j, k;

    Float x, y;
    Double resources;

    HashMap<String, Double> assimilation_by_player = new HashMap<>();

    public String toString() {
        return String.format(Locale.US, "%d %d %d\t%f\t%f", i, j, k, x, y);
    }

    @Override
    public int compareTo(Hexel h) {
        return resources.compareTo(h.resources);
    }
}




