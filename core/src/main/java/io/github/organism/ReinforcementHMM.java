package io.github.organism;

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class ReinforcementHMM  implements Model {

    /*
    Increases weights of transitions in real time

     */

    private final Random rng;

    public Point player_tournament_id;
    public static Integer states;
    public static Integer inputs;
    public Integer current_state;
    public double [][][] transition_weights;
    public double [][][] emission_weights;
    public String name;
    BitSet transition_bit_mask;
    ArrayList<Float> territory_history;
    ArrayList<ArrayList<Point>> transition_history;
    ArrayList<ArrayList<Point>> emission_history;
    ArrayList<Point> current_transitions;
    ArrayList<Point> current_emissions;
    int max_history_length = 20;

    String model_type = "re";

    OrganismGame game;
    public ReinforcementHMM(OrganismGame g, int s, int n) {
        game = g;
        states = Math.max(3, s);
        inputs = n;
        name = "bot";
        rng = game.rng;
        current_state = rng.nextInt(states);

        transition_history = new ArrayList<>();
        territory_history = new ArrayList<>();
        emission_history = new ArrayList<>();

        current_transitions = new ArrayList<>();
        current_emissions = new ArrayList<>();
    }

    @Override
    public void notify_move_completed(int turn, float territory) {

        if (territory < 1) {
            return;
        }

        territory_history.add(0, territory);
        transition_history.add(0, current_transitions);
        emission_history.add(0, current_emissions);

        if (territory_history.size() > max_history_length){
            territory_history.remove(territory_history.size() - 1);
            transition_history.remove(transition_history.size() - 1);
            emission_history.remove(emission_history.size() - 1);
        }

        current_transitions = new ArrayList<>();
        current_emissions = new ArrayList<>();

        for (int i=1; i<territory_history.size(); i++){
            float prev_territory = territory_history.get(i);
            float territory_delta = territory - prev_territory;

            // update the vector of weights corresponding to the transitions that were made
            for (Point t : transition_history.get(i)) {
                double [] weights = transition_weights[t.x][t.y];
                for (int w=0; w<weights.length; w++){
                    double weight = weights[w];
                    weights[w] = weight + Math.pow(territory_delta/territory, 0.5);
                }
                transition_weights[t.x][t.y] = weights;
            }

            // update the vector of weights corresponding to the emission that were made
            for (Point t : emission_history.get(i)) {
                double [] weights = emission_weights[t.x][t.y];
                for (int w=0; w<weights.length; w++){
                    double weight = weights[w];
                    weights[w] = weight + (territory_delta/territory);
                }
                emission_weights[t.x][t.y] = weights;
            }

        }
    }

    public void warmup() {

        int [] counts = new int [states];

        for (int j=0; j<100; j++) {
            float[] random_inputs = new float[inputs];
            for (int i = 0; i < inputs; i++) {
                random_inputs[i] = rng.nextFloat();
            }

            for (int i = 0; i < 100; i++) {
                transition(random_inputs);
                counts[current_state] ++;
            }
        }

        int max = 0;

        for (int c=0; c<states; c++) {
            if (counts[c] > max){
                max = counts[c];
                current_state = c;
            }
        }
    }

    /**
     * @param tr
     * @param em
     */
    @Override
    public void set_weights(double [][][] tr, double [][][] em) {
        transition_weights = tr;
        emission_weights = em;
        warmup();
    }

    public void init_random_weights() {

        init_random_transition_mask();
        init_random_transition_weights();
        //apply_transition_mask();
        init_random_emission_weights();
        warmup();

    }

    public BitSet get_transition_bit_mask(){
        return transition_bit_mask;
    }

    public double [][][] get_emission_weights(){
        return emission_weights;
    }

    public double [][][] get_transition_weights(){
        return transition_weights;
    }

    /**
     * @param m
     */
    @Override
    public void set_transition_bit_mask(BitSet m) {
        transition_bit_mask = m;
    }

    /**
     * @param w
     */
    @Override
    public void set_emission_weights(double[][][] w) {
        emission_weights = w;
    }

    /**
     * @param w
     */
    @Override
    public void set_transition_weights(double[][][] w) {
        transition_weights = w;
    }

    /**
     * @param name
     */
    @Override
    public void set_name(String name) {
        this.name = name;
    }

    /**
     * @param p
     */
    @Override
    public void setPlayerTournamentId(Point p) {
        player_tournament_id = p;
    }

    private void init_random_transition_mask() {}

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
    }

    /**
     *
     */
    @Override
    public void mutate_bitmask() {

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
        int s = current_state;
        ArrayList<Double> scores = new ArrayList<>();
        double [] weights = {};
        for (int emission=0; emission<4; emission++){
            weights = emission_weights[current_state][emission];
            scores.add(calculate_score(input_vals, weights));
        }
        ArrayList<Double> norm_scores = normalize_scores(scores);
        int n = weighted_random_choice(norm_scores);
        current_emissions.add(new Point(s, n));
        return n;

    }

    public void transition(float [] input_vals) {
        /*
        Transition to a new state
        */

        int s = current_state;
        ArrayList<Double> scores = new ArrayList<>();
        double [] weights = {};
        for (int state_to=0; state_to<states; state_to++){
            weights = transition_weights[current_state][state_to];
            scores.add(calculate_score(input_vals, weights));
        }
        ArrayList<Double> norm_scores = normalize_scores(scores);
        current_state = weighted_random_choice(norm_scores);
        current_transitions.add(new Point(s, current_state));
    }

    /**
     * @return
     */
    @Override
    public Model spawn() {
        return null;
    }

    /**
     *
     */
    @Override
    public void dispose() {

    }

    /**
     *
     */
    @Override
    public String get_model_type() {
        return model_type;
    }

    /**
     * @param fh
     */
    @Override
    public void save(FileHandler fh) {

    }

}
