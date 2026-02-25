package io.github.organism;

import com.badlogic.gdx.math.Vector2;

import java.util.Arrays;

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

    public static Vector2 xyToPolarFloat(Vector2 v){
        float r = (float) Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
        float theta = (float) Math.atan2(v.y, v.x);
        return new Vector2(r, theta);
    }


    public static Vector2 polarToXYFloat(Vector2 polarCoord) {
        float x = (float) (polarCoord.x * Math.cos(polarCoord.y));
        float y = (float) (polarCoord.x * Math.sin(polarCoord.y));
        return new Vector2(x, y);
    }

    public static float sigmoid(float x) {
        return (float)(1.0 / (1.0 + Math.exp(-x)));
    }

    public static float angularDifference(float a, float b) {
        float diff = (a - b) % (2 * (float)Math.PI);
        if (diff > Math.PI) diff -= 2 * Math.PI;
        if (diff < -Math.PI) diff += 2 * Math.PI;
        return diff;
    }

    // Squared angular error
    public static float angularError(float a, float b) {
        float diff = Math.abs((a - b) % (2 * (float)Math.PI));
        if (diff > Math.PI) diff = 2 * (float)Math.PI - diff;
        return diff * diff;
    }

    // Normalize angle to [0, 2π)
    public static float normalizeAngle(float theta) {
        theta = theta % (2 * (float)Math.PI);
        if (theta < 0) theta += 2 * (float)Math.PI;
        return theta;
    }

    // Inverse sigmoid (logit): ln(x/(1-x))
    public float inverseSigmoid(float y) {
        return (float)Math.log(y / (1 - y));
    }

    // Tanh: (e^x - e^-x)/(e^x + e^-x)
    public float tanh(float x) {
        return (float)Math.tanh(x);
    }

    public static float relu(float x) {
        return x > 0 ? x : 0;
    }

    // Deep copy of matrix
    public float[][] copyMatrix(float[][] original) {
        float[][] copy = new float[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }

    // Deep copy of vector
    public float[] copyVector(float[] original) {
        return Arrays.copyOf(original, original.length);
    }

    // Element-wise add: result = a + b
    public void addVectors(float[] result, float[] a, float[] b) {
        for (int i = 0; i < result.length; i++) {
            result[i] = a[i] + b[i];
        }
    }

    public float[] networkForward(float[] state, float[][] W1, float[] b1,
                           float[][] W2, float[] b2) {
        int hiddenSize = W1.length;
        int stateSize = state.length;

        // Hidden layer: ReLU activation
        float[] hidden = new float[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            float sum = b1[i];
            for (int j = 0; j < stateSize; j++) {
                sum += W1[i][j] * state[j];
            }
            hidden[i] = sum > 0 ? sum : 0; // ReLU
        }

        // Output layer: linear
        float[] output = new float[2];
        for (int i = 0; i < 2; i++) {
            float sum = b2[i];
            for (int j = 0; j < hiddenSize; j++) {
                sum += W2[i][j] * hidden[j];
            }
            output[i] = sum;
        }

        // Apply output constraints
        float r = (float)(1.0 / (1.0 + Math.exp(-output[0]))); // sigmoid
        float theta = output[1] % (2 * (float)Math.PI);
        if (theta < 0) theta += 2 * (float)Math.PI;

        return new float[]{r, theta};
    }


}
