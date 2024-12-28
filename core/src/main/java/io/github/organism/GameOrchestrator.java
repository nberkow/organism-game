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
    float total_territory;


    public GameOrchestrator(GameBoard gb) {
        game_board = gb;
        total_territory = (float) game_board.universe_map.vertex_grid.get_unmasked_vertices();
        turn_max = total_territory * 3;
        player_territory = new HashMap<>();
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
            p.get_organism().update_income();
        }

        if (hmm_transition) {
            for (String b : game_board.bot_player_names){
                if (game_board.rng.nextFloat() > 0.5) {
                    game_board.players.get(b).transition();
                }
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

    public boolean update_victory_conditions() {
        System.out.println(turn);
        System.out.println(total_territory);

        if (turn > turn_max) {
            return true;
        }

        for (String p : game_board.players.keySet()) {
            float p_territory = game_board.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            player_territory.put(p, p_territory);
            System.out.println(p + ":\t" + p_territory);
            if (p_territory / total_territory > VICTORY_THRESHOLD) {
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

    private void dequeue_and_execute(){

        for (String p : game_board.all_player_names){
            Player player = game_board.players.get(p);
            Organism organism = player.get_organism();
            Integer move = player.get_move();
            organism.make_move(move);
        }

    }
    public void run(){
        paused = false;
    }

    public void pause() {
        paused = true;
    }
}
