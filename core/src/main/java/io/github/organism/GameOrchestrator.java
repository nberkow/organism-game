package io.github.organism;

import com.badlogic.gdx.Gdx;

import java.awt.Point;
import java.util.HashMap;

public class GameOrchestrator {

    final float VICTORY_THRESHOLD = 2/3f;
    public boolean finished;
    float turn_max;

    int turn = 0;
    GameBoard game_board;
    double base_action_time = 1d;
    double action_time = base_action_time;

    double queue_action_frequency = 10;

    double hmm_transition_frequency = 50;

    boolean execute_actions = false;
    double action_clock = 0d;
    boolean paused = true;
    HashMap<Point, Float> player_territory;
    HashMap<Point, Integer> current_moves;
    float total_territory;
    int resource_exhausted_countdown = 36;
    boolean show_countdown;

    public GameOrchestrator(GameBoard gb) {
        game_board = gb;
        total_territory = (float) game_board.universe_map.vertex_grid.get_unmasked_vertices();
        turn_max = total_territory * 3;
        player_territory = new HashMap<>();
        current_moves = new HashMap<>();
        for (Point p : game_board.players.keySet()) {
            player_territory.put(p, (float) game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices());
        }
        finished = false;
    }

    public void update_speed(float speed){
        action_time = base_action_time / speed;
    }

    public void updateTimersAndFlags() {

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

    }

    public void updatePlayers() {
        for (Player p : game_board.players.values()) {
            p.get_organism().update_resources();
        }

        // dequeue an action from each player's queue and execute it
        if (execute_actions) {

            for (int i=0; i<queue_action_frequency; i++) {
                for (Point b : game_board.bot_player_ids) {
                    game_board.players.get(b).transition();
                }
            }

            for (int i=0; i<hmm_transition_frequency; i++) {
                for (Point b : game_board.bot_player_ids) {
                    game_board.players.get(b).generate_and_queue();
                }
            }

            dequeue_and_execute();

        }
    }

    public Point testVictoryConditions() {

        Point leader = null;
        float leader_territory = 0;

        int remaining_resources = game_board.count_resources();

        if (show_countdown) {
            resource_exhausted_countdown -= 1;
        }

        if (remaining_resources == 0) {
            show_countdown = true;
        }

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

        if (resource_exhausted_countdown <= 0) {
            return leader;
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

        for (int i = 0; i<game_board.allPlayerIds.size(); i++){
            int p = (i + turn) % 3;
            Player player = game_board.players.get(game_board.allPlayerIds.get(p));
            player.get_organism().update_income();

            Player left_player = game_board.players.get(game_board.allPlayerIds.get((p+2) % 3));
            Player right_player = game_board.players.get(game_board.allPlayerIds.get((p+1) % 3));

            Player target;
            Integer move = all_player_moves.get(player.getTournamentId());
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
        for (int p = 0; p<game_board.allPlayerIds.size(); p++){
            Player player = game_board.players.get(game_board.allPlayerIds.get(p));
            Integer move = player.get_move();
            all_player_moves.put(player.getTournamentId(), move);
        }

        resolve_moves(all_player_moves);

        // this turn's moves set the diplomacy for the next turn
        game_board.diplomacyGraph.update_diplomacy(all_player_moves);
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
        for (Point p : game_board.allPlayerIds) {
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
