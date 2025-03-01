package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Simulation implements GameMode{
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

    float mutation_rate;
    boolean show_summary_screen;
    boolean next_round_begin;
    boolean write_files;

    int max_models_to_save = 3;

    boolean kill = false;

    boolean silent = true;

    float between_round_pause = 2f;
    float between_round_pause_timer;

    HashMap<Point, String> player_names;
    HashMap<Point, Color> tournament_player_colors;

    ArrayList<Color> available_colors;
    int player_primary_index;
    String[] numerals = {"I", "II", "III", "IV", "V",
                        "VI", "VII", "VIII", "IX", "X",
                        "XI", "XII", "XIII", "XIV", "XV",
                        "XVI", "XVII", "XVIII", "XIX", "XX"};

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
    HashMap<Point, ArrayList<Point>> win_records;
    HashMap<Point, ArrayList<Integer>> win_record_turns;

    WinRecordGraph win_record_graph;
    int pool_size = 9;


    //MoveLogger move_logger;
    //boolean log_written = false;


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
        win_record_turns = new HashMap<>();

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
        mutation_rate = (float) Math.pow(1f/MODEL_STATES, 3);

        write_files = screen.write_files;

        float graph_width = (float) lab_screen.game.VIRTUAL_WIDTH / 2;
        float graph_height = graph_width * .9f;

        win_record_graph = new WinRecordGraph(
            lab_screen.game, this,
        (lab_screen.game.VIRTUAL_WIDTH - graph_width)/2,
        (lab_screen.game.VIRTUAL_HEIGHT - graph_height)/2,
                graph_width,
                graph_height
        );
    }

    public void run_silent(){

        // setup and run one game until victory
        create_game_board();
        create_players_from_model_pool();
        create_player_starts();
        current_game.create_player_summary_displays();
        current_game_orchestrator.run();
        while (!current_game_orchestrator.finished) {
            logic();
        }
        Point winner_id = current_game_orchestrator.test_victory_conditions();

        finish_this_round(winner_id);
        setup_next_round_models(winner_id);

    }

    public void create_game_board() {

        if (available_colors.size() < 3){
            available_colors.addAll(Arrays.asList(lab_screen.game.player_colors));
        }

        current_game = new GameBoard(lab_screen.game, cfg, lab_screen);
        current_game.void_distributor.distribute();
        current_game.resource_distributor.distribute();

        current_game_orchestrator = new GameOrchestrator(current_game);
        current_game.set_orchestrator(current_game_orchestrator);

        current_game.center_x = map_center_x;
        current_game.center_y = map_center_y;
        current_game.show_player_summary = true;
        current_game.show_diplomacy = true;
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

            ArrayList<Point> wins = new ArrayList<>();
            ArrayList<Integer> turns = new ArrayList<>();

            wins.add(new Point(0, 0));
            turns.add(0);

            win_records.put(player_id, wins);
            win_record_turns.put(player_id, turns);
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
        current_game_orchestrator.update_speed(cfg.gameplay_settings.get("speed"));
        current_game_orchestrator.run();
    }

    private void finish_this_round(Point winner_id) {

        System.out.println("iteration: " + current_iteration + "/" + iterations);

        for (Point p : current_game.players.keySet()) {
            Point prev_rec = win_records.get(p).get(0);
            Point rec = new Point(prev_rec.x, prev_rec.y);
            if (p == winner_id){
                rec.x += 1;
            }
            else {
                rec.y += 1;
            }
            win_records.get(p).add(0, rec);
            win_record_turns.get(p).add(0, current_iteration);
        }

        round_summary.set_winner(winner_id);
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
        current_game_orchestrator.update_speed(cfg.gameplay_settings.get("speed"));
        current_game_orchestrator.run();
    }

    private void create_players_from_model_pool() {
        /*
        randomly select 3 models from the pool

        use these to create 3 players on the current game board
         */

        ArrayList<Point> player_ids = new ArrayList<>(model_pool.keySet());
        Collections.shuffle(player_ids, lab_screen.game.rng);

        for (int i=0; i<3; i++) {
            Point player_id = player_ids.get(i);
            HMM model = model_pool.get(player_id);
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

    public void prune_model_pool(){

        ArrayList<Point> to_remove = new ArrayList<>();
        HashMap<Float, ArrayList<Point>> models_by_win_margin = new HashMap<>();

        for (Point p : model_pool.keySet()) {
            Point rec = win_records.get(p).get(0);
            float margin = (rec.x - rec.y);
            if (margin < 1 & rec.y > 0) {
                // recycle colors and mark eliminated players in gray
                to_remove.add(p);
                Color player_color = tournament_player_colors.get(p);
                if (player_color != null && !player_color.equals(Color.DARK_GRAY)) {
                    available_colors.add(player_color); // Recycle color
                }
                tournament_player_colors.put(p, Color.DARK_GRAY); // Mark eliminated players
            }
            if (!models_by_win_margin.containsKey(margin)) {
                models_by_win_margin.put(margin, new ArrayList<>());
            }
        }

        System.out.println("removing " + to_remove.size());
        for (Point p : to_remove){
            model_pool.remove(p);
        }
        System.out.println("new size: " + model_pool.size());

    }

    public void add_new_random_models(int n){
        // add new random models

        System.out.println("adding random " + n);
        for (int i=0; i<n; i++) {
            HMM model = new HMM(lab_screen.game, MODEL_STATES, MODEL_INPUTS);
            model.init_random_weights();
            player_primary_index++;
            Point player_id = new Point(player_primary_index, 0);
            model.player_tournament_id = player_id;
            model_pool.put(player_id, model);
            ArrayList<Point> rec = new ArrayList<>();
            rec.add(new Point(0, 0));
            win_records.put(player_id, rec);
            ArrayList<Integer> turn = new ArrayList<>();
            turn.add(current_iteration);
            win_record_turns.put(player_id, turn);
        }
        System.out.println("new size: " + model_pool.size());
    }

    public void setup_next_round_models(Point winner_id) {

        //System.out.println("next round models");
        BotPlayer winner = (BotPlayer) current_game.players.get(winner_id);

        // add a model by averaging the last round models
        HMM offspring = get_last_round_offspring();
        offspring.transition_bit_mask = winner.model.transition_bit_mask;
        for (int i=0; i<offspring.transition_bit_mask.size(); i++) {
            if (lab_screen.game.rng.nextFloat() < mutation_rate) {
                offspring.transition_bit_mask.flip(i);
            }
        }

        offspring.apply_transition_mask();
        Point offspring_player_id = new Point(
            winner_id.x,
            winner_id.y + 1
        );
        System.out.println(offspring_player_id);

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
        ArrayList<Point> rec = new ArrayList<>();
        rec.add(new Point(0, 0));
        win_records.put(offspring_player_id, rec);

        ArrayList<Integer> turn = new ArrayList<>();
        turn.add(current_iteration);
        win_record_turns.put(offspring_player_id, turn);

        prune_model_pool();

        int n = pool_size - model_pool.size();
        if (n > 0) {
            add_new_random_models(n);
        }

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

    public void silent_logic(){
        int iterations = Math.round(lab_screen.overlay.saved_settings.get("iterations"));
        if (current_iteration < iterations){
            run_silent();
            current_iteration ++;
        }
    }


    public void logic(){
        Point winner_id = current_game_orchestrator.test_victory_conditions();
        if (!current_game_orchestrator.paused) {
            // if victory conditions were met, finish up the last round and start the timer for the next one
            if (winner_id != null) {
                current_game_orchestrator.pause();
                current_game_orchestrator.finished = true;

                if (!show_summary_screen) {
                    // end of round housekeeping
                    finish_this_round(winner_id);
                    show_summary_screen = true;
                    next_round_begin = false;
                    between_round_pause_timer = 0f;

                    if (current_iteration == iterations & write_files) {
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

    public void silent_draw(){
        model_pool_display.render();
        win_record_graph.render();
    }



    public void render(){

        if (kill) {
            dispose();
        }
        else {
            if (silent) {
                silent_logic();
                silent_draw();
            }
            else{
                logic();
                draw();
            }

        }
    }

    public void dispose() {
        model_pool.clear();
        win_records.clear();
        current_game.dispose();
    }


    public void write_champions_to_file(){
        System.out.println("saving files");

        HashMap<Float, ArrayList<Point>> models_by_win_margin = new HashMap<>();

        for (Point p : model_pool.keySet()){
            Point rec = win_records.get(p).get(0);
            float margin = rec.x - rec.y;
            if (!models_by_win_margin.containsKey(margin)){
                models_by_win_margin.put(margin, new ArrayList<>());
            }
            ArrayList<Point> m = models_by_win_margin.get(margin);
            m.add(p);
            models_by_win_margin.put(margin, m);
        }

        int s = 0;
        for (Float m : models_by_win_margin.keySet()) {
            for (Point p : models_by_win_margin.get(m)){
                if (s < max_models_to_save){
                    lab_screen.game.file_handler.save_model(model_pool.get(p), player_names.get(p));
                    s++;
                }
            }
        }
    }
}
