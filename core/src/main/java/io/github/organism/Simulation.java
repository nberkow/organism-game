package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Simulation {
    LabScreen lab_screen;
    GameBoard current_game;
    GameOrchestrator current_game_orchestrator;
    ModelPoolDisplay model_pool_display;

    RoundSummary round_summary;
    float map_center_x;
    float map_center_y;
    GameConfig cfg;
    int iterations;

    int MODEL_STATES = 36;
    int MODEL_INPUTS = 6;

    boolean show_summary_screen;
    boolean next_round_begin;

    int between_round_pause = 10;
    float between_round_pause_timer;

    HashMap<int [], String> player_names;
    HashMap<int [], Color> player_colors;
    int name_index;
    String[] numerals = {"I", "II", "III", "IV", "V",
                        "VI", "VII", "VIII", "IX", "X",
                        "XI", "XII", "XIII", "XIV", "XV",
                        "XVI", "XVII", "XVIII", "XIX", "XX"};

    String[]  player_names_array = {
        "Serpula lacrymans",
        "Turkey tail",
        "Hoof fungus",
        "Chicken of the woods",
        "Red-Belted Conk",
        "Honey fungus",
        "Splitgill mushroom",
        "Artist's bracket",
        "Oyster Mushroom",
        "Pleurotus",
        "Coniophora",
        "Dyer's polypore",
        "Shiitake",
        "Kretzschmaria deusta",
        "Sulphur tuft",
        "Porodaedalea pini",
        "Donkioporia expansa",
        "Northern cinnabar polypore",
        "Fibroporia vaillantii",
        "Dead man's fingers",
        "Phanerodontia chrysosporium",
        "Chaetomium",
        "China root",
        "Ceratocystis",
        "Bondarzewia berkeleyi"
    };

    HashMap<int [], HMM> model_pool;

    HashMap<int [], Integer> win_records;

    HashMap<int [], HMM> champions;

    int pool_size = 9;


    //MoveLogger move_logger;

    boolean log_written = false;


    int current_iteration;

    public Simulation(LabScreen screen, GameConfig c, int n) {

        lab_screen = screen;
        cfg = c;
        iterations = n;
        current_iteration = 0;
        name_index = 0;
        map_center_x = lab_screen.game.VIRTUAL_WIDTH / 2f;
        map_center_y = lab_screen.game.VIRTUAL_HEIGHT / 2f;

        model_pool = new HashMap<>();
        win_records = new HashMap<>();
        champions = new HashMap<>();
        model_pool_display = new ModelPoolDisplay(lab_screen.game, this);

        round_summary = new RoundSummary(lab_screen.game, this);
        player_colors = new HashMap<>();
        player_names = new HashMap<>();
        //move_logger = new MoveLogger(lab_screen.game);

        show_summary_screen = false;
        next_round_begin = true;
        between_round_pause_timer = 0;
    }

    public void create_game_board() {

        current_game = new GameBoard(lab_screen.game, cfg);
        current_game.void_distributor.distribute();
        current_game.resource_distributor.distribute();

        current_game_orchestrator = new GameOrchestrator(current_game);
        current_game.set_orchestrator(current_game_orchestrator);

        current_game.center_x = map_center_x;
        current_game.center_y = map_center_y;
    }

    public void create_player_starts() {
        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.player_start_positions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = current_game.player_start_assigner.randomize_starting_coords();
            current_game.player_start_assigner.assign_starting_hexes(starting_coords);
        }
    }

    public void initialize_model_pool() {
        /*
        start the pool with models given totally random weights
         */

        for (int i=0; i<pool_size; i++){
            HMM model = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
            model.init_random_weights();
            name_index += 1;
            int [] player_id  = new int[] {name_index, 0};
            model.player_tournament_id = player_id;
            model_pool.put(player_id, model);
            win_records.put(player_id, 0);
        }
    }

    public void run_simulation() {

        System.out.println("first iteration: " + current_iteration + "/" + iterations);
        initialize_model_pool();

        // setup the first game
        create_game_board();
        create_players_from_model_pool();
        create_player_starts();
        current_game.create_player_summary_displays();
        current_game_orchestrator.run();
    }

    private void finish_this_round(int[] winner_id) {

        System.out.println("iteration: " + current_iteration + "/" + iterations);
        int wins = win_records.get(winner_id);
        //noinspection Java8MapApi
        win_records.put(winner_id, wins + 1);
        round_summary.set_winner(winner_id);
        show_summary_screen = true;
    }

    private void setup_next_round(int[] winner_id) {
        setup_next_round_models(winner_id);

        current_game.dispose();
        current_game_orchestrator.dispose();

        create_game_board();
        create_players_from_model_pool();
        create_player_starts();
        current_game.create_player_summary_displays();
        current_game_orchestrator.run();
    }

    private void create_players_from_model_pool() {
        /*
        randomly remove 3 models from the pool

        use these to create 3 players on the current game board
         */

        ArrayList<int []> player_ids = new ArrayList<>(model_pool.keySet());
        Collections.shuffle(player_ids, lab_screen.game.rng);

        for (int i=0; i<3; i++) {
            int [] player_id = player_ids.get(i);
            HMM model = model_pool.remove(player_id);
            String name = player_names_array[player_id[0] % player_names_array.length] + " " + numerals[player_id[1] % numerals.length];
            Color color = lab_screen.game.player_colors[player_id[0] % lab_screen.game.player_colors.length];
            current_game.create_bot_player(name, player_id, color, model);
            player_names.put(player_id, name);
            player_colors.put(player_id, color);
        }

        // reset diplomacy with newly created players
        current_game.diplomacy_graph = new DiplomacyGraph(this.lab_screen.game, current_game);
    }

    public void setup_next_round_models(int[] winner_id) {

        // add the winner back to the model pool
        BotPlayer winner = (BotPlayer) current_game.players.get(winner_id);
        model_pool.put(winner_id, winner.model);

        // add a model by averaging the last round models
        HMM offspring = get_last_round_offspring();
        int [] offspring_player_id = new int [] {
            winner_id[0],
            (winner_id[1] + 1) % numerals.length
        };
        offspring.player_tournament_id = offspring_player_id;
        model_pool.put(offspring_player_id, offspring);

        // add a brand new random model
        HMM model = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
        model.init_random_weights();
        int[] player_id = new int[]{name_index, 0};
        name_index = (name_index + 1) % player_names_array.length;
        model.player_tournament_id = player_id;
        model_pool.put(player_id, model);

    }

    public HMM get_last_round_offspring() {

        /*
        in the future this can have more behaviors

        for now
        - create a new model by calculating the weighted average of
        the models from the last round (weighted by final territory)

         */


        // parse params and init datastructures
        int states = HMM.states;
        int inputs = HMM.inputs;

        double [][][] avg_transition_weights = new double[states][states][inputs];
        double [][][] avg_emission_weights = new double[states][4][inputs];

        // calculate the weights
        HashMap<int [], Double> weights = new HashMap<>();

        double total_territory = 0;
        for (int [] player_id : current_game.players.keySet()) {
            double territory = current_game.players.get(player_id).get_organism().territory_vertex.get_unmasked_vertices();
            total_territory += territory;
        }

        for (int [] player_id  : current_game.players.keySet()) {
            double territory = current_game.players.get(player_id).get_organism().territory_vertex.get_unmasked_vertices();
            weights.put(player_id, territory / total_territory);
        }

        // average the transition weights
        for (int i=0; i<states; i++){
            for (int j=0; j<states; j++){
                for (int k=0; k<inputs; k++){
                    for (int [] p : current_game.players.keySet()) {
                        BotPlayer player = (BotPlayer) current_game.players.get(p);
                        double s = player.model.transition_weights[i][j][k];
                        double w = weights.get(p);
                        avg_transition_weights[i][j][k] += s * w;
                    }
                }
            }
        }

        // average the emission weights
        for (int i=0; i<states; i++){
            for (int j=0; j<4; j++){
                for (int k=0; k<inputs; k++){
                    for (int [] p: current_game.players.keySet()) {
                        BotPlayer player = (BotPlayer) current_game.players.get(p);
                        double s = player.model.emission_weights[i][j][k];
                        double w = weights.get(p);
                        avg_emission_weights[i][j][k] += s * w;
                    }
                }
            }
        }

        HMM offspring = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
        offspring.set_weights(avg_transition_weights, avg_emission_weights);

        return offspring;
    }


    public void logic(){
        if (!current_game_orchestrator.paused) {
            int [] winner_id = current_game_orchestrator.test_victory_conditions();

            // if victory conditions were met, finish up the last round and start the timer for the next one
            if (winner_id != null) {
                current_game_orchestrator.pause();

                if (!show_summary_screen) {
                    // end of round housekeeping
                    finish_this_round(winner_id);
                    show_summary_screen = true;
                    next_round_begin = false;
                    between_round_pause_timer = 0f;
                }

                between_round_pause_timer += Gdx.graphics.getDeltaTime();
                if (between_round_pause_timer >= between_round_pause) {
                    show_summary_screen = false;
                    next_round_begin = true;
                }

                if (current_iteration < iterations & next_round_begin) {
                    current_iteration ++;
                    setup_next_round(winner_id); // this will set winner id back to null
                    next_round_begin = false;
                }

            }

            else {
                current_game_orchestrator.update_players();
                HashMap<String, String> logger_stats = current_game_orchestrator.get_logger_stats();
                logger_stats.put("game", String.valueOf(current_iteration));
                //move_logger.log_move(logger_stats);
                current_game_orchestrator.update_timers_and_flags();
            }

            /*if (iterations == current_iteration && !log_written) {
                move_logger.write_moves("moves");
            }*/
        }
    }

    public void draw(){
        current_game.game.camera.update();
        current_game.render();
        model_pool_display.render();
        if (show_summary_screen) {
            round_summary.render();
        }
    }

    public void render(){
        logic();
        draw();
    }

    public void dispose() {
        model_pool.clear();
        win_records.clear();
        champions.clear();
        current_game.dispose();
    }

}
