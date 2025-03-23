package io.github.organism;

public class DoublePair<Double> {
    public Double r;
    public Double t;

    public DoublePair(Double x, Double y) {
        this.r = x;
        this.t = y;
    }

    public FloatPair<Float> toFloat(){
        return new FloatPair<>(
            (Float) this.r,
            (Float) this.t
        );
    }
}
