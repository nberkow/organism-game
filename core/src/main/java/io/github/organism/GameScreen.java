package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.Point;
import java.util.ArrayList;

public class GameScreen implements Screen {

    public SettingsManager settings_manager;
    public SettingsOverlay overlay;
    OrganismGame game;
    GameInputProcessor input_processor;
    GameConfig cfg;
    PlayerHud player1_hud;
    PlayerHud player2_hud;
    ArrayList<String> io_player_names;
    ArrayList<Point> io_player_ids;

    public GameScreen(OrganismGame g){
        game = g;
        setup_overlay();
        overlay.setup_sliders();

        io_player_names = new ArrayList<>();
        io_player_ids = new ArrayList<>();
    }

    public void clear_players(){
        io_player_names = new ArrayList<>();
        io_player_ids = new ArrayList<>();
        input_processor.clear_players();
    }

    public ArrayList<Player> get_io_players(){
        ArrayList<Player> players = new ArrayList<>();
        return players;
    }

    public void add_player(Player player, boolean player2){

        io_player_names.add(player.get_player_name());
        Point player_id = player.get_tournament_id();
        io_player_ids.add(player_id);

        if (!player2) {
            player1_hud = new PlayerHud(game, this, player, false);
        }

        else {
            player2_hud = new PlayerHud(game, this, player, true);
        }
        input_processor.add_player(player_id);

    }

    private void setup_overlay(){

        float overlay_w = this.game.VIRTUAL_WIDTH / 1.8f;
        float overlay_x = (this.game.VIRTUAL_WIDTH - overlay_w) / 2;
        float overlay_h = this.game.VIRTUAL_HEIGHT * 0.9f;
        float overlay_y = (this.game.VIRTUAL_HEIGHT - overlay_h) / 2f;

        overlay = new SettingsOverlay(game, this, overlay_x, overlay_y, overlay_w, overlay_h);
    }

    private void input() {
        if (!game.main_arcade_loop.current_game_orchestrator.paused) {
            input_processor.update_timers(Gdx.graphics.getDeltaTime());
            input_processor.update_queues_with_input();
            game.main_arcade_loop.current_game_orchestrator.update_players();
            game.main_arcade_loop.current_game_orchestrator.update_timers_and_flags();
        }
    }

    private void logic() {}

    private void draw() {}

    @Override
    public void resize(int width, int height) { }
    /**
     *
     */
    @Override
    public void show() {

    }

    /**
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        input();

        ScreenUtils.clear(game.background_color);

        if (game.main_arcade_loop != null) {
            game.main_arcade_loop.render();
        }

        if (player1_hud != null) {
            player1_hud.render();
        }


        if (player2_hud != null) {
            player2_hud.render();
        }

        if (overlay.show_control_overlay) {
            overlay.render();
        }
    }

    /**
     *
     */
    @Override
    public void pause() {

    }

    /**
     *
     */
    @Override
    public void resume() {

    }

    /**
     *
     */
    @Override
    public void hide() {

    }

    /**
     *
     */
    @Override
    public void dispose() {

    }
}
