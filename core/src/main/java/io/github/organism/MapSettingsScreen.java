package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

import io.github.organism.map.GridPosition;

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
        controls_x = OrganismGame.VIRTUAL_WIDTH * 9 / 16f;
        controls_w = OrganismGame.VIRTUAL_WIDTH / 2.5f;
        buttons_y = OrganismGame.VIRTUAL_HEIGHT * .05f;

        game_board = new GameBoard(game, cfg, null);
        game_board.radius = DEFAULT_SIZE;
        game_board.centerX = OrganismGame.VIRTUAL_WIDTH / 3.5f;
        game_board.centerY = OrganismGame.VIRTUAL_HEIGHT / 2f;

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
        // Execute deferred actions from input processor
        if (inputProcessor != null && inputProcessor.deferredAction != null) {
            Runnable action = inputProcessor.deferredAction;
            inputProcessor.deferredAction = null;
            action.run();
            return; // Don't render this frame, we're transitioning
        }

        ScreenUtils.clear(game_board.game.backgroundColor);
        game_board.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : game_board.universeMap.hexGrid) {
            if (pos.content != null) pos.content.render();
        }
        for (GridPosition pos : game_board.universeMap.vertexGrid) {
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

        game_board = new GameBoard(game, cfg, null);
        game_board.voidDistributor.distribute();
        game_board.resourceDistributor.distribute();

        int sc = (int) Math.floor(Math.pow(cfg.radius, cfg.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = game_board.playerStartAssigner.randomizeStartingCoords();
            game_board.playerStartAssigner.assignStartingHexes(starting_coords);
        }

        game_board.centerX = OrganismGame.VIRTUAL_WIDTH / 3.5f;
        game_board.centerY = OrganismGame.VIRTUAL_HEIGHT / 2f;

    }

    public void create_config() {
        if (cfg == null) {
            cfg = new GameConfig();
        }

        cfg.radius = (int) Math.ceil(sliders.slider_selected_vals.get("radius"));
        cfg.resources = sliders.slider_selected_vals.get("resources");
        cfg.vertex_density = sliders.slider_selected_vals.get("density");
        cfg.playerStartPositions = sliders.slider_selected_vals.get("starts");

        cfg.layout = selection_boxes.selected_vals.get("layout");
        cfg.difficulty = selection_boxes.selected_vals.get("opponents");

        // Preserve or generate seed
        if (cfg.seed == 0) {
            cfg.seed = System.currentTimeMillis();
        }
    }

    public GameConfig getCurrentConfig() {
        // Always create fresh config from current UI state
        create_config();

        // Ensure seed is set
        if (cfg.seed == 0) {
            cfg.seed = System.currentTimeMillis();
        }

        return cfg;
    }


    public void set_new_game_board(GameConfig config) {
        cfg = config;

        // Update sliders to match loaded config
        sliders.slider_selected_vals.put("radius", (float) config.radius);
        sliders.slider_selected_vals.put("resources", config.resources);
        sliders.slider_selected_vals.put("density", config.vertex_density);
        sliders.slider_selected_vals.put("starts", config.playerStartPositions);
        sliders.loadInitialPositions();

        // Update selection boxes to match loaded config
        selection_boxes.selected_vals.put("human players", String.valueOf(config.humanPlayers));
        selection_boxes.selected_vals.put("players", String.valueOf(config.humanPlayers + config.botPlayers));
        selection_boxes.selected_vals.put("layout", config.layout);
        selection_boxes.selected_vals.put("opponents", config.difficulty);

        game_board = new GameBoard(this.game, config, null);
        game_board.voidDistributor.distribute();
        game_board.resourceDistributor.distribute();

        int sc = (int) Math.floor(Math.pow(config.radius, config.playerStartPositions));
        for (int i=0; i<sc; i++) {
            ArrayList<int[]> starting_coords = game_board.playerStartAssigner.randomizeStartingCoords();
            game_board.playerStartAssigner.assignStartingHexes(starting_coords);
        }

        game_board.centerX = OrganismGame.VIRTUAL_WIDTH / 3.5f;
        game_board.centerY = OrganismGame.VIRTUAL_HEIGHT / 2f;
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
