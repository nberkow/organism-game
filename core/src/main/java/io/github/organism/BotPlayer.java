package io.github.organism;

import java.util.LinkedList;

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

    public Integer compute_move(){
        return model.generate_move();
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
        float t = game_board.rng.nextFloat() / 2   ;

        if (t > prob){
            Integer move = compute_move();
            queue_move(move);
        }
    }
}
