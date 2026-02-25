package io.github.organism.learning;
import io.github.organism.GameBoard;
import io.github.organism.Util;
import com.badlogic.gdx.math.Vector2;

import java.util.*;
import java.io.*;

import io.github.organism.hud.PlayerHud;
import io.github.organism.map.GridPosition;
import io.github.organism.map.MapVertex;
import io.github.organism.map.TriangularGrid;
import io.github.organism.player.Player;

public class SlimeRLAgent {

    Player player;


    // ========== CORE RL COMPONENTS ==========

    // Neural Network for continuous action output (r, θ)
    private static class PolicyNetwork {
        float[][] W1, W2;
        float[] b1, b2;
        int stateSize, hiddenSize;

        PolicyNetwork(int stateSize, int hiddenSize) {
            this.stateSize = stateSize;
            this.hiddenSize = hiddenSize;

            // Xavier initialization
            W1 = randomMatrix(hiddenSize, stateSize,
                (float)Math.sqrt(2.0 / (stateSize + hiddenSize)));
            b1 = randomVector(hiddenSize, 0.01f);

            W2 = randomMatrix(2, hiddenSize,
                (float)Math.sqrt(2.0 / (hiddenSize + 2)));
            b2 = randomVector(2, 0.01f);
        }

        // Forward pass: state → (mean_r, mean_θ)
        public float[] forward(float[] state) {
            // Hidden layer with ReLU
            float[] hidden = new float[hiddenSize];
            for (int i = 0; i < hiddenSize; i++) {
                float sum = b1[i];
                for (int j = 0; j < stateSize; j++) {
                    sum += W1[i][j] * state[j];
                }
                hidden[i] = Math.max(0, sum); // ReLU
            }

            // Output layer (no activation yet)
            float[] output = new float[2];
            for (int i = 0; i < 2; i++) {
                float sum = b2[i];
                for (int j = 0; j < hiddenSize; j++) {
                    sum += W2[i][j] * hidden[j];
                }
                output[i] = sum;
            }

            return output;
        }

        // Sample action with exploration noise
        public float[] sampleAction(float[] state, Random rand, float explorationNoise) {
            float[] mean = forward(state);

            // Add Gaussian noise for exploration
            float r = mean[0] + (float)rand.nextGaussian() * explorationNoise;
            float theta = mean[1] + (float)rand.nextGaussian() * explorationNoise * 2;

            // Apply constraints
            r = sigmoid(r);                         // r ∈ [0,1]
            theta = (float)(theta % (2 * Math.PI)); // θ ∈ [0,2π]

            return new float[]{r, theta};
        }

        // Compute log probability of action given state
        public float logProbability(float[] state, float[] action) {
            float[] mean = forward(state);

            // Inverse transform to compare with raw outputs
            float r_raw = inverseSigmoid(action[0]);
            float theta_raw = action[1]; // θ is linear

            // Assume unit variance for simplicity
            // In practice, you'd learn variance too
            float logProb = 0;
            logProb += -0.5f * (float)Math.pow(r_raw - mean[0], 2);
            logProb += -0.5f * (float)Math.pow(theta_raw - mean[1], 2);

            return logProb;
        }

