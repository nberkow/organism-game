package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class IO_Player  implements Player {

    String player_name;

    int index;

    Color color;
    Organism organism;

    GameBoard game_board;
    LinkedList<Integer> move_queue;

    int most_recent_move;
    ActionHistory move_history;

    HashMap<String, Player> diplomacy;
    boolean player_2 = false;

    public IO_Player(GameBoard gb, String name, int idx, Organism org, boolean p2, Color c){

        game_board = gb;
        player_name = name;
        index = idx;
        organism = org;
        player_2 = p2;
        color = c;
        move_queue = new LinkedList<>();
        move_history = new ActionHistory(game_board);
        diplomacy = new HashMap<>();
        diplomacy.put("enemy", null);
        diplomacy.put("ally", null);
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
        return index;
    }

    /**
     * @return
     */
    @Override
    public HashMap<String, Player> get_diplomacy() {
        return diplomacy;
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
