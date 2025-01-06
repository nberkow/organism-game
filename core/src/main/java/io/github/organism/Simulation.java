package io.github.organism;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Simulation {
    LabScreen lab_screen;
    GameBoard current_game;
    GameOrchestrator current_game_orchestrator;
    float map_center_x;
    float map_center_y;
    GameConfig cfg;
    int iterations;
    MoveLogger move_logger;

    boolean log_written = false;
    int current_iteration;

    public Simulation(LabScreen screen, GameConfig c, int n) {
        lab_screen = screen;
        cfg = c;
        iterations = n;
        current_iteration = 0;
        map_center_x = lab_screen.game.VIRTUAL_WIDTH / 2f;
        map_center_y = lab_screen.game.VIRTUAL_HEIGHT / 2f;
        move_logger = new MoveLogger(lab_screen.game);
    }

    public void create_game() {
        current_game = new GameBoard(lab_screen.game, cfg);
        current_game.void_distributor.distribute();
        current_game.resource_distributor.distribute();

        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.player_start_positions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = current_game.player_start_assigner.randomize_starting_coords();
            current_game.player_start_assigner.assign_starting_hexes(starting_coords);
        }

        current_game_orchestrator = new GameOrchestrator(current_game);
        current_game.set_orchestrator(current_game_orchestrator);

        current_game.center_x = map_center_x;
        current_game.center_y = map_center_y;
    }

    public void set_random_models() {
        for (String player_name : current_game.bot_player_names){
            HMM model = new HMM(current_game, 6, 0.5f, 6);
            BotPlayer bot = (BotPlayer) current_game.players.get(player_name);
            bot.model = model;
        }
    }

    public void set_next_round_models(HashMap<String, double[][][]> weights) {
        boolean first = true;

        for (String player_name : current_game.bot_player_names) {
            HMM model = new HMM(current_game, 6, 0.5f, 6);
            if (first) {
                model.transition_weights = weights.get("transition");
                model.emission_weights = weights.get("emission");
                first = false;
            }
            BotPlayer bot = (BotPlayer) current_game.players.get(player_name);
            bot.model = model;
        }
    }

    public void run_simulation() {
        current_game_orchestrator.run();
    }

    public void setup_next_round() {
        HashMap<String, double[][][]> model_weights = get_average_weights();
        set_next_round_models(model_weights);
        create_game();
    }

    public HashMap<String, double [][][]> get_average_weights() {
        /*
        in the future this can have more behaviors

        for now
        - create a new model by calculating the weighted average of
        the models from the last round (weighted by final territory)
        - create two new random models
         */


        // parse params and init datastructures
        BotPlayer example_player = (BotPlayer) current_game.players.values().iterator().next();
        int states = example_player.model.states;
        int inputs = example_player.model.inputs;

        double [][][] transition_weights = new double[states][states][inputs];
        double [][][] emission_weights = new double[states][4][inputs];

        // calculate the weights
        HashMap<String, Double> weights = new HashMap<>();

        double total_territory = 0;
        for (String p : current_game.players.keySet()) {
            double territory = current_game.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            total_territory += territory;
        }

        for (String p : current_game.players.keySet()) {
            double territory = current_game.players.get(p).get_organism().territory_vertex.get_unmasked_vertices();
            weights.put(p, territory / total_territory);
        }

        // average the transition weights
        for (int i=0; i<states; i++){
            for (int j=0; j<states; j++){
                for (int k=0; k<inputs; k++){
                    for (String p : current_game.players.keySet()) {
                        BotPlayer player = (BotPlayer) current_game.players.get(p);
                        double s = player.model.transition_weights[i][j][k];
                        double w = weights.get(p);
                        transition_weights[i][j][k] += s * w;
                    }
                }
            }
        }

        // average the emission weights
        for (int i=0; i<states; i++){
            for (int j=0; j<4; j++){
                for (int k=0; k<inputs; k++){
                    for (String p : current_game.players.keySet()) {
                        BotPlayer player = (BotPlayer) current_game.players.get(p);
                        double s = player.model.emission_weights[i][j][k];
                        double w = weights.get(p);
                        emission_weights[i][j][k] += s * w;
                    }
                }
            }
        }

        HashMap<String, double[][][]> model_weights = new HashMap<>();
        model_weights.put("transition", transition_weights);
        model_weights.put("emission", emission_weights);

        return model_weights;
    }


    public void logic(){
        if (!current_game_orchestrator.paused) {
            if (current_game_orchestrator.test_victory_conditions()) {
                current_game_orchestrator.pause();
                //TODO: post game data collection visualization

                // save the current model (commented for debug)
                if (current_game.rng.nextFloat() < 0.1) {
                    BotPlayer b = (BotPlayer) current_game.players.get(current_game.bot_player_names.get(0));
                    lab_screen.game.file_handler.save_model(b.model, "saprophyte" + current_iteration);
                }

                if (current_iteration < iterations) {
                    setup_next_round();
                    current_iteration ++;
                    run_simulation();
                }
            }

            else {
                current_game_orchestrator.update_players();
                HashMap<String, String> logger_stats = current_game_orchestrator.get_logger_stats();
                logger_stats.put("game", String.valueOf(current_iteration));
                move_logger.log_move(logger_stats);
                current_game_orchestrator.update_timers_and_flags();
            }

            if (iterations == current_iteration && !log_written) {
                move_logger.write_moves("moves");

            }
        }
    }

    public void draw(){
        current_game.game.camera.update();
        current_game.render();
    }

    public void render(){
        logic();
        draw();
    }

}
