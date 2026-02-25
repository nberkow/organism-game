package io.github.organism.learning;
import java.util.Random;

public class GeneticLayer {
    // Non-learnable mask that evolves, doesn't train
    private float[][] mask; // [outputSize][inputSize]
    private Random random;

    public GeneticLayer(int inputSize, int outputSize, Random random) {
        this.random = random;
        this.mask = createRandomMask(inputSize, outputSize);
    }

    private float[][] createRandomMask(int inputSize, int outputSize) {
        float[][] mask = new float[outputSize][inputSize];
        // Sparse random connections (like biological neural development)
        float sparsity = 0.3f; // 30% of connections exist

        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                if (random.nextFloat() < sparsity) {
                    // Random weight between -1 and 1
                    mask[i][j] = (random.nextFloat() * 2 - 1);
                } else {
                    mask[i][j] = 0; // No connection
                }
            }
        }
        return mask;
    }

    public float[] forward(float[] input) {
        float[] output = new float[mask.length];

        for (int i = 0; i < mask.length; i++) {
            float sum = 0;
            for (int j = 0; j < mask[0].length; j++) {
                sum += mask[i][j] * input[j];
            }
            output[i] = sum;
        }

        return output;
    }

    // Evolutionary operations
    public GeneticLayer mutate(float mutationRate) {
        GeneticLayer mutant = new GeneticLayer(mask[0].length, mask.length, random);

        // Copy then mutate
        for (int i = 0; i < mask.length; i++) {
            System.arraycopy(mask[i], 0, mutant.mask[i], 0, mask[0].length);
        }

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                if (random.nextFloat() < mutationRate) {
                    // Three types of mutation:
                    float choice = random.nextFloat();
                    if (choice < 0.33f) {
                        // Weight change
                        mutant.mask[i][j] += (random.nextFloat() * 2 - 1) * 0.2f;
                    } else if (choice < 0.66f && Math.abs(mutant.mask[i][j]) > 0.1f) {
                        // Connection removal (if not already near zero)
                        mutant.mask[i][j] = 0;
                    } else {
                        // New connection
                        if (Math.abs(mutant.mask[i][j]) < 0.1f) {
                            mutant.mask[i][j] = (random.nextFloat() * 2 - 1);
                        }
                    }
                }
            }
        }

        return mutant;
    }

    public GeneticLayer crossover(GeneticLayer other) {
        // Simple uniform crossover
        GeneticLayer child = new GeneticLayer(mask[0].length, mask.length, random);

        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                if (random.nextBoolean()) {
                    child.mask[i][j] = this.mask[i][j];
                } else {
                    child.mask[i][j] = other.mask[i][j];
                }
            }
        }

        return child;
    }

    // Save/load for evolution
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mask.length; i++) {
            for (int j = 0; j < mask[0].length; j++) {
                sb.append(String.format("%.4f", mask[i][j]));
                if (j < mask[0].length - 1) sb.append(",");
            }
            if (i < mask.length - 1) sb.append(";");
        }
        return sb.toString();
    }

    public static GeneticLayer deserialize(String data, Random random) {
        String[] rows = data.split(";");
        String[] firstRow = rows[0].split(",");

        GeneticLayer layer = new GeneticLayer(firstRow.length, rows.length, random);

        for (int i = 0; i < rows.length; i++) {
            String[] values = rows[i].split(",");
            for (int j = 0; j < values.length; j++) {
                layer.mask[i][j] = Float.parseFloat(values[j]);
            }
        }

        return layer;
    }
}
