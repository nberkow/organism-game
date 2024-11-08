package io.github.organism;

import com.badlogic.gdx.Gdx;

public class GameOrchestrator {

    GameBoard game_board;
    double action_time = 1d;
    double queue_time = action_time / 40;
    boolean execute_actions = false;
    boolean queue_bot_actions = false;
    double action_clock = 0d;
    double queue_clock = 0d;
    public GameOrchestrator(GameBoard gb) {
        game_board = gb;
    }

    public void update_timers_and_flags() {

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
    }

    public void update_players() {
        for (Player p : game_board.players.values()) {
            p.get_organism().update_resources();
            p.get_organism().update_income();
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

    private void dequeue_and_execute(){

        for (String p : game_board.all_player_names){
            Player player = game_board.players.get(p);
            Organism organism = player.get_organism();
            Integer move = player.get_move();
            organism.make_move(move);
        }

    }
}
