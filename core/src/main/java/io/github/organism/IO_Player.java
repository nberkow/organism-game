package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.LinkedList;

public class IO_Player  implements Player {

    String player_name;


    int [] tournament_id; // id in tournament or other large player collection
    int game_index; // index within game

    Color color;
    Organism organism;

    GameBoard game_board;
    LinkedList<Integer> move_queue;

    int most_recent_move;
    ActionHistory move_history;

    boolean player_2 = false;

    public IO_Player(GameBoard gb, String name, int idx, int [] id, Organism org, boolean p2, Color c){

        game_board = gb;
        player_name = name;
        game_index = idx;
        tournament_id = id;
        organism = org;
        player_2 = p2;
        color = c;
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
            int m = move_queue.remove();
            most_recent_move = m;
            return m;
        }
        return null;
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

    /**
     * @return
     */
    @Override
    public int get_most_recent_move() {
        return most_recent_move;
    }

    /**
     *
     */
    @Override
    public void transition() {
        // interface consistency. used for bot players
    }
}
