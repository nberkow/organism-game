package io.github.organism;

public class Util {

    public static Pair<Double, Double> polarToXY(Double r, Double theta) {
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return new Pair<>(x, y);
    }

    public static FloatPair<Float> polarToVisualXY(Double r, Double theta) {
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return new FloatPair<>(x, y);
    }

    public static DoublePair<Double> xyToPolarDouble(Double x, Double y){
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double theta = Math.atan2(y, x);
        return new DoublePair<>(r, theta);
    }

    public static FloatPair<Float> xyToPolarFloat(Float x, Float y){
        float r = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        float theta = (float) Math.atan2(y, x);
        return new FloatPair<>(r, theta);
    }
}
