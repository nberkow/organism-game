package io.github.organism;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class BotPlayer implements Player{

    public LinkedList<Integer> move_queue;
    public ActionHistory move_history;

    public GameBoard game_board;

    public String player_name;

    public HMM model;

    public Organism organism;

    public BotPlayer(GameBoard gb, String name, Organism org, HMM m){

        game_board = gb;
        model = m;
        player_name = name;
        organism = org;
        move_queue = new LinkedList<>();
        move_history = new ActionHistory(game_board);
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
        for (String p : game_board.players.keySet()) {
            if (!Objects.equals(p, player_name)){
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
            return move_queue.remove();
        }
        return on_empty_queue();
    }

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
}
