package io.github.organism;

import java.awt.Point;
import java.util.BitSet;

public interface Model {

    public void set_weights(double [][][] tr, double [][][] em);

    public void init_random_weights();

    public void warmup();

    public Integer emit(float [] input_vals);

    public void transition(float [] input_vals);

    public Model spawn();

    public void dispose();

    public String get_model_type();

    public void save(FileHandler fh);


    void notify_move_completed(int turn, float territory);

    BitSet get_transition_bit_mask();

    double[][][] get_emission_weights();

    double[][][] get_transition_weights();
    void set_transition_bit_mask(BitSet m);

    void set_emission_weights(double[][][] w);

    void set_transition_weights(double[][][] w);

    void set_name(String name);

    void setPlayerTournamentId(Point p);

    void apply_transition_mask();

    void mutate_bitmask();
}
