package io.github.organism;

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

public class HMM_pair_score implements Model  {

    /*
    uses a pair score per node to make transition/emission choices

    the bit mask is used to determine which scores are used.
     */

    public final Random rng;

    public Point player_tournament_id;
    public static Integer states;
    public static Integer inputs;
    public Integer current_state;
    public double [][][] transition_weights;
    public double [][][] emission_weights;
    public String name;

    BitSet transition_bit_mask;

    String model_type = "pair";
    OrganismGame game;
    public HMM_pair_score(OrganismGame g, int s, int n) {
        name = "bot";
        rng = game.rng;
        current_state = rng.nextInt(states);
    }

    public void set_weights(double [][][] tr, double [][][] em) {
        transition_weights = tr;
        emission_weights = em;
    }

    public void init_random_weights() {
        init_random_transition_mask();
        init_random_transition_weights();
        apply_transition_mask();
        init_random_emission_weights();
    }

    /**
     *
     */
    @Override
    public void warmup() {

    }

    private void init_random_transition_mask() {
        int totalBits = states * states * inputs;
        transition_bit_mask = new BitSet(totalBits);

        // Randomly set bits in the BitSet
        for (int i = 0; i < totalBits; i++) {
            if (rng.nextBoolean()) {
                transition_bit_mask.set(i); // Set the bit at position i
            }
        }
    }

    private void init_random_transition_weights() {
        // the number of possible transitions is the number of states squared

        // every input has a weight for every transition pair

        transition_weights = new double[states][states][inputs];
        for (int i=0; i<states; i++){
            for (int j=0; j<states; j++){
                for (int k=0; k<inputs; k++){
                    transition_weights[i][j][k] = rng.nextDouble();
                }
            }
        }
    }

    private void init_random_emission_weights() {
        // there are 4 possible emissions, 1 for each move plus a null output
        emission_weights = new double[states][4][inputs];
        for (int i=0; i<states; i++){
            for (int j=0; j<4; j++){
                for (int k=0; k<inputs; k++){
                    emission_weights[i][j][k] = rng.nextDouble();
                }
            }
        }
    }

    public void apply_transition_mask() {
        int n = 0; // Bit position counter
        for (int i = 0; i < states; i++) {
            for (int j = 0; j < states; j++) {
                for (int k = 0; k < inputs; k++) {
                    // Check if the bit at position n is not set
                    if (!transition_bit_mask.get(n)) {
                        transition_weights[i][j][k] = 0;
                    }
                    n++; // Move to the next bit position
                }
            }
        }
    }

    /**
     *
     */
    @Override
    public void mutate_bitmask() {

    }

    private double calculate_score(float [] input_vals, double [] weights){
        // sum the products of all the scores with their weights
        // and take the mod1 remainder

        double score = 0;
        for (int i=0; i<input_vals.length; i++){
            score += weights[i] * input_vals[i];
        }
        return score % 1;
    }

    private ArrayList<Double> normalize_scores(ArrayList<Double> scores) {
        double max_score = Collections.max(scores);
        double total = 0;

        for (int i=0; i<scores.size(); i++){
            scores.set(i, Math.pow(Math.E, scores.get(i) - max_score));
            total += scores.get(i);
        }

        for (int i=0; i<scores.size(); i++){
            scores.set(i, scores.get(i) / total);
        }

        return scores;
    }

    private int weighted_random_choice(ArrayList<Double> scores) {
        int s = rng.nextInt(scores.size());
        double score_sum = 0;
        double r = rng.nextDouble();

        while (score_sum < r) {
            s = (s+1) % scores.size();
            score_sum += scores.get(s);
        }
        return s;
    }

    public Integer emit(float [] input_vals) {
        /*
        Emit a move based on the current state
        */
        ArrayList<Double> scores = new ArrayList<>();
        double [] weights = {};
        for (int emission=0; emission<4; emission++){
            weights = emission_weights[current_state][emission];
            scores.add(calculate_score(input_vals, weights));
        }
        ArrayList<Double> norm_scores = normalize_scores(scores);

        return weighted_random_choice(norm_scores);

    }

    public void transition(float [] input_vals) {
        /*
        Transition to a new state
        */
        ArrayList<Double> scores = new ArrayList<>();
        double [] weights = {};
        for (int state_to=0; state_to<states; state_to++){
            weights = transition_weights[current_state][state_to];
            scores.add(calculate_score(input_vals, weights));
        }
        ArrayList<Double> norm_scores = normalize_scores(scores);
        current_state = weighted_random_choice(norm_scores);

    }

    /**
     * @return
     */
    @Override
    public Model spawn() {
        return null;
    }

    public void dispose() {
        transition_weights = null;
        emission_weights = null;
        game = null;
    }

    /**
     * @return
     */
    @Override
    public String get_model_type() {
        return "";
    }

    /**
     * @param fh
     */
    @Override
    public void save(FileHandler fh) {

    }

    /**
     * @param turn
     * @param territory
     */
    @Override
    public void notify_move_completed(int turn, float territory) {

    }

    /**
     * @return
     */
    @Override
    public BitSet get_transition_bit_mask() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public double[][][] get_emission_weights() {
        return new double[0][][];
    }

    /**
     * @return
     */
    @Override
    public double[][][] get_transition_weights() {
        return new double[0][][];
    }

    /**
     * @param m
     */
    @Override
    public void set_transition_bit_mask(BitSet m) {

    }

    /**
     * @param w
     */
    @Override
    public void set_emission_weights(double[][][] w) {

    }

    /**
     * @param w
     */
    @Override
    public void set_transition_weights(double[][][] w) {

    }

    /**
     * @param name
     */
    @Override
    public void set_name(String name) {

    }

    /**
     * @param p
     */
    @Override
    public void setPlayerTournamentId(Point p) {

    }

}
