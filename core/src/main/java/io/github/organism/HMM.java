package io.github.organism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class HMM {

    public HashMap<Integer, HashMap<String, HashMap<Integer, Double>>> params = new HashMap<>();
    public double randomness;
    private final Random rng;

    public Integer states;
    public Integer inputs;
    public Integer current_state;
    public double [][][] transition_weights;
    public double [][][] emission_weights;

    GameBoard game_board;
    public HMM(GameBoard gb, int s, float r, int n) {
        game_board = gb;
        states = Math.max(3, s);
        inputs = n;

        rng = game_board.rng;
        randomness = r;

        current_state = rng.nextInt(states);

        init_transition_weights();
        init_emission_weights();
    }
    private void init_transition_weights() {
        transition_weights = new double[states][states][inputs];
        for (int i=0; i<states; i++){
            for (int j=0; j<states; j++){
                for (int k=0; k<inputs; k++){
                    transition_weights[i][j][k] = rng.nextDouble();
                }
            }
        }
    }

    private void init_emission_weights() {
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

    private double calculate_score(float [] input_vals, double [] weights){
        double score = 0;
        for (int i=0; i<input_vals.length; i++){
            score += (weights[i] * Math.log(input_vals[i]));
        }
        return score;
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
}
