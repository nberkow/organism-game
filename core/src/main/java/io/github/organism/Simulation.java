package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Simulation implements GameSession {

    OrganismGame game;
    Screen screen;
    GameBoard currentGame;
    GameOrchestrator currentGameOrchestrator;
    ModelPoolDisplay modelPoolDisplay;
    RoundSummary roundSummary;
    WinRecordGraph winRecordGraph;
    float mapCenterX;
    float mapCenterY;
    GameConfig cfg;
    int iterations;
    static int MODEL_STATES = 36;

    // 3 players * ((3 indicator variable per move * 6 moves) + energy + territory)
    static int MODEL_INPUTS = 3 * ((3 * 6) + 2);
    float mutation_rate;
    boolean show_summary_screen;
    boolean next_round_begin;
    boolean writeFiles;

    int max_models_to_save = 3;

    boolean kill = false;

    boolean silent = false;

    float between_round_pause = 2f;
    float between_round_pause_timer;

    HashMap<Point, String> player_names;
    HashMap<Point, Color> tournament_player_colors;

    ArrayList<Color> availableColors;
    int playerPrimaryIndex;
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

    HashMap<Point, Model> modelPool;
    HashMap<Point, ArrayList<Point>> winRecords;

    HashMap<Point, ArrayList<Integer>> winRecordTurns;


    public boolean show_histograms = false;
    InputHistogram inputHistogram;
    int pool_size = 9;


    //MoveLogger move_logger;
    //boolean log_written = false;


    int currentIteration;

    public Simulation(OrganismGame g, Screen scr, GameConfig c, int n) {

        game = g;
        screen = scr;
        cfg = c;
        iterations = n;
        currentIteration = 1;
        playerPrimaryIndex = 0;
        mapCenterX = OrganismGame.VIRTUAL_WIDTH / 2f;
        mapCenterY = OrganismGame.VIRTUAL_HEIGHT / 2f;

        modelPool = new HashMap<>();
        winRecords = new HashMap<>();
        winRecordTurns = new HashMap<>();

        modelPoolDisplay = new ModelPoolDisplay(game, this);
        roundSummary = new RoundSummary(game, this);
        availableColors = new ArrayList<>();
        availableColors.addAll(Arrays.asList(game.playerColors));

        tournament_player_colors = new HashMap<>();
        player_names = new HashMap<>();
        //move_logger = new MoveLogger(screen.game);

        show_summary_screen = false;
        next_round_begin = true;
        between_round_pause_timer = 0;
        mutation_rate = (float) Math.pow(1f/MODEL_STATES, 3);

        if (screen.getClass() == LabScreen.class){
            LabScreen ls =  (LabScreen) screen;
            writeFiles = ls.getWriteFiles();
        }


        float graphWidth = (float) OrganismGame.VIRTUAL_WIDTH / 2;
        float graphHeight = graphWidth * .9f;

        winRecordGraph = new WinRecordGraph(
            game, this,
        (OrganismGame.VIRTUAL_WIDTH - graphWidth)/2,
        (OrganismGame.VIRTUAL_HEIGHT - graphHeight)/2,
                graphWidth,
                graphHeight
        );

        inputHistogram = new InputHistogram(
                game, this,
                MODEL_INPUTS,
                (OrganismGame.VIRTUAL_WIDTH - graphWidth)/2,
                (OrganismGame.VIRTUAL_HEIGHT - graphHeight)/2,
                graphWidth,
                graphHeight
        );
    }

    public void run_silent(){

        // setup and run one game until victory
        silent = true;
        createGameBoard();
        createPlayersFromModelPool();
        createPlayerStarts();
        currentGame.createPlayerSummaryDisplays();
        currentGameOrchestrator.run();
        while (!currentGameOrchestrator.finished) {
            logic();
        }
        Point winner_id = currentGameOrchestrator.testVictoryConditions();

        finishThisRound(winner_id);
        setupNextRoundModels(winner_id);

    }

    public void createGameBoard() {

        if (availableColors.size() < 3){
            availableColors.addAll(Arrays.asList(game.playerColors));
        }

        currentGame = new GameBoard(game, cfg, screen);
        currentGame.voidDistributor.distribute();
        currentGame.resourceDistributor.distribute();

        currentGameOrchestrator = new GameOrchestrator(currentGame);
        currentGame.set_orchestrator(currentGameOrchestrator);

        currentGame.center_x = mapCenterX;
        currentGame.center_y = mapCenterY;
        currentGame.showPlayerSummary = true;
        currentGame.showDiplomacy = true;
    }

    public void createPlayerStarts() {
        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = currentGame.player_start_assigner.randomizeStartingCoords();
            currentGame.player_start_assigner.assignStartingHexes(starting_coords);
        }
    }

    public void initialize_model_pool() {
        /*
        start the pool with models given totally random weights
         */

        for (int i=0; i<pool_size; i++){
            Model model = new ReinforcementHMM(game, MODEL_STATES, MODEL_INPUTS);

            model.init_random_weights();
            playerPrimaryIndex += 1;
            Point player_id  = new Point(playerPrimaryIndex, 0);
            model.setPlayerTournamentId(player_id);
            modelPool.put(player_id, model);

            ArrayList<Point> wins = new ArrayList<>();
            ArrayList<Integer> turns = new ArrayList<>();

            wins.add(new Point(0, 0));
            turns.add(0);

            winRecords.put(player_id, wins);
            winRecordTurns.put(player_id, turns);
        }
    }

    public void run_simulation() {

        System.out.println("first iteration");
        silent = false;
        initialize_model_pool();
        // setup the first game
        createGameBoard();
        createPlayersFromModelPool();
        createPlayerStarts();
        currentGame.createPlayerSummaryDisplays();
        currentGameOrchestrator.update_speed(cfg.gameplaySettings.get("speed"));
        currentGameOrchestrator.run();

    }

    private void finishThisRound(Point winner_id) {

        System.out.println("iteration: " + currentIteration + "/" + iterations);

        for (Point p : currentGame.players.keySet()) {
            Point prev_rec = winRecords.get(p).get(0);
            Point rec = new Point(prev_rec.x, prev_rec.y);
            if (p == winner_id){
                rec.x += 1;
            }
            else {
                rec.y += 1;
            }
            winRecords.get(p).add(0, rec);
            winRecordTurns.get(p).add(0, currentIteration);
        }

        roundSummary.set_winner(winner_id);
        show_summary_screen = true;

    }

    private void setup_next_round(Point winner_id) {

        setupNextRoundModels(winner_id);

        currentGame.dispose();
        currentGameOrchestrator.dispose();

        createGameBoard();
        createPlayersFromModelPool();
        createPlayerStarts();
        currentGame.createPlayerSummaryDisplays();
        currentGameOrchestrator.update_speed(cfg.gameplaySettings.get("speed"));
        currentGameOrchestrator.run();
    }

    private void createPlayersFromModelPool() {
        /*
        randomly select 3 models from the pool

        use these to create 3 players on the current game board
         */

        ArrayList<Point> player_ids = new ArrayList<>(modelPool.keySet());
        Collections.shuffle(player_ids, game.rng);

        for (int i=0; i<3; i++) {
            Point player_id = player_ids.get(i);
            Model model = modelPool.get(player_id);
            String name = player_names_array[player_id.x % player_names_array.length] + " " + numerals[player_id.y % numerals.length];

            Color color;
            if (tournament_player_colors.containsKey(player_id)){
                color = tournament_player_colors.get(player_id);
            } else {
                color = availableColors.remove(0);
            }

            currentGame.create_bot_player(name, player_id, color, model);
            player_names.put(player_id, name);
            tournament_player_colors.put(player_id, color);
        }

        // reset diplomacy with newly created players
        currentGame.diplomacyGraph = new DiplomacyGraph(game, currentGame);
    }

    public void prune_model_pool(){

        ArrayList<Point> to_remove = new ArrayList<>();
        HashMap<Float, ArrayList<Point>> models_by_win_margin = new HashMap<>();

        for (Point p : modelPool.keySet()) {
            Point rec = winRecords.get(p).get(0);
            float margin = (rec.x - rec.y);
            if (margin < 1 & rec.y > 0) {
                // recycle colors and mark eliminated players in gray
                to_remove.add(p);
                Color player_color = tournament_player_colors.get(p);
                if (player_color != null && !player_color.equals(Color.DARK_GRAY)) {
                    availableColors.add(player_color); // Recycle color
                }
                tournament_player_colors.put(p, Color.DARK_GRAY); // Mark eliminated players
            }
            if (!models_by_win_margin.containsKey(margin)) {
                models_by_win_margin.put(margin, new ArrayList<>());
            }
        }

        //System.out.println("removing " + to_remove.size());
        for (Point p : to_remove){
            modelPool.remove(p);
        }
        //System.out.println("new size: " + model_pool.size());

    }

    public void add_new_random_models(int n){
        // add new random models

        //System.out.println("adding random " + n);
        for (int i=0; i<n; i++) {
            Model model = new ReinforcementHMM(game, MODEL_STATES, MODEL_INPUTS);
            model.init_random_weights();
            playerPrimaryIndex++;
            Point player_id = new Point(playerPrimaryIndex, 0);
            model.setPlayerTournamentId(player_id);
            modelPool.put(player_id, model);
            ArrayList<Point> rec = new ArrayList<>();
            rec.add(new Point(0, 0));
            winRecords.put(player_id, rec);
            ArrayList<Integer> turn = new ArrayList<>();
            turn.add(currentIteration);
            winRecordTurns.put(player_id, turn);
        }
        //System.out.println("new size: " + model_pool.size());
    }

    public void setupNextRoundModels(Point winner_id) {

        //System.out.println("next round models");
        BotPlayer winner = (BotPlayer) currentGame.players.get(winner_id);

        // add a model by averaging the last round models
        Model offspring = get_last_round_offspring();
        offspring.set_transition_bit_mask(winner.model.get_transition_bit_mask());
        offspring.mutate_bitmask();


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

        offspring.setPlayerTournamentId(offspring_player_id);
        modelPool.put(offspring_player_id, offspring);
        ArrayList<Point> rec = new ArrayList<>();
        rec.add(new Point(0, 0));
        winRecords.put(offspring_player_id, rec);

        ArrayList<Integer> turn = new ArrayList<>();
        turn.add(currentIteration);
        winRecordTurns.put(offspring_player_id, turn);

        prune_model_pool();

        int n = pool_size - modelPool.size();
        if (n > 0) {
            add_new_random_models(n);
        }

    }

    public Model get_last_round_offspring() {

        /*
        in the future this can have more behaviors

        for now
        - create a new model by calculating the weighted average of
        the models from the last round (weighted by final territory)
         */

        // parse params and init datastructures
        int states = MODEL_STATES;
        int inputs = MODEL_INPUTS;

        double [][][] avg_transition_weights = new double[states][states][inputs];
        double [][][] avg_emission_weights = new double[states][4][inputs];

        // calculate the weights
        HashMap<Point, Double> weights = new HashMap<>();

        double total_territory = 0;
        for (Point player_id : currentGame.players.keySet()) {
            double territory = currentGame.players.get(player_id).getOrganism().territory_vertex.get_unmasked_vertices();
            total_territory += territory;
        }

        for (Point player_id  : currentGame.players.keySet()) {
            double territory = currentGame.players.get(player_id).getOrganism().territory_vertex.get_unmasked_vertices();
            weights.put(player_id, territory / total_territory);
        }

        // average the transition weights
        for (int i=0; i<states; i++){
            for (int j=0; j<states; j++){
                for (int k=0; k<inputs; k++){
                    for (Point p : currentGame.players.keySet()) {
                        BotPlayer player = (BotPlayer) currentGame.players.get(p);
                        double s = player.model.get_transition_weights()[i][j][k];
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
                    for (Point p: currentGame.players.keySet()) {
                        BotPlayer player = (BotPlayer) currentGame.players.get(p);
                        double s = player.model.get_emission_weights()[i][j][k];
                        double w = weights.get(p);
                        avg_emission_weights[i][j][k] += s * w;
                    }
                }
            }
        }

        Model offspring = new ReinforcementHMM(game, MODEL_STATES, MODEL_INPUTS);
        offspring.set_weights(avg_transition_weights, avg_emission_weights);
        return offspring;
    }

    public void silent_logic(){
        if (screen.getClass() == LabScreen.class) {
            LabScreen ls = (LabScreen) screen;
            int iterations = Math.round(ls.overlay.savedSettings.get("iterations"));
            if (currentIteration < iterations){
                run_silent();
                currentIteration++;
            }

        }
    }


    public void logic(){
        Point winner_id = currentGameOrchestrator.testVictoryConditions();
        if (!currentGameOrchestrator.paused) {
            // if victory conditions were met, finish up the last round and start the timer for the next one
            if (winner_id != null) {
                currentGameOrchestrator.pause();
                currentGameOrchestrator.finished = true;

                if (!show_summary_screen) {
                    // end of round housekeeping
                    finishThisRound(winner_id);
                    show_summary_screen = true;
                    next_round_begin = false;
                    between_round_pause_timer = 0f;

                    if (currentIteration == iterations & writeFiles) {
                        write_champions_to_file();
                    }
                }
            }

            else {
                inputHistogram.update_inputs();
                currentGameOrchestrator.updatePlayers();
                currentGameOrchestrator.updateTimersAndFlags();
            }
        }

        else {
            between_round_pause_timer += Gdx.graphics.getDeltaTime();

            if (between_round_pause_timer >= between_round_pause) {
                next_round_begin = true;
            }

            if (currentIteration < iterations & next_round_begin) {
                currentIteration++;
                setup_next_round(winner_id); // this will set winner id back to null

                next_round_begin = false;
                show_summary_screen = false;
            }

        }
    }


    public void draw(){
        currentGame.game.camera.update();
        currentGame.render();
        modelPoolDisplay.render();
        if (show_summary_screen & between_round_pause > 0 ) {
            roundSummary.render();
        }
    }

    public void silent_draw(){
        modelPoolDisplay.render();

        if (show_histograms) {
            inputHistogram.render();
        } else {
            winRecordGraph.render();
        }
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
        modelPool.clear();
        winRecords.clear();
        currentGame.dispose();
    }


    public void write_champions_to_file(){
        System.out.println("saving files");

        HashMap<Float, ArrayList<Point>> models_by_win_margin = new HashMap<>();

        for (Point p : modelPool.keySet()){
            Point rec = winRecords.get(p).get(0);
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
                    game.fileHandler.save_model(modelPool.get(p), player_names.get(p));
                    s++;
                }
            }
        }
    }

    /**
     * @return
     */
    @Override
    public InputProcessor getInputProcessor() {
        LabScreen lb = (LabScreen) screen;
        return lb.inputProcessor;
    }
}
