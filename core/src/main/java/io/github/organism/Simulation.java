package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.sun.org.apache.xpath.internal.operations.Or;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Simulation {

    LabScreen lab_screen;
    GameBoard current_game;

    GameOrchestrator current_game_orchestrator;
    float map_center_x;
    float map_center_y;
    GameConfig cfg;
    int iterations;



    public Simulation(LabScreen screen, GameConfig c, int n) {
        lab_screen = screen;
        cfg = c;
        iterations = n;
        map_center_x = lab_screen.game.VIRTUAL_WIDTH / 2f;
        map_center_y = lab_screen.game.VIRTUAL_HEIGHT / 2f;

        create_game();
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

    public void run_simulation() {
        current_game_orchestrator.run();
    }

    public void logic(){
        if (!current_game_orchestrator.paused) {
            if (current_game_orchestrator.update_victory_conditions()) {
                current_game_orchestrator.pause();
            }
            current_game_orchestrator.update_players();
            current_game_orchestrator.update_timers_and_flags();
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
