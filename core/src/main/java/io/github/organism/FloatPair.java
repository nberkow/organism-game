package io.github.organism;

public class FloatPair<Float> {
    public float a;
    public float b;

    public FloatPair(Float x, Float y) {
        this.a = (float) x;
        this.b = (float) y;
    }

    public FloatPair(Double x, Double y) {
        this.a = x.floatValue();
        this.b = y.floatValue();
    }
}

