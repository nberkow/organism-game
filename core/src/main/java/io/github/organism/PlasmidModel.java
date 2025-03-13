package io.github.organism;

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

public class PlasmidModel implements Model {

    /*
    Repeat a fixed string of moves
     */

    public final Random rng;
    public Point player_tournament_id;
    public Integer current_pos;

    public String name;

    int plasmid_size;
    ArrayList<Integer> plasmid_sequence;
    BitSet transition_bit_mask;

    OrganismGame game;
    public PlasmidModel(OrganismGame g, int n) {
        game = g;
        plasmid_size = n;
        name = "bot";
        rng = game.rng;

        make_random_sequence(n);
        current_pos = rng.nextInt(plasmid_size);
    }

    private void make_random_sequence(int n) {

    }

    private void set_sequence(int n) {

    }

    public void set_weights(double [][][] tr, double [][][] em) {}

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

    private void init_random_transition_mask() { }

    private void init_random_transition_weights() {}

    private void init_random_emission_weights() {}

    public void apply_transition_mask() {}

    /**
     *
     */
    @Override
    public void mutate_bitmask() {}

    private double calculate_score(float [] input_vals, double [] weights){
        return 0d;
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
        return 0;
    }

    public void transition(float [] input_vals) {}

    /**
     * @return
     */
    @Override
    public Model spawn() {
        return null;
    }

    public void dispose() {}

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
