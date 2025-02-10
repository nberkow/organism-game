package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.LinkedList;

public class BotPlayer implements Player{

    public LinkedList<Integer> move_queue;
    //public ActionHistory move_history;

    public GameBoard game_board;
    public int game_index; // index within a single game
    public Point tournament_id; // id in tournament or other large player collection

    public Color color;
    public String player_name;

    public HMM model;

    public Organism organism;

    int most_recent_move;

    Point ally_id;

    public BotPlayer(GameBoard gb, String name, int idx, Point id, Organism org, HMM mod, Color c){

        game_board = gb;
        color = c;
        player_name = name;
        tournament_id = id;
        game_index = idx;
        organism = org;
        model = mod;
        move_queue = new LinkedList<>();
        ally_id = null;

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

    /**
     *
     */


    public float [] gather_inputs(){
        float [] hmm_inputs = new float [Simulation.MODEL_INPUTS];

        // how many moves to consider from each players queue
        int move_queue_depth = 6;

        // move queue depth plus two states for each player's energy and territory
        int register_size = move_queue_depth + 2;

        // expand targets are not visible in player queues, so this can be expand, extract or null
        int option_per_move = 3;

        // add player stats in order, starting with self
        for (int i=0; i<3; i++) {
            int p = ((i + game_index) % 3);
            int register_index = p * (register_size);

            Point player_id = game_board.all_player_ids.get(p);
            Player player = game_board.players.get(player_id);
            Organism organism = player.get_organism();

            // player's energy
            hmm_inputs[register_index] = (float) organism.energy / organism.MAX_ENERGY;

            // player's territory
            hmm_inputs[register_index + 1] = organism.territory_vertex.get_unmasked_vertices();

            // indicator variable for each players move queue
            // 0 or 1 for each possibility (0,1 or Null), for
            // -- 0 and 2 both mean expand (register 0)
            // -- 1 means extract

            LinkedList<Integer> move_queue = player.get_move_queue();
            int pos;

            int move_indicator = 2; // value for null
            for (int q=0; q<move_queue_depth; q++) {
                if (q < move_queue.size()) {
                    move_indicator = move_queue.get(q) % 2; // 0 and 2 both mean expand. 1 means extract
                }
                for (int m=0; m<option_per_move; m++) {
                    pos = register_index + 2 + (q * option_per_move) + m;
                    hmm_inputs[pos] = 0;
                    if (move_indicator == m) {
                        hmm_inputs[pos] = 1;
                    }
                }
            }
        }

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

    @Override
    public void dispose() {
        move_queue.clear();
        game_board = null;
        model.dispose();
        organism.dispose();
    }

    /**
     * @return
     */
    @Override
    public Point get_tournament_id() {
       return tournament_id;
    }

    /**
     * @return
     */
    @Override
    public Point get_ally_id() {
        return ally_id;
    }

    public void set_ally_id(Point p) {
        ally_id = p;
    }
}
