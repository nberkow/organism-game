package io.github.organism;

import com.badlogic.gdx.Gdx;

import java.awt.Point;
import java.util.HashMap;

public class GameOrchestrator {

    final float VICTORY_THRESHOLD = 2/3f;
    float turn_max;

    int turn = 0;
    GameBoard game_board;
    double action_time = 1d;
    double queue_time = action_time / 10;

    double hmm_transition_time = action_time / 50;

    boolean execute_actions = false;
    boolean queue_bot_actions = false;
    private boolean hmm_transition = false;
    double action_clock = 0d;
    double queue_clock = 0d;
    double hmm_transition_clock = 0d;
    boolean paused = true;
    HashMap<Point, Float> player_territory;
    HashMap<Point, Integer> current_moves;
    float total_territory;

    public GameOrchestrator(GameBoard gb) {
        game_board = gb;
        total_territory = (float) game_board.universe_map.vertex_grid.get_unmasked_vertices();
        turn_max = total_territory * 3;
        player_territory = new HashMap<>();
        current_moves = new HashMap<>();
        for (Point p : game_board.players.keySet()) {
            player_territory.put(p, (float) game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices());
        }
    }

    public void update_timers_and_flags() {

        if (turn >= turn_max) {
            return;
        }

        action_clock += Gdx.graphics.getDeltaTime();
        execute_actions = false;
        if (action_clock > action_time){
            execute_actions = true;
            turn ++;
            action_clock = action_clock % action_time;
        }

        queue_clock += Gdx.graphics.getDeltaTime();
        queue_bot_actions = false;
        if (queue_clock > queue_time){
            queue_bot_actions = true;
            queue_clock = queue_clock % queue_time;
        }

        hmm_transition_clock += Gdx.graphics.getDeltaTime();
        hmm_transition = false;
        if (hmm_transition_clock > hmm_transition_time){
            hmm_transition = true;
            hmm_transition_clock = hmm_transition_clock % hmm_transition_time;
        }
    }

    public void update_players() {
        for (Player p : game_board.players.values()) {
            p.get_organism().update_resources();
        }

        if (hmm_transition) {
            for (Point b : game_board.bot_player_ids){
                game_board.players.get(b).transition();
            }
        }

        if (queue_bot_actions) {
            for (Point b : game_board.bot_player_ids){
                game_board.players.get(b).generate_and_queue();
            }
        }

        // dequeue an action from each player's queue and execute it
        if (execute_actions) {
            dequeue_and_execute();

        }
    }

    public Point test_victory_conditions() {

        Point leader = null;
        float leader_territory = 0;

        for (Point p : game_board.players.keySet()) {
            float p_territory = game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            player_territory.put(p, p_territory);
            if (p_territory > leader_territory) {
                leader = p;
                leader_territory = p_territory;
            }

            if (p_territory / total_territory >= VICTORY_THRESHOLD) {
                return p;
            }
        }

        if (turn >= turn_max) {
            return leader;
        }

        return null;
    }

    public void enqueue_action(Point player_id, int button_val) {
        // run one of the three actions depending on the button val
        Player player = game_board.players.get(player_id);
        player.queue_move(button_val);
    }

    private void resolve_moves(HashMap<Point, Integer> all_player_moves) {
        /*
        Resolve moves based on all player responses

        Extract
        - 1 player chooses extract - energy based on resources
        - 2 players choose extract - energy based on max(each resource)
        - 3 players choose extract - energy based on min(each resources)

        Expand - cost to claim vertex
        - free territory cost: 3
        - remove enemy player cost 9, take vertex cost 3
        - remove extracting player cost: 3, take vertex cost 3
        - remove player attacking other player (flanked player) cost: 1

         */

        // move execution rotates order

        for (int i=0; i<game_board.all_player_ids.size(); i++){
            int p = (i + turn) % 3;
            Player player = game_board.players.get(game_board.all_player_ids.get(p));
            player.get_organism().update_income();

            Player left_player = game_board.players.get(game_board.all_player_ids.get((p+2) % 3));
            Player right_player = game_board.players.get(game_board.all_player_ids.get((p+1) % 3));

            Player target;
            Integer move = all_player_moves.get(player.get_tournament_id());
            if (move != null) {
                if (move == 0 || move == 2) {

                    target = left_player;
                    if (move == 0) {
                        target = right_player;
                    }

                    player.get_organism().expand(target);
                } else {
                    player.get_organism().extract();
                }
            }
        }
    }

    private void dequeue_and_execute(){
        HashMap<Point, Integer> all_player_moves = new HashMap<>();
        for (int p=0; p<game_board.all_player_ids.size(); p++){
            Player player = game_board.players.get(game_board.all_player_ids.get(p));
            Integer move = player.get_move();
            all_player_moves.put(player.get_tournament_id(), move);
        }

        resolve_moves(all_player_moves);

        // this turn's moves set the diplomacy for the next turn
        game_board.diplomacy_graph.update_diplomacy(all_player_moves);
    }

    public void run(){
        paused = false;
    }

    public void pause() {
        paused = true;
    }

    public HashMap<String, String> get_logger_stats() {
        HashMap<String, String> most_recent_move_stats = new HashMap<>();
        most_recent_move_stats.put("turn", String.valueOf(turn));

        int i = 1;
        for (Point p : game_board.all_player_ids) {
            int move = game_board.players.get(p).get_most_recent_move();
            int territory = game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            most_recent_move_stats.put("player" + i + "_move", String.valueOf(move));
            most_recent_move_stats.put("player" + i + "_territory", String.valueOf(territory));
            i++;
        }
        return most_recent_move_stats;
    }


    public void dispose() {
    }
}
