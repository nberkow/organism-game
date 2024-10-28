package io.github.organism;

public class GridPosition {

    public MapElement content;
    int i;
    int j;
    int k;

    TriangularGrid grid;

    public GridPosition(int i, int j, int k, TriangularGrid grid) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.grid = grid;
    }
}
