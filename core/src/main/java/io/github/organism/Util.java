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

    public static Pair<Double, Double> xyToPoloar(Double x, Double y){
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double theta = Math.atan(y/x);
        return new Pair<>(r, theta);
    }
}
