package io.github.organism;

import java.util.HashMap;
import java.util.Random;

public class HMM {

    public HashMap<Integer, HashMap<String, HashMap<Integer, Double>>> params = new HashMap<>();
    public double randomness;
    private Random rng;

    public Integer state;
    private HashMap<String, Double> bias;

    GameBoard game_board;
    public HMM(GameBoard gb) {
        game_board = gb;
        rng = game_board.rng;
        state = rng.nextInt(2);
        bias = new HashMap<>();
        bias.put("emission", 0.8);
        bias.put("transition", 1d/3);
    }

    public void init(double r){

        randomness = r;
        for (int state_in=0; state_in<=2; state_in++){
            params.put(state_in, new HashMap<>());
            for (String probability_type : new String [] {"emission", "transition"}){
                HashMap<String, HashMap<Integer, Double>> s_in = params.get(state_in);
                s_in.put(probability_type, new HashMap<>());

                double denom = 0d;
                for (int state_out=0; state_out<=2; state_out++){
                    HashMap<Integer, Double> s_out = params.get(state_in).get(probability_type);
                    double noise = rng.nextDouble() - 0.5;
                    double probability = (1 - bias.get(probability_type)/2) * (1 - randomness) + noise * randomness;
                    if (state_in == state_out) {
                        probability = (bias.get(probability_type))*(1 - randomness) + noise * randomness;
                    }
                    s_out.put(state_out, probability);
                    denom += probability;
                }

                // normalize
                for (int state_out=0; state_out<=2; state_out++){
                    double prob = params.get(state_in).get(probability_type).get(state_out);
                    HashMap<Integer, Double> s_out = params.get(state_in).get(probability_type);
                    s_out.put(state_out, prob / denom);
                }
            }
        }
    }

    public Integer generate_move() {
        /*
        Emit a move based on the current state and transition to the next state
        */

        HashMap<Integer, Double> emission_probabilities = params.get(state).get("emission");
        double rnum = rng.nextDouble();
        Integer emission = null;
        Double sum = 0d;
        int i = 0;
        while (i<=2 && emission == null){
            sum += emission_probabilities.get(i);
            if (rnum < sum){
                emission = i;
            }
            i++;
        }

        // transition to the next state
        HashMap<Integer, Double> transition_probabilities = params.get(state).get("transition");
        rnum = rng.nextDouble();
        Integer transition = null;
        sum = 0d;
        i = 0;
        while (i<=2 && transition == null){
            sum += transition_probabilities.get(i);
            if (rnum < sum){
                transition = i;
            }
            i++;
        }
        state = transition;
        return emission;
    }
}
