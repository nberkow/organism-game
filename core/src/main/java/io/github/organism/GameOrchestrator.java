package io.github.organism;

import com.badlogic.gdx.Gdx;

import java.awt.Point;
import java.util.HashMap;

public class GameOrchestrator {

    final float VICTORY_THRESHOLD = 2/3f;
    public boolean finished;
    float turn_max;

    int turn = 0;
    GameBoard gameBoard;
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
        gameBoard = gb;
        total_territory = (float) gameBoard.universe_map.vertex_grid.get_unmasked_vertices();
        turn_max = total_territory * 3;
        player_territory = new HashMap<>();
        current_moves = new HashMap<>();
        for (Point p : gameBoard.players.keySet()) {
            player_territory.put(p, (float) gameBoard.players.get(p).getOrganism().territory_vertex.get_unmasked_vertices());
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
        for (Player p : gameBoard.players.values()) {
            Organism organism = p.getOrganism();
            if (organism != null){
                organism.updateResources();
            }
        }

        // dequeue an action from each player's queue and execute it
        if (execute_actions) {

            for (int i=0; i<queue_action_frequency; i++) {
                for (Point b : gameBoard.bot_player_ids) {
                    gameBoard.players.get(b).transition();
                }
            }

            for (int i=0; i<hmm_transition_frequency; i++) {
                for (Point b : gameBoard.bot_player_ids) {
                    gameBoard.players.get(b).generate_and_queue();
                }
            }

            dequeue_and_execute();

        }
    }

    public Point testVictoryConditions() {

        Point leader = null;
        float leader_territory = 0;

        int remaining_resources = gameBoard.count_resources();

        if (show_countdown) {
            resource_exhausted_countdown -= 1;
        }

        if (remaining_resources == 0) {
            show_countdown = true;
        }

        for (Point p : gameBoard.players.keySet()) {
            float p_territory = gameBoard.players.get(p).getOrganism().territory_vertex.get_unmasked_vertices();
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
        Player player = gameBoard.players.get(player_id);
        player.queue_move(button_val);
    }

    private void resolve_moves(HashMap<Point, Integer> allPlayerMoves) {
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

        for (int i = 0; i< gameBoard.allPlayerIds.size(); i++){
            int p = (i + turn) % 3;
            Player player = gameBoard.players.get(gameBoard.allPlayerIds.get(p));
            Organism organism = player.getOrganism();
            if (organism != null) {
                organism.update_income();
            }

            Player leftPlayer = gameBoard.players.get(gameBoard.allPlayerIds.get((p+2) % 3));
            Player rightPlayer = gameBoard.players.get(gameBoard.allPlayerIds.get((p+1) % 3));

            Player target;
            Integer move = allPlayerMoves.get(player.getTournamentId());
            
            if (move != null & organism != null) {
                if (move == 0 || move == 2) {

                    target = leftPlayer;
                    if (move == 0) {
                        target = rightPlayer;
                    }

                    player.getOrganism().expand(target);
                } else {
                    player.getOrganism().extract();
                }
            }
        }
    }

    private void dequeue_and_execute(){

        HashMap<Point, Integer> all_player_moves = new HashMap<>();
        for (int p = 0; p< gameBoard.allPlayerIds.size(); p++){
            Player player = gameBoard.players.get(gameBoard.allPlayerIds.get(p));
            Integer move = player.get_move();
            all_player_moves.put(player.getTournamentId(), move);
        }

        resolve_moves(all_player_moves);

        // this turn's moves set the diplomacy for the next turn
        gameBoard.diplomacyGraph.update_diplomacy(all_player_moves);
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
        for (Point p : gameBoard.allPlayerIds) {
            int move = gameBoard.players.get(p).get_most_recent_move();
            int territory = gameBoard.players.get(p).getOrganism().territory_vertex.get_unmasked_vertices();
            most_recent_move_stats.put("player" + i + "_move", String.valueOf(move));
            most_recent_move_stats.put("player" + i + "_territory", String.valueOf(territory));
            i++;
        }
        return most_recent_move_stats;
    }


    public void dispose() {
    }
}
