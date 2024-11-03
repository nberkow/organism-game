package io.github.organism;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {

    GameBoard game_board;
    OrthographicCamera camera;
    FitViewport viewport;
    GameInputProcessor input_processor;
    double action_time = 1d;

    double queue_time = action_time / 40;
    boolean execute_actions = false;

    boolean queue_bot_actions = false;

    double action_clock = 0d;
    double queue_clock = 0d;

    public final int VIRTUAL_WIDTH = 1920/2;  // Virtual resolution width
    public final int VIRTUAL_HEIGHT = 1080/2; // Virtual resolution height

    @Override
    public void create() {
        // Initialize the camera and viewport for the virtual game world dimensions
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

        game_board = new GameBoard(this, new GameConfig());

        input_processor = new GameInputProcessor(game_board);
        Gdx.input.setInputProcessor(input_processor);
    }

    @Override
    public void render() {
        // Clear the screen and render the game board
        ScreenUtils.clear(game_board.background_color);  // Clear with black color
        input();
        logic();
        draw();
    }

    private void input() {
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

        // handle input
        input_processor.update_timers(Gdx.graphics.getDeltaTime());
        input_processor.update_queues_with_input();


    }

    private void logic() {

        //FIXME for (Player p : game_board.players.values()) {
        for (String s : game_board.human_player_names){
            Player p = game_board.players.get(s);
            p.get_organism().update_resources();
            p.get_organism().update_income();
        }

        /*
        if (queue_bot_actions) {
            for (String b : game_board.bot_player_names){
                game_board.players.get(b).generate_and_queue();
            }
        }*/

        // dequeue an action from each player's queue and execute it
        if (execute_actions) {
            dequeue_and_execute();
        }
    }

    private void draw() {
        // Ensure the camera is updated before drawing
        camera.update();
        game_board.render();
    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport and camera based on the new window size
        viewport.update(width, height, true);  // true centers the camera
    }

    @Override
    public void dispose() {
        // Handle disposing of game resources
        game_board.dispose();
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
