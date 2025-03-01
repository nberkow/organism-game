package io.github.organism;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ArcadeLoop implements GameMode{


    float map_center_x;
    float map_center_y;
    GameConfig menu_cfg;
    GameConfig game_cfg;

    SettingsOverlay menu_overlay;
    SettingsOverlay game_overlay;
    int iterations;

    boolean show_summary_screen;
    boolean next_round_begin;
    boolean kill = false;

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
    HashMap<Point, Point> win_records;
    int current_iteration;
    OrganismGame game;
    GameBoard current_game;
    GameOrchestrator current_game_orchestrator;
    RoundSummary round_summary;

    Screen current_screen;

    public ArcadeLoop(OrganismGame g) {
        game = g;
        model_pool = new HashMap<>();
        win_records = new HashMap<>();

        setup_overlays();
        load_models();

        available_colors = new ArrayList<>();

        tournament_player_colors = new HashMap<>();
        player_names = new HashMap<>();

        show_summary_screen = false;
        next_round_begin = true;

        map_center_x = game.VIRTUAL_WIDTH / 2f;
        map_center_y = game.VIRTUAL_HEIGHT / 2f;
    }

    private void setup_overlays() {

        menu_cfg = game.file_handler.read_cfg("kingdoms", "map");
        game_cfg = game.file_handler.read_cfg("kingdoms", "map");

        float overlay_w = this.game.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (this.game.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = this.game.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (this.game.VIRTUAL_HEIGHT - overlay_h) / 2f;

        game_overlay = new SettingsOverlay(game, game.game_screen, overlay_x, overlay_y, overlay_w, overlay_h);
        game_overlay.setup_sliders();
        game_overlay.setup_buttons();
        game_cfg.gameplay_settings = game_overlay.saved_settings;

        menu_overlay = new SettingsOverlay(game, game.menu_screen, overlay_x, overlay_y, overlay_w, overlay_h);
        menu_overlay.setup_sliders();
        menu_overlay.setup_buttons();
        menu_cfg.gameplay_settings = game_overlay.saved_settings;
    }

    public void setup_for_menu(){

        menu_cfg.map_view_size_param = 350;

        between_round_pause_timer = 0;

        menu_cfg.human_players = 0;
        menu_cfg.bot_players = 3;
        current_iteration = 1;
        player_primary_index = 0;
        current_screen = game.menu_screen;

        create_game_board();
        create_players_from_model_pool(3);
        create_player_starts();
        current_game.create_player_summary_displays();
        current_game.show_player_summary = false;
        current_game_orchestrator.update_speed(1);
        current_game_orchestrator.run();
    }

    private void create_players_from_model_pool(int n) {
        ArrayList<Point> player_ids = new ArrayList<>(model_pool.keySet());
        Collections.shuffle(player_ids, game.rng);

        for (int i=0; i<n; i++) {
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
        current_game.diplomacy_graph = new DiplomacyGraph(game, current_game);
    }

    public void setup_for_arcade(int n) {
        game.game_screen.io_player_names = new ArrayList<>();
        game.game_screen.io_player_ids = new ArrayList<>();

        between_round_pause_timer = 2;

        game_cfg.human_players = n;
        game_cfg.bot_players = 3 - n;
        current_iteration = 1;
        player_primary_index = 0;
        current_screen = game.game_screen;

        create_game_board();

        create_players_from_model_pool(game_cfg.bot_players);
        create_human_players();
        create_player_starts();
        set_player_button_colors();

        current_game.create_player_summary_displays();
        current_game.show_player_summary = true;

        current_game.diplomacy_graph = new DiplomacyGraph(game, current_game);
        current_game.show_diplomacy = true;

        current_game_orchestrator.update_speed(1);
        current_game_orchestrator.run();
    }


    public void create_game_board() {

        if (available_colors.size() < 3){
            available_colors.addAll(Arrays.asList(game.player_colors).subList(2, game.player_colors.length));
        }

        current_game = new GameBoard(game, menu_cfg, current_screen);
        game.game_screen.input_processor.game_board = current_game;
        current_game.void_distributor.distribute();
        current_game.resource_distributor.distribute();

        current_game_orchestrator = new GameOrchestrator(current_game);
        current_game.set_orchestrator(current_game_orchestrator);

        current_game.center_x = map_center_x;
        current_game.center_y = map_center_y;
    }

    public void create_player_starts() {
        int sc = (int) Math.floor(Math.pow(menu_cfg.radius, menu_cfg.player_start_positions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = current_game.player_start_assigner.randomize_starting_coords();
            current_game.player_start_assigner.assign_starting_hexes(starting_coords);
        }
    }

    public void load_models() {
        for (HMM model : game.file_handler.load_models()){
            Point p = new Point(player_primary_index, 0);
            model.player_tournament_id = p;
            model_pool.put(p, model);
            player_primary_index++;
        }
    }

    public void run_arcade_loop() {
        System.out.println("first iteration");
        current_game_orchestrator.update_speed(menu_cfg.gameplay_settings.get("speed"));
        current_game_orchestrator.run();
    }

    private void finish_this_round(Point winner_id) {

        System.out.println("iteration: " + current_iteration + "/" + iterations);

        for (Point p : current_game.players.keySet()) {
            Point rec = win_records.get(p);
            if (p == winner_id){
                rec.x += 1;
            }
            else {
                rec.y += 1;
            }
            win_records.put(p, rec);
        }

        round_summary.set_winner(winner_id);
        show_summary_screen = true;

    }

    private void setup_next_round() {

        current_game.dispose();
        current_game_orchestrator.dispose();

        create_game_board();
        create_human_players();
        create_bot_players();
        create_player_starts();
        set_player_button_colors();
        current_game.create_player_summary_displays();
        current_game_orchestrator.update_speed(menu_cfg.gameplay_settings.get("speed"));
        current_game_orchestrator.run();
    }

    private void set_player_button_colors() {
        GameScreen game_screen = (GameScreen) current_screen;
        ArrayList<Player> players = game_screen.get_io_players();

        // player 1
        Player p = players.get(0);
        int idx = current_game.all_player_ids.indexOf(p);

        Point left_p = current_game.all_player_ids.get((idx + 2) % 3);
        Color left_c = tournament_player_colors.get(left_p);

        Point right_p = current_game.all_player_ids.get((idx + 1) % 3);
        Color right_c = tournament_player_colors.get(right_p);

        game_screen.player1_hud.game_buttons.set_button_colors(left_c, right_c);

        if (players.size() == 2) {
            p = players.get(1);
            idx = current_game.all_player_ids.indexOf(p);

            left_p = current_game.all_player_ids.get((idx + 2) % 3);
            left_c = tournament_player_colors.get(left_p);

            right_p = current_game.all_player_ids.get((idx + 1) % 3);
            right_c = tournament_player_colors.get(right_p);

            game_screen.player2_hud.game_buttons.set_button_colors(left_c, right_c);
        }
    }

    public void create_human_players(){

        for (int p = 0; p<menu_cfg.human_players; p++){
            Point player_id = new Point(-1, p);

            Color color;
            if (tournament_player_colors.containsKey(player_id)){
                color = tournament_player_colors.get(player_id);
            } else {
                color = available_colors.remove(0);
                tournament_player_colors.put(player_id, color);
            }

            String name = "Player " + (p + 1);
            Organism organism = new Organism(current_game);
            Player player = new IO_Player(
                current_game,
                name,
                p,
                player_id,
                organism,
                false,
                color
            );
            organism.player = player;
            current_game.players.put(player_id, player);
            game.game_screen.add_player(player, p==1);

            current_game.human_player_ids.add(player_id);
            current_game.all_player_ids.add(player_id);
        }
    }
    private void create_bot_players() {

        /*
        randomly select 3 models from the pool

        use these to create 3 players on the current game board

         */


        ArrayList<Point> player_ids = new ArrayList<>(model_pool.keySet());
        Collections.shuffle(player_ids, game.rng);

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
        current_game.diplomacy_graph = new DiplomacyGraph(game, current_game);
    }


    public void logic(){

    }

    private void finish_arcade_loop() {
    }

    public void draw(){
        current_game.game.camera.update();
        current_game.render();
        if (show_summary_screen & between_round_pause > 0 ) {
            round_summary.render();
        }
    }

    public void render(){

        if (kill) {
            dispose();
        }
        else {
            logic();
            draw();
        }
    }

    public void dispose() {
        model_pool.clear();
        win_records.clear();
        current_game.dispose();
    }

}
