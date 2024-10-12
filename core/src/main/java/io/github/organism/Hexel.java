package io.github.organism;

import java.util.Locale;
import java.util.Objects;

public class Hexel implements Comparable<Hexel>{

    Integer i, j, k;

    Float x, y;
    Double resources;

    public String toString() {
        return String.format(Locale.US, "%d %d %d\t%f\t%f", i, j, k, x, y);
    }

    @Override
    public int compareTo(Hexel h) {
        return resources.compareTo(h.resources);
    }
}




