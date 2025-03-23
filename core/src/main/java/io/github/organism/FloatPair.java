package io.github.organism;

public class FloatPair<Float> {
    public float x;
    public float y;

    public FloatPair(Float x, Float y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public FloatPair(Double x, Double y) {
        this.x = x.floatValue();
        this.y = y.floatValue();
    }
}

