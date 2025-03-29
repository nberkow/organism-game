package io.github.organism.map;

public class GridPosition {

    public MapElement content;
    public int i;
    public int j;
    public int k;

    public TriangularGrid grid;

    public GridPosition(int i, int j, int k, TriangularGrid grid) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.grid = grid;
    }

}
