package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class MapSettingsScreen implements Screen {

    final int DEFAULT_SIZE = 6;
    public float buttons_y;
    public float file_buttons_y;
    float controls_x;
    float controls_w;
    OrganismGame game;
    GameBoard game_board;
    GameConfig cfg;
    MapSettingsInputProcessor inputProcessor;
    MapSettingsSliders sliders;
    MapSettingSelectionBoxes selection_boxes;
    MapSettingsButtons buttons;
    MapSettingsButtons file_buttons;

    boolean render_file_buttons;

    public MapSettingsScreen(OrganismGame g){

        game = g;
        cfg = new GameConfig();
        cfg.radius = DEFAULT_SIZE;
        controls_x = this.game.VIRTUAL_WIDTH * 9 / 16f;
        controls_w = this.game.VIRTUAL_WIDTH / 2.5f;
        buttons_y = this.game.VIRTUAL_HEIGHT * .05f;

        game_board = new GameBoard(game, cfg, this);
        game_board.radius = DEFAULT_SIZE;
        game_board.center_x = this.game.VIRTUAL_WIDTH / 3.5f;
        game_board.center_y = this.game.VIRTUAL_HEIGHT / 2f;

        sliders = new MapSettingsSliders(game, this);
        selection_boxes = new MapSettingSelectionBoxes(game, this);
        buttons = new MapSettingsButtons(this, new String[] {"preview", "save", "load"}, buttons_y);
        file_buttons_y = buttons_y + buttons.button_height * 1.1f;
        file_buttons = new MapSettingsButtons(this, new String[] {"slot1", "slot2", "slot3"}, file_buttons_y);

        update_map();
    }

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

        ScreenUtils.clear(game_board.game.backgroundColor);
        game_board.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : game_board.universe_map.hex_grid) {
            if (pos.content != null) pos.content.render();
        }
        for (GridPosition pos : game_board.universe_map.vertex_grid) {
            if (pos.content != null) pos.content.render();
        }

        sliders.render();
        selection_boxes.render();
        buttons.render();
        if (render_file_buttons) {
            file_buttons.render();
        }
    }

    public void update_map() {
        create_config();
        cfg.radius = (int) Math.floor(sliders.slider_selected_vals.get("radius"));

        game_board = new GameBoard(game, cfg, this);
        game_board.voidDistributor.distribute();
        game_board.resourceDistributor.distribute();

        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = game_board.player_start_assigner.randomizeStartingCoords();
            game_board.player_start_assigner.assignStartingHexes(starting_coords);
        }

        game_board.center_x = this.game.VIRTUAL_WIDTH / 3.5f;
        game_board.center_y = this.game.VIRTUAL_HEIGHT / 2f;

    }

    public void create_config() {
        cfg = new GameConfig();

        cfg.radius = (int) Math.ceil(sliders.slider_selected_vals.get("radius"));
        cfg.resources = sliders.slider_selected_vals.get("resources");
        cfg.vertex_density = sliders.slider_selected_vals.get("density");
        cfg.playerStartPositions = sliders.slider_selected_vals.get("starts");

        cfg.humanPlayers = Integer.parseInt(selection_boxes.selected_vals.get("human players"));
        cfg.botPlayers = Integer.parseInt(selection_boxes.selected_vals.get("players")) - cfg.humanPlayers;
        cfg.layout = selection_boxes.selected_vals.get("layout");
        cfg.difficulty = selection_boxes.selected_vals.get("opponents");


    }


    public void set_new_game_board(GameConfig config) {
        game_board = new GameBoard(this.game, config, this);
        game_board.voidDistributor.distribute();
        game_board.resourceDistributor.distribute();

        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = game_board.player_start_assigner.randomizeStartingCoords();
            game_board.player_start_assigner.assignStartingHexes(starting_coords);
        }

        game_board.center_x = this.game.VIRTUAL_WIDTH / 3.5f;
        game_board.center_y = this.game.VIRTUAL_HEIGHT / 2f;
    }

    /**
     * @param width
     * @param height
     */
    @Override
    public void resize(int width, int height) {

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
