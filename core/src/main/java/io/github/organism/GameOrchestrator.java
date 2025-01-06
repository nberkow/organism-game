package io.github.organism;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;

public class GameOrchestrator {

    final float VICTORY_THRESHOLD = 2/3f;
    float turn_max;

    int turn = 0;
    GameBoard game_board;
    double action_time = 1d;
    double queue_time = action_time / 4;

    double hmm_transition_time = action_time / 50;

    boolean execute_actions = false;
    boolean queue_bot_actions = false;
    private boolean hmm_transition = false;
    double action_clock = 0d;
    double queue_clock = 0d;
    double hmm_transition_clock = 0d;
    boolean paused = true;
    HashMap<String, Float> player_territory;
    HashMap<String, Integer> current_moves;
    float total_territory;

    final int VERTEX_COST_REMOVE_ENEMY_PLAYER = 9;
    final int VERTEX_COST_REMOVE_EXTRACTING_PLAYER = 9;
    final int VERTEX_COST_REMOVE_FLANKED_PLAYER = 1;
    final int VERTEX_COST_TAKE_VERTEX = 3;


    public GameOrchestrator(GameBoard gb) {
        game_board = gb;
        total_territory = (float) game_board.universe_map.vertex_grid.get_unmasked_vertices();
        turn_max = total_territory * 36;
        player_territory = new HashMap<>();
        current_moves = new HashMap<>();
        for (String p : game_board.players.keySet()) {
            player_territory.put(p, (float) game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices());
        }
    }

    public void update_timers_and_flags() {
        turn ++;
        action_clock += Gdx.graphics.getDeltaTime();
        execute_actions = false;
        if (action_clock > action_time){
            execute_actions = true;
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
            for (String b : game_board.bot_player_names){
                game_board.players.get(b).transition();
            }
        }

        if (queue_bot_actions) {
            for (String b : game_board.bot_player_names){
                game_board.players.get(b).generate_and_queue();
            }
        }

        // dequeue an action from each player's queue and execute it
        if (execute_actions) {
            dequeue_and_execute();
        }
    }

    public boolean test_victory_conditions() {

        if (turn > turn_max) {
            return true;
        }

        for (String p : game_board.players.keySet()) {
            float p_territory = game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            player_territory.put(p, p_territory);

            if (p_territory / total_territory >= VICTORY_THRESHOLD) {
                return true;
            }
        }

        return false;
    }

    public void enqueue_action(String player_name, int button_val) {
        // run one of the three actions depending on the button val
        Player player = game_board.players.get(player_name);
        player.queue_move(button_val);
    }

    private void resolve_moves(Integer[] all_player_moves) {
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
        - remove player attacking other player cost: 1

         */

        // move execution rotates order

        for (int p=0; p<game_board.all_player_names.size(); p++){
            Player player = game_board.players.get(game_board.all_player_names.get((p + turn) % 3));
            player.get_organism().update_income();
            int move = all_player_moves[p];

            if (move == 0 || move == 2) {
                Player enemy_player = player.get_diplomacy().get("enemy");
                player.get_organism().expand(enemy_player);
            }

            else {
                player.get_organism().extract();
            }

        }
    }

    private void dequeue_and_execute(){

        Integer [] all_player_moves = new Integer[3];
        for (int p=0; p<game_board.all_player_names.size(); p++){
            Player player = game_board.players.get(game_board.all_player_names.get(p));
            Integer move = player.get_move();
            all_player_moves[p] = move;
        }

        update_diplomacy(all_player_moves);
        resolve_moves(all_player_moves);

    }

    private void update_diplomacy(Integer[] all_player_moves) {
        /*
        If all players choose expand (cooperate) all players are neutral. this clears
        enemy relationships

        If exactly two players choose expand (cooperate) they become allies
        until one attacks the other

        exterminate (defect) is directed against one opponent. enemy relationships
        do not have to be symmetrical.

        exterminate(player) sets that player to enemy

        */

        for (int p=0; p<3; p++) {
            Player player = game_board.players.get(game_board.all_player_names.get(p));
            Player left_player = game_board.players.get(game_board.all_player_names.get((p+2) % 3));
            Player right_player = game_board.players.get(game_board.all_player_names.get((p+1) % 3));
            HashMap<String, Player> diplomacy = player.get_diplomacy();

            int move = all_player_moves[p];
            if (move == 0) {
                diplomacy.put("enemy", left_player);
                if (diplomacy.get("ally") == left_player) {
                    diplomacy.put("ally", null);
                }
            }

            if (move == 2) {
                diplomacy.put("enemy", right_player);
                if (diplomacy.get("ally") == right_player) {
                    diplomacy.put("ally", null);
                }
            }

            if (move == 1) {
                if (all_player_moves[(p+2) % 3] == 1 && all_player_moves[(p+1) % 3] == 1) {
                    player.get_diplomacy().put("enemy", null);
                    player.get_diplomacy().put("ally", null);
                } else {
                    if (all_player_moves[(p+2) % 3] == 1) {
                        player.get_diplomacy().put("ally", left_player);
                    }
                    if (all_player_moves[(p+1) % 3] == 1) {
                        player.get_diplomacy().put("ally", right_player);
                    }
                }
            }
        }
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
        for (String p : game_board.all_player_names) {
            int move = game_board.players.get(p).get_most_recent_move();
            int territory = game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            most_recent_move_stats.put("player" + i + "_move", String.valueOf(move));
            most_recent_move_stats.put("player" + i + "_territory", String.valueOf(territory));
            i++;
        }
        return most_recent_move_stats;
    }


}
