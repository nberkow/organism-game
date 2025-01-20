package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class BotPlayer implements Player{

    public LinkedList<Integer> move_queue;
    //public ActionHistory move_history;

    public GameBoard game_board;
    public int game_index; // index within a single game
    int [] tournament_id; // id in tournament or other large player collection

    public Color color;
    public String player_name;

    public HMM model;

    public Organism organism;

    int most_recent_move;

    public BotPlayer(GameBoard gb, String name, int idx, int [] id, Organism org, HMM mod, Color c){

        game_board = gb;
        color = c;
        player_name = name;
        tournament_id = id;
        game_index = idx;
        organism = org;
        model = mod;
        move_queue = new LinkedList<>();

    }


    public void queue_move(Integer move) {
        move_queue.add(move);
    }

    public Integer compute_move() {
        float [] hmm_inputs = gather_inputs();
        return model.emit(hmm_inputs);
    }

    public void transition(){
        float [] hmm_inputs = gather_inputs();
        model.transition(hmm_inputs);
    }

    public float [] gather_inputs(){
        float [] hmm_inputs = new float [6];

        // this player's energy
        hmm_inputs[0] = (float) organism.energy / organism.MAX_ENERGY;

        // this player's territory
        hmm_inputs[1] = organism.territory_vertex.get_unmasked_vertices();

        ArrayList<Player> opponents = new ArrayList<>();
        for (int [] p : game_board.players.keySet()) {
            if (!Arrays.equals(p, tournament_id)){
                opponents.add(game_board.players.get(p));
            }
        }

        // opponent energy and territory
        hmm_inputs[2] = (float) opponents.get(0).get_organism().energy / organism.MAX_ENERGY;
        hmm_inputs[3] = opponents.get(0).get_organism().territory_vertex.get_unmasked_vertices();

        hmm_inputs[4] = (float) opponents.get(1).get_organism().energy / organism.MAX_ENERGY;
        hmm_inputs[5] = opponents.get(1).get_organism().territory_vertex.get_unmasked_vertices();

        return hmm_inputs;

    }

    public Integer get_move() {
        if (!move_queue.isEmpty()) {
            int m = move_queue.remove();
            most_recent_move = m;
            return m;
        }
        return on_empty_queue();
    }

    /**
     * @return
     */
    @Override
    public Color get_color() {
        return color;
    }

    /**
     * @return
     */
    @Override
    public int get_index() {
        return game_index;
    }

    /**
     * @return
     */

    public Integer on_empty_queue() {
        return null;
    }

    @Override
    public String get_player_name() {
        return player_name;
    }

    public Organism get_organism(){
        return organism;
    }

    @Override
    public LinkedList<Integer> get_move_queue() {
        return move_queue;
    }

    @Override
    public void generate_and_queue() {
        float prob = (float) move_queue.size() / game_board.MAX_QUEUED_ACTIONS;
        float t = game_board.rng.nextFloat() / 2;

        if (t > prob){
            Integer move = compute_move();
            if (move < 3) {
                queue_move(move);
            }
        }
    }

    /**
     * @return
     */
    @Override
    public int get_most_recent_move() {
        return most_recent_move;
    }
}
