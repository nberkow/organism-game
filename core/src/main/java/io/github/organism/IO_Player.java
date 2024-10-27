package io.github.organism;

import java.util.ArrayList;
import java.util.LinkedList;

public class IO_Player  implements Player {

    String player_name;

    Organism organism;

    GameBoard game_board;
    LinkedList<Integer> move_queue;

    ActionHistory move_history;

    boolean player_2 = false;

    public IO_Player(GameBoard gb, String name, Organism org, boolean p2){

        game_board = gb;
        player_name = name;
        organism = org;
        player_2 = p2;
        move_queue = new LinkedList<>();
        move_history = new ActionHistory(game_board);
    }

    @Override
    public void queue_move(Integer move) {
        if (move_queue.size() < game_board.MAX_QUEUED_ACTIONS){
            move_queue.add(move);
        }
    }

    @Override
    public Integer get_move() {
        if (!move_queue.isEmpty()){
            return move_queue.remove();
        }
        return null;
    }

    @Override
    public Integer on_empty_queue() {
        return 0;
    }


    @Override
    public String get_player_name() {
        return player_name;
    }


    @Override
    public Organism get_organism() {
        return organism;
    }

    @Override
    public LinkedList<Integer> get_move_queue() {
        return move_queue;
    }

    public void generate_and_queue() {}
}
