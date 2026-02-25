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


    public void save(FileHandler fh);


    BitSet get_transition_bit_mask();

    double[][][] get_emission_weights();

    double[][][] get_transition_weights();

    void setPlayerTournamentId(Point p);

}