        // Update weights using gradients
        public void update(float[][] gradW1, float[] gradB1,
                           float[][] gradW2, float[] gradB2, float lr) {
            for (int i = 0; i < hiddenSize; i++) {
                for (int j = 0; j < stateSize; j++) {
                    W1[i][j] += lr * gradW1[i][j];
                }
                b1[i] += lr * gradB1[i];
            }

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < hiddenSize; j++) {
                    W2[i][j] += lr * gradW2[i][j];
                }
                b2[i] += lr * gradB2[i];
            }
        }
    }

    // ========== EXPERIENCE BUFFER ==========

    private static class ExperienceBuffer {
        private List<float[]> states = new ArrayList<>();
        private List<float[]> actions = new ArrayList<>();
        private List<Float> rewards = new ArrayList<>();
        private List<float[]> nextStates = new ArrayList<>();
        private List<Boolean> dones = new ArrayList<>();

        private int maxSize;

        ExperienceBuffer(int maxSize) {
            this.maxSize = maxSize;
        }

        void add(float[] state, float[] action, float reward,
                 float[] nextState, boolean done) {
            if (states.size() >= maxSize) {
                states.remove(0);
                actions.remove(0);
                rewards.remove(0);
                nextStates.remove(0);
                dones.remove(0);
            }

            states.add(Arrays.copyOf(state, state.length));
            actions.add(Arrays.copyOf(action, action.length));
            rewards.add(reward);
            nextStates.add(Arrays.copyOf(nextState, nextState.length));
            dones.add(done);
        }

        List<Batch> sampleBatches(int batchSize, int numBatches) {
            List<Batch> batches = new ArrayList<>();
            Random rand = new Random();

            for (int b = 0; b < numBatches; b++) {
                Batch batch = new Batch();
                for (int i = 0; i < batchSize; i++) {
                    int idx = rand.nextInt(states.size());
                    batch.states.add(states.get(idx));
                    batch.actions.add(actions.get(idx));
                    batch.rewards.add(rewards.get(idx));
                    batch.nextStates.add(nextStates.get(idx));
                    batch.dones.add(dones.get(idx));
                }
                batches.add(batch);
            }
            return batches;
        }

        static class Batch {
            List<float[]> states = new ArrayList<>();
            List<float[]> actions = new ArrayList<>();
            List<Float> rewards = new ArrayList<>();
            List<float[]> nextStates = new ArrayList<>();
            List<Boolean> dones = new ArrayList<>();
        }
    }

    // ========== AGENT IMPLEMENTATION ==========

    private PolicyNetwork policy;
    private ExperienceBuffer buffer;
    private Random random;

    // Hyperparameters
    private float learningRate = 0.001f;
    private float gamma = 0.99f;      // Discount factor
    private float lambda = 0.95f;     // GAE parameter
    private float explorationNoise = 0.1f;
    private int batchSize = 32;

    public SlimeRLAgent(Player p, int stateSize, int bufferSize) {
        this.player = p;
        this.policy = new PolicyNetwork(stateSize, 64);
        this.buffer = new ExperienceBuffer(bufferSize);
        this.random  = player.getGameboard().rng;
    }

    // ========== CORE RL ALGORITHM: PPO ==========

    public void trainPPO(int epochs, float clipEpsilon) {
        if (buffer.states.size() < batchSize) return;

        // Sample experiences
        List<ExperienceBuffer.Batch> batches = buffer.sampleBatches(batchSize, epochs);

        for (ExperienceBuffer.Batch batch : batches) {
            // Compute advantages using Generalized Advantage Estimation
            float[] advantages = computeGAE(batch);

            // Compute old log probabilities (before update)
            float[] oldLogProbs = new float[batch.states.size()];
            for (int i = 0; i < batch.states.size(); i++) {
                oldLogProbs[i] = policy.logProbability(
                    batch.states.get(i), batch.actions.get(i));
            }

            // PPO update
            float policyLoss = 0;
            float[][] gradW1 = new float[policy.hiddenSize][policy.stateSize];
            float[] gradB1 = new float[policy.hiddenSize];
            float[][] gradW2 = new float[2][policy.hiddenSize];
            float[] gradB2 = new float[2];

            for (int i = 0; i < batch.states.size(); i++) {
                float[] state = batch.states.get(i);
                float[] action = batch.actions.get(i);
                float advantage = advantages[i];

                // Current log probability
                float logProb = policy.logProbability(state, action);
                float ratio = (float)Math.exp(logProb - oldLogProbs[i]);

                // Clipped objective
                float clippedRatio = Math.max(1 - clipEpsilon,
                    Math.min(1 + clipEpsilon, ratio));

                float loss = -Math.min(ratio * advantage,
                    clippedRatio * advantage);

                policyLoss += loss;

                // Compute gradients (simplified - in practice use backprop)
                // For demonstration, we'll use a simple finite difference
                addGradients(state, action, loss,
                    gradW1, gradB1, gradW2, gradB2);
            }

            // Average and update
            scaleGradients(gradW1, gradB1, gradW2, gradB2,
                1.0f / batch.states.size());

            policy.update(gradW1, gradB1, gradW2, gradB2, learningRate);

            // Decay exploration
            explorationNoise *= 0.995f;
        }
    }

    private float[] computeGAE(ExperienceBuffer.Batch batch) {
        // Simplified - in practice you'd need a value network
        // For now, use Monte Carlo returns
        float[] advantages = new float[batch.states.size()];

        for (int i = 0; i < batch.states.size(); i++) {
            float G = 0;
            float discount = 1;
            for (int j = i; j < Math.min(i + 10, batch.states.size()); j++) {
                G += discount * batch.rewards.get(j);
                discount *= gamma;
                if (batch.dones.get(j)) break;
            }
            advantages[i] = G;
        }

        // Normalize advantages
        normalize(advantages);
        return advantages;
    }

    // ========== INTERFACE WITH YOUR GAME ==========

    public Vector2 decideNewCursorVector(float[] state) {
        // Use policy to get action
        float[] action = policy.sampleAction(state, random, explorationNoise);

        // Convert to Vector2
        float r = action[0];
        float theta = action[1];

        return new Vector2(
            r * (float)Math.cos(theta),
            r * (float)Math.sin(theta)
        );
    }

    public void recordExperience(float[] state, Vector2 planchette,
                                 float reward, float[] nextState, boolean done) {
        // Convert planchette back to (r, θ)
        float r = planchette.len();
        float theta = planchette.angleRad();
        float[] action = new float[]{r, theta};

        buffer.add(state, action, reward, nextState, done);
    }

    public void learnFromExperience() {
        trainPPO(4, 0.2f); // 4 epochs, clip epsilon = 0.2
    }

    // ========== SIMPLE REINFORCE (EASIER TO START) ==========

    public void trainREINFORCE(List<float[]> episodeStates,
                               List<float[]> episodeActions,
                               List<Float> episodeRewards) {
        // Compute returns
        float[] returns = computeReturns(episodeRewards);

        // Normalize returns for stability
        normalize(returns);

        // Update policy
        for (int t = 0; t < episodeStates.size(); t++) {
            float[] state = episodeStates.get(t);
            float[] action = episodeActions.get(t);
            float G = returns[t];

            // REINFORCE update: ∇J ≈ G * ∇log π(a|s)
            // We'll approximate with a target that scales with return
            float[] scaledAction = Arrays.copyOf(action, 2);
            scaledAction[0] *= (1 + G * 0.1f); // Scale magnitude by return
            scaledAction[1] += G * 0.05f;      // Shift angle by return

            // Use supervised learning toward scaled action
            // (Simplified - in practice compute proper gradient)
            simpleGradientUpdate(state, scaledAction, learningRate);
        }
    }

    // ========== UTILITY METHODS ==========

    private static float[][] randomMatrix(int rows, int cols, float scale) {
        float[][] matrix = new float[rows][cols];
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (rand.nextFloat() * 2 - 1) * scale;
            }
        }
        return matrix;
    }

    private static float[] randomVector(int size, float scale) {
        float[] vector = new float[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            vector[i] = (rand.nextFloat() * 2 - 1) * scale;
        }
        return vector;
    }

    private static float sigmoid(float x) {
        return (float)(1.0 / (1.0 + Math.exp(-x)));
    }

    private static float inverseSigmoid(float y) {
        return (float)Math.log(y / (1 - y));
    }

    private float[] computeReturns(List<Float> rewards) {
        float[] returns = new float[rewards.size()];
        float G = 0;
        for (int t = rewards.size() - 1; t >= 0; t--) {
            G = rewards.get(t) + gamma * G;
            returns[t] = G;
        }
        return returns;
    }

    private void normalize(float[] array) {
        float mean = 0, std = 0;
        for (float v : array) mean += v;
        mean /= array.length;

        for (float v : array) std += (v - mean) * (v - mean);
        std = (float)Math.sqrt(std / array.length);

        if (std > 1e-8) {
            for (int i = 0; i < array.length; i++) {
                array[i] = (array[i] - mean) / std;
            }
        }
    }

    private void scaleGradients(float[][] gradW1, float[] gradB1,
                                float[][] gradW2, float[] gradB2, float scale) {
        // Scale all gradients by the batch size (to get average gradient)

        // Scale W1 gradients
        for (int i = 0; i < gradW1.length; i++) {
            for (int j = 0; j < gradW1[0].length; j++) {
                gradW1[i][j] *= scale;
            }
        }

        // Scale b1 gradients
        for (int i = 0; i < gradB1.length; i++) {
            gradB1[i] *= scale;
        }

        // Scale W2 gradients
        for (int i = 0; i < gradW2.length; i++) {
            for (int j = 0; j < gradW2[0].length; j++) {
                gradW2[i][j] *= scale;
            }
        }

        // Scale b2 gradients
        for (int i = 0; i < gradB2.length; i++) {
            gradB2[i] *= scale;
        }
    }



    // Simplified gradient computation (for demonstration)
    private void addGradients(float[] state, float[] action, float loss,
                              float[][] gradW1, float[] gradB1,
                              float[][] gradW2, float[] gradB2) {
        // Finite difference approximation
        float eps = 1e-4f;

        // Approximate gradient for output weights
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < policy.hiddenSize; j++) {
                float orig = policy.W2[i][j];

                policy.W2[i][j] = orig + eps;
                float lossPlus = computeLoss(state, action);

                policy.W2[i][j] = orig - eps;
                float lossMinus = computeLoss(state, action);

                policy.W2[i][j] = orig; // Restore

                gradW2[i][j] += (lossPlus - lossMinus) / (2 * eps);
            }
        }
    }

    private float computeLoss(float[] state, float[] action) {
        // Simplified loss for gradient approximation
        float[] pred = policy.forward(state);
        float error = 0;
        error += (float)Math.pow(pred[0] - inverseSigmoid(action[0]), 2);
        error += angularError(pred[1], action[1]);
        return error;
    }

    private float angularError(float a, float b) {
        float diff = (float)Math.abs((a - b) % (2 * Math.PI));
        if (diff > Math.PI) diff = (float)(2 * Math.PI - diff);
        return diff * diff;
    }

    private void simpleGradientUpdate(float[] state, float[] targetAction, float lr) {
        // Forward pass to get current prediction
        float[] currentAction = policy.forward(state);

        // Convert targets to raw network outputs (inverse transform)
        float targetR_raw = inverseSigmoid(targetAction[0]);
        float targetTheta_raw = targetAction[1]; // θ is already linear

        // Compute errors at output layer
        float errorR = targetR_raw - currentAction[0];
        float errorTheta = targetTheta_raw - currentAction[1];

        // === Backward pass: compute gradients ===

        // First, do forward pass while storing intermediate values
        float[] hidden = new float[policy.hiddenSize];
        float[] hiddenPreActivation = new float[policy.hiddenSize];

        // Layer 1 forward (storing pre-activation for backprop)
        for (int i = 0; i < policy.hiddenSize; i++) {
            float sum = policy.b1[i];
            for (int j = 0; j < policy.stateSize; j++) {
                sum += policy.W1[i][j] * state[j];
            }
            hiddenPreActivation[i] = sum;
            hidden[i] = Util.relu(sum);
        }

        // Initialize gradients
        float[][] gradW2 = new float[2][policy.hiddenSize];
        float[] gradB2 = new float[2];
        float[][] gradW1 = new float[policy.hiddenSize][policy.stateSize];
        float[] gradB1 = new float[policy.hiddenSize];

        // === Output layer gradients ===
        // For linear output: ∂Loss/∂W2 = error * hidden
        for (int i = 0; i < 2; i++) {
            float error = (i == 0) ? errorR : errorTheta;
            gradB2[i] = error;  // ∂Loss/∂b2 = error

            for (int j = 0; j < policy.hiddenSize; j++) {
                gradW2[i][j] = error * hidden[j];  // ∂Loss/∂W2 = error * hidden
            }
        }

        // === Hidden layer gradients ===
        // Error backpropagated through hidden layer
        float[] hiddenError = new float[policy.hiddenSize];

        for (int j = 0; j < policy.hiddenSize; j++) {
            // Error coming from both outputs
            float errorFromR = errorR * policy.W2[0][j];
            float errorFromTheta = errorTheta * policy.W2[1][j];
            float totalError = errorFromR + errorFromTheta;

            // Multiply by derivative of ReLU
            float reluDerivative = (hiddenPreActivation[j] > 0) ? 1 : 0;
            hiddenError[j] = totalError * reluDerivative;

            // Gradients for W1 and b1
            gradB1[j] = hiddenError[j];

            for (int k = 0; k < policy.stateSize; k++) {
                gradW1[j][k] = hiddenError[j] * state[k];
            }
        }

        // === Update weights ===
        for (int i = 0; i < policy.hiddenSize; i++) {
            // Update W1
            for (int j = 0; j < policy.stateSize; j++) {
                policy.W1[i][j] += lr * gradW1[i][j];
            }
            // Update b1
            policy.b1[i] += lr * gradB1[i];
        }

        for (int i = 0; i < 2; i++) {
            // Update W2
            for (int j = 0; j < policy.hiddenSize; j++) {
                policy.W2[i][j] += lr * gradW2[i][j];
            }
            // Update b2
            policy.b2[i] += lr * gradB2[i];
        }
    }

    // ========== INTEGRATION WITH THE GAME ==========

    public static class GameRLInterface {
        private SlimeRLAgent agent;
        private List<float[]> episodeStates = new ArrayList<>();
        private List<float[]> episodeActions = new ArrayList<>();
        private List<Float> episodeRewards = new ArrayList<>();

        public Player player;

        public GameBoard gameBoard;

        public GameRLInterface(Player p, int stateSize) {
            this.agent = new SlimeRLAgent(p, stateSize, 10000);
            this.player = p;
        }

        public Vector2 nextMove() {

            // The current state is information about the vertex closest to the planchette cursor (not the planchette itself)
            float[] state = extractState();
            episodeStates.add(state);

            // Use the state to decide the new cursor position
            Vector2 cursor = agent.decideNewCursorVector(state);
            PlayerHud hud = player.getHud();
            hud.setBotInputVector(Util.polarToXYFloat(cursor));


            // 3. Convert to (r, θ) for storage
            float[] action = new float[]{
                cursor.len(),
                cursor.angleRad()
            };
            episodeActions.add(action);

            return cursor;
        }

        public void recordReward(float reward) {
            episodeRewards.add(reward);
        }

        public void endEpisode(boolean gameOver) {
            // Learn from the completed episode
            if (gameOver) {
                agent.trainREINFORCE(episodeStates, episodeActions, episodeRewards);

                // Also add to experience buffer for PPO
                for (int t = 0; t < episodeStates.size() - 1; t++) {
                    agent.recordExperience(
                        episodeStates.get(t),
                        new Vector2(
                            episodeActions.get(t)[0] * (float)Math.cos(episodeActions.get(t)[1]),
                            episodeActions.get(t)[0] * (float)Math.sin(episodeActions.get(t)[1])
                        ),
                        episodeRewards.get(t),
                        episodeStates.get(t + 1),
                        t == episodeStates.size() - 1
                    );
                }

                // Clear episode data
                episodeStates.clear();
                episodeActions.clear();
                episodeRewards.clear();
            }
        }



        public void saveAgent(String filename) throws IOException {
            saveWeights(agent.policy, filename);
        }

        public float[] extractState() {
            /*
            Look at the current planchette cursor position (not the planchette itself)

            Return information about its neighboring hexes and vertices
            */

            return player.gatherInputs();
        }



        public void dispose() {

        }
    }

    // ========== SERIALIZATION ==========

    public static void saveWeights(PolicyNetwork network, String filename)
        throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
            new FileOutputStream(filename))) {
            // Save architecture
            dos.writeInt(network.stateSize);
            dos.writeInt(network.hiddenSize);

            // Save weights
            saveMatrix(dos, network.W1);
            saveVector(dos, network.b1);
            saveMatrix(dos, network.W2);
            saveVector(dos, network.b2);
        }
    }

    private static void saveMatrix(DataOutputStream dos, float[][] matrix)
        throws IOException {
        dos.writeInt(matrix.length);
        dos.writeInt(matrix[0].length);
        for (float[] row : matrix) {
            for (float val : row) dos.writeFloat(val);
        }
    }

    private static void saveVector(DataOutputStream dos, float[] vector)
        throws IOException {
        dos.writeInt(vector.length);
        for (float val : vector) dos.writeFloat(val);
    }
}
