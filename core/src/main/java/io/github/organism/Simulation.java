package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    static int MODEL_STATES = 36;

    // 3 players * ((3 indicator variable per move * 6 moves) + energy + territory)
    static int MODEL_INPUTS = 3 * ((3 * 6) + 2);

    boolean show_summary_screen;
    boolean next_round_begin;
    boolean write_files;

    float between_round_pause = 0f;
    float between_round_pause_timer;

    HashMap<Point, String> player_names;
    HashMap<Point, Color> tournament_player_colors;

    ArrayList<Color> available_colors;
    int player_primary_index;
    String[] numerals = {"I", "II", "III", "IV", "V",
                        "VI", "VII", "VIII", "IX", "X",
                        "XI", "XII", "XIII", "XIV", "XV",
                        "XVI", "XVII", "XVIII", "XIX", "XX"};
    HashMap<Point, HMM> current_champions;

    int min_wins = 2;

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

    HashMap<Point, HMM> model_pool;
    HashMap<Point, Integer> win_records;

    int pool_size = 9;


    //MoveLogger move_logger;
    boolean log_written = false;


    int current_iteration;

    public Simulation(LabScreen screen, GameConfig c, int n) {

        lab_screen = screen;
        cfg = c;
        iterations = n;
        current_iteration = 1;
        player_primary_index = 0;
        map_center_x = lab_screen.game.VIRTUAL_WIDTH / 2f;
        map_center_y = lab_screen.game.VIRTUAL_HEIGHT / 2f;

        model_pool = new HashMap<>();
        win_records = new HashMap<>();
        current_champions = new HashMap<>();

        model_pool_display = new ModelPoolDisplay(lab_screen.game, this);

        round_summary = new RoundSummary(lab_screen.game, this);
        available_colors = new ArrayList<>();
        available_colors.addAll(Arrays.asList(lab_screen.game.player_colors));

        tournament_player_colors = new HashMap<>();
        player_names = new HashMap<>();
        //move_logger = new MoveLogger(lab_screen.game);

        show_summary_screen = false;
        next_round_begin = true;
        between_round_pause_timer = 0;

        write_files = true;
    }

    public void create_game_board() {

        if (available_colors.size() < 3){
            available_colors.addAll(Arrays.asList(lab_screen.game.player_colors));
        }

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
            model.ablate();
            player_primary_index += 1;
            Point player_id  = new Point(player_primary_index, 0);
            model.player_tournament_id = player_id;
            model_pool.put(player_id, model);
            win_records.put(player_id, 0);
        }
    }

    public void run_simulation() {

        System.out.println("first iteration");
        initialize_model_pool();

        // setup the first game
        create_game_board();
        create_players_from_model_pool();
        create_player_starts();
        current_game.create_player_summary_displays();
        current_game_orchestrator.run();
    }

    private void finish_this_round(Point winner_id) {

        System.out.println("iteration: " + current_iteration + "/" + iterations);

        int wins = win_records.get(winner_id);
        //noinspection Java8MapApi
        win_records.put(winner_id, wins + 1);


        round_summary.set_winner(winner_id);
        update_champions(winner_id);
        show_summary_screen = true;
    }

    private void setup_next_round(Point winner_id) {
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

        ArrayList<Point> player_ids = new ArrayList<>(model_pool.keySet());
        Collections.shuffle(player_ids, lab_screen.game.rng);


        for (int i=0; i<3; i++) {
            Point player_id = player_ids.get(i);
            HMM model = model_pool.remove(player_id);
            String name = player_names_array[player_id.x % player_names_array.length] + " " + numerals[player_id.y % numerals.length];

            Color color;
            if (tournament_player_colors.containsKey(player_id)){
                color = tournament_player_colors.get(player_id);
            } else {
                color = available_colors.remove(0);
            }

            current_game.create_bot_player(name, player_id, color, model);
            player_names.put(player_id, name);
            tournament_player_colors.put(player_id, color);
        }

        // reset diplomacy with newly created players
        current_game.diplomacy_graph = new DiplomacyGraph(this.lab_screen.game, current_game);
    }

    public void setup_next_round_models(Point winner_id) {

        //System.out.println("next round models");

        // add the winner back to the model pool
        BotPlayer winner = (BotPlayer) current_game.players.get(winner_id);
        HMM winner_model_copy = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
        winner_model_copy.set_weights(winner.model.transition_weights, winner.model.emission_weights);
        winner_model_copy.transition_bit_mask = winner.model.transition_bit_mask;
        model_pool.put(winner_id, winner_model_copy);

        // recycle colors and mark eliminated players in gray
        ArrayList<Point> to_remove = new ArrayList<>();
        for (Point p : current_game.all_player_ids) {
            if (!p.equals(winner_id)) {
                Color player_color = tournament_player_colors.get(p);
                if (player_color != null && !player_color.equals(Color.DARK_GRAY)) {
                    available_colors.add(player_color); // Recycle color
                }
                tournament_player_colors.put(p, Color.DARK_GRAY); // Mark eliminated players
                to_remove.add(p);
            }
        }
        for (Point p : to_remove) {
            model_pool.remove(p);
        }

        // add a model by averaging the last round models
        HMM offspring = get_last_round_offspring();
        offspring.transition_bit_mask = winner.model.transition_bit_mask;
        offspring.apply_transition_mask();
        Point offspring_player_id = new Point(
            winner_id.x,
            winner_id.y + 1
        );

        // avoid collisions from different inheritance paths
        while (tournament_player_colors.containsKey(offspring_player_id)){
            int y = offspring_player_id.y;
            offspring_player_id = new Point(
                winner_id.x,
                y + 1
            );
        }

        offspring.player_tournament_id = offspring_player_id;
        model_pool.put(offspring_player_id, offspring);
        win_records.put(offspring_player_id, 0);

        // add a brand new random model
        HMM model = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
        model.init_random_weights();
        player_primary_index++;
        Point player_id = new Point(player_primary_index, 0);
        model.player_tournament_id = player_id;
        model_pool.put(player_id, model);
        win_records.put(player_id, 0);

        //System.out.println("3: " + player_id[0] + " " + player_id[1]);
        //System.out.println(model.transition_weights);
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
        HashMap<Point, Double> weights = new HashMap<>();

        double total_territory = 0;
        for (Point player_id : current_game.players.keySet()) {
            double territory = current_game.players.get(player_id).get_organism().territory_vertex.get_unmasked_vertices();
            total_territory += territory;
        }

        for (Point player_id  : current_game.players.keySet()) {
            double territory = current_game.players.get(player_id).get_organism().territory_vertex.get_unmasked_vertices();
            weights.put(player_id, territory / total_territory);
        }

        // average the transition weights
        for (int i=0; i<states; i++){
            for (int j=0; j<states; j++){
                for (int k=0; k<inputs; k++){
                    for (Point p : current_game.players.keySet()) {
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
                    for (Point p: current_game.players.keySet()) {
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
        Point winner_id = current_game_orchestrator.test_victory_conditions();
        if (!current_game_orchestrator.paused) {

            // if victory conditions were met, finish up the last round and start the timer for the next one
            if (winner_id != null) {
                current_game_orchestrator.pause();

                if (!show_summary_screen) {
                    // end of round housekeeping
                    finish_this_round(winner_id);
                    show_summary_screen = true;
                    next_round_begin = false;
                    between_round_pause_timer = 0f;

                    if (current_iteration == iterations) {
                        write_champions_to_file();
                    }
                }
            }

            else {
                current_game_orchestrator.update_players();
                current_game_orchestrator.update_timers_and_flags();
            }
        }

        else {
            between_round_pause_timer += Gdx.graphics.getDeltaTime();

            if (between_round_pause_timer >= between_round_pause) {
                next_round_begin = true;
            }

            if (current_iteration < iterations & next_round_begin) {
                current_iteration ++;
                setup_next_round(winner_id); // this will set winner id back to null

                next_round_begin = false;
                show_summary_screen = false;
            }
        }
    }

    public void draw(){
        current_game.game.camera.update();
        current_game.render();
        model_pool_display.render();
        if (show_summary_screen & between_round_pause > 0 ) {
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
        current_game.dispose();
    }

    public void update_champions(Point winner_id) {
        int wins = win_records.get(winner_id);

        // add the winner of the last round to the list of champions
        if (wins >= min_wins & !current_champions.containsKey(winner_id)) {

            HMM winner_model_copy = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
            BotPlayer winning_player = (BotPlayer) current_game.players.get(winner_id);
            winner_model_copy.transition_weights = winning_player.model.transition_weights;
            winner_model_copy.emission_weights = winning_player.model.emission_weights;
            winner_model_copy.player_tournament_id = winning_player.tournament_id;

            current_champions.put(winner_id, winner_model_copy);
            System.out.println("adding: " + winner_id);

        }

        // remove last round losers if their record is not high
        ArrayList<Integer> leaderboard_wins = new ArrayList<>(win_records.values());
        leaderboard_wins.sort(Comparator.reverseOrder());

        ArrayList<Point> to_remove = new ArrayList<>();
        for (Point p : current_game.players.keySet()) {
            if (!p.equals(winner_id)) {
                int eliminated_player_wins = win_records.get(p);
                if (eliminated_player_wins < leaderboard_wins.get(2)) {
                    to_remove.add(p);
                }
            }
        }

        for (Point p : to_remove) {
            System.out.println("removing: " + p);
            current_champions.remove(p);
        }

        System.out.println("size: " + current_champions.size());
    }

    public void write_champions_to_file(){

        for (Point p : current_champions.keySet()) {
            lab_screen.game.file_handler.save_model(current_champions.get(p), p.x + "_" + p.y);
        }
    }

}
