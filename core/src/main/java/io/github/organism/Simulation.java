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

import io.github.organism.player.BotPlayer;

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
    
    // Tournament scoring
    HashMap<Point, Integer> cumulativeScores;  // Total vertices captured across all games
    HashMap<Point, Integer> gamesPlayed;       // Number of games each player has participated in
    
    // Tournament management
    int tournamentRound;
    int roundsPerElimination = 10;  // Eliminate players every N rounds
    int playersToEliminatePerRound = 2;  // How many to eliminate each time
    
    ModelSpawner modelSpawner;


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
        cumulativeScores = new HashMap<>();
        gamesPlayed = new HashMap<>();
        
        tournamentRound = 0;
        modelSpawner = new ModelSpawner(0);

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

    public void runSilent(){

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
        Point winnerId = currentGameOrchestrator.testVictoryConditions();

        finishThisRound(winnerId);
        setupNextRoundModels(winnerId);

    }

    public void createGameBoard() {

        if (availableColors.size() < 3){
            availableColors.addAll(Arrays.asList(game.playerColors));
        }

        currentGame = new GameBoard(game, cfg, this);
        currentGame.voidDistributor.distribute();
        currentGame.resourceDistributor.distribute();

        currentGameOrchestrator = new GameOrchestrator(currentGame);
        currentGame.setOrchestrator(currentGameOrchestrator);

        currentGame.centerX = mapCenterX;
        currentGame.centerY = mapCenterY;
        currentGame.showPlayerSummary = true;
        currentGame.showDiplomacy = true;
    }

    public void createPlayerStarts() {
        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = currentGame.playerStartAssigner.randomizeStartingCoords();
            currentGame.playerStartAssigner.assignStartingHexes(starting_coords);
        }
    }

    public void initialize_model_pool() {
        /*
        start the pool with models given totally random weights
         */

        for (int i=0; i<pool_size; i++){
            Model model = modelSpawner.createRandomModel();
            Point player_id = model.getPlayerTournamentId();
            
            modelPool.put(player_id, model);

            ArrayList<Point> wins = new ArrayList<>();
            ArrayList<Integer> turns = new ArrayList<>();

            wins.add(new Point(0, 0));
            turns.add(0);

            winRecords.put(player_id, wins);
            winRecordTurns.put(player_id, turns);
            cumulativeScores.put(player_id, 0);
            gamesPlayed.put(player_id, 0);
        }
        
        playerPrimaryIndex = modelSpawner.getNextPrimaryIndex();
    }

    public void advanceFrameCount(){

    }

    /**
     * @return
     */
    @Override
    public Screen getScreen() {
        return screen;
    }

    /**
     * @param p
     * @param organism
     */
    @Override
    public void updateHud(Point p, Organism organism) {
        /*FIXME
        PlayerHud hud = playerIdToHud.get(p);
        hud.setIncome(organism.income);
        hud.setEnergy(organism.energy);
        hud.setSpend(organism.spend);*/
    }

    public void runSimulation() {

        System.out.println("first iteration");
        silent = false;
        initialize_model_pool();
        // setup the first game
        createGameBoard();
        createPlayersFromModelPool();
        createPlayerStarts();
        currentGame.createPlayerSummaryDisplays();
        currentGameOrchestrator.updateSpeed(cfg.gameplaySettings.get("speed"));
        currentGameOrchestrator.run();

    }

    private void finishThisRound(Point winner_id) {

        System.out.println("iteration: " + currentIteration + "/" + iterations);
        tournamentRound++;

        // Update win/loss records and capture scores
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
            
            // Capture vertices as score
            int verticesCaptured = currentGame.players.get(p).getOrganism().territoryVertex.size();
            int currentScore = cumulativeScores.getOrDefault(p, 0);
            cumulativeScores.put(p, currentScore + verticesCaptured);
            
            int games = gamesPlayed.getOrDefault(p, 0);
            gamesPlayed.put(p, games + 1);
            
            System.out.println("Player " + p + " captured " + verticesCaptured + " vertices. Total: " + cumulativeScores.get(p));
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
        currentGameOrchestrator.updateSpeed(cfg.gameplaySettings.get("speed"));
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

            currentGame.createBotPlayer(name, player_id, color);
            player_names.put(player_id, name);
            tournament_player_colors.put(player_id, color);
        }

    }

    public void eliminateBottomPlayers(){
        // Only eliminate if we have enough players and it's an elimination round
        if (modelPool.size() <= 3 || tournamentRound % roundsPerElimination != 0) {
            return;
        }

        System.out.println("=== ELIMINATION ROUND " + tournamentRound + " ===");
        
        // Sort players by average score (total score / games played)
        ArrayList<Point> playerIds = new ArrayList<>(modelPool.keySet());
        playerIds.sort((p1, p2) -> {
            double avg1 = cumulativeScores.getOrDefault(p1, 0) / (double) Math.max(1, gamesPlayed.getOrDefault(p1, 1));
            double avg2 = cumulativeScores.getOrDefault(p2, 0) / (double) Math.max(1, gamesPlayed.getOrDefault(p2, 1));
            return Double.compare(avg2, avg1); // Descending order (best first)
        });
        
        // Eliminate bottom N players
        int toEliminate = Math.min(playersToEliminatePerRound, playerIds.size() - 3); // Keep at least 3
        ArrayList<Point> eliminated = new ArrayList<>();
        
        for (int i = playerIds.size() - 1; i >= playerIds.size() - toEliminate && i >= 0; i--) {
            Point p = playerIds.get(i);
            eliminated.add(p);
            
            // Recycle color
            Color player_color = tournament_player_colors.get(p);
            if (player_color != null && !player_color.equals(Color.DARK_GRAY)) {
                availableColors.add(player_color);
            }
            tournament_player_colors.put(p, Color.DARK_GRAY);
            
            double avgScore = cumulativeScores.getOrDefault(p, 0) / (double) Math.max(1, gamesPlayed.getOrDefault(p, 1));
            System.out.println("Eliminated: " + player_names.get(p) + " (avg: " + avgScore + ")");
        }
        
        // Remove from active pool
        for (Point p : eliminated) {
            modelPool.remove(p);
        }
        
        System.out.println("Pool size after elimination: " + modelPool.size());
    }

    public void add_new_random_models(int n){
        // add new random models
        System.out.println("Adding " + n + " new random organisms");
        
        for (int i=0; i<n; i++) {
            Model model = modelSpawner.createRandomModel();
            Point player_id = model.getPlayerTournamentId();
            
            modelPool.put(player_id, model);
            
            ArrayList<Point> rec = new ArrayList<>();
            rec.add(new Point(0, 0));
            winRecords.put(player_id, rec);
            
            ArrayList<Integer> turn = new ArrayList<>();
            turn.add(currentIteration);
            winRecordTurns.put(player_id, turn);
            
            cumulativeScores.put(player_id, 0);
            gamesPlayed.put(player_id, 0);
            
            System.out.println("Added new organism: " + player_id);
        }
        
        playerPrimaryIndex = modelSpawner.getNextPrimaryIndex();
        System.out.println("Pool size after addition: " + modelPool.size());
    }

    public void setupNextRoundModels(Point winner_id) {
        // Evolution logic disabled for now
        
        // Check if it's time to eliminate players
        eliminateBottomPlayers();
        
        // Add new random organisms to maintain pool size
        int n = pool_size - modelPool.size();
        if (n > 0) {
            add_new_random_models(n);
        }
    }

    // Evolution logic commented out - will be re-enabled later
    /*
    public Model get_last_round_offspring() {
        // This method will be used for evolution in the future
        // For now, we just spawn random organisms
        return null;
    }
    */

    public void silent_logic(){
        if (screen.getClass() == LabScreen.class) {
            LabScreen ls = (LabScreen) screen;
            int iterations = Math.round(ls.overlay.savedSettings.get("iterations"));
            if (currentIteration < iterations){
                runSilent();
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
                
                // Print standings periodically
                if (tournamentRound % 5 == 0) {
                    printTournamentStandings();
                }
                
                setup_next_round(winner_id); // this will set winner id back to null

                next_round_begin = false;
                show_summary_screen = false;
            }

        }
    }

    public void draw(float delta){
        currentGame.game.camera.update();
        currentGame.render(delta);
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

    public void render(float delta){

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
                draw(delta);
            }

        }
    }

    public void dispose() {
        modelPool.clear();
        winRecords.clear();
        currentGame.dispose();
    }


    public void printTournamentStandings() {
        System.out.println("\n=== TOURNAMENT STANDINGS (Round " + tournamentRound + ") ===");
        
        ArrayList<Point> playerIds = new ArrayList<>(modelPool.keySet());
        playerIds.sort((p1, p2) -> {
            double avg1 = cumulativeScores.getOrDefault(p1, 0) / (double) Math.max(1, gamesPlayed.getOrDefault(p1, 1));
            double avg2 = cumulativeScores.getOrDefault(p2, 0) / (double) Math.max(1, gamesPlayed.getOrDefault(p2, 1));
            return Double.compare(avg2, avg1);
        });
        
        int rank = 1;
        for (Point p : playerIds) {
            String name = player_names.getOrDefault(p, "Unknown");
            int total = cumulativeScores.getOrDefault(p, 0);
            int games = gamesPlayed.getOrDefault(p, 0);
            double avg = total / (double) Math.max(1, games);
            System.out.println(rank + ". " + name + " - Total: " + total + ", Games: " + games + ", Avg: " + String.format("%.2f", avg));
            rank++;
        }
        System.out.println("=====================================\n");
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
