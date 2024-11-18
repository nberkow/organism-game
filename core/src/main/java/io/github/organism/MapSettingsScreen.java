package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class MapSettingsScreen implements Screen {

    final int DEFAULT_SIZE = 6;
    public float buttons_y;
    float controls_x;
    float controls_w;
    OrganismGame game;
    GameBoard game_board;
    GameConfig cfg;
    MapSettingsInputProcessor input_processor;

    MapSettingsSliders sliders;
    MapSettingSelectionBoxes selection_boxes;
    MapSettingsButtons buttons;

    public MapSettingsScreen(OrganismGame g){

        game = g;
        cfg = new GameConfig();
        cfg.radius = DEFAULT_SIZE;
        controls_x = this.game.VIRTUAL_WIDTH * 9 / 16f;
        controls_w = this.game.VIRTUAL_WIDTH / 2.5f;
        buttons_y = this.game.VIRTUAL_HEIGHT * .05f;

        game_board = new GameBoard(game, cfg);
        game_board.radius = DEFAULT_SIZE;
        game_board.center_x = this.game.VIRTUAL_WIDTH / 3.5f;
        game_board.center_y = this.game.VIRTUAL_HEIGHT / 2f;

        sliders = new MapSettingsSliders(game, this);
        selection_boxes = new MapSettingSelectionBoxes(game, this);
        buttons = new MapSettingsButtons(this);
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

        ScreenUtils.clear(game_board.background_color);
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : game_board.universe_map.hex_grid) {
            if (pos.content != null) pos.content.render();
        }
        for (GridPosition pos : game_board.universe_map.vertex_grid) {
            if (pos.content != null) pos.content.render();
        }

        sliders.render();
        selection_boxes.render();
        buttons.render();
    }

    public void update_map() {
        GameConfig cfg = new GameConfig();
        cfg.radius = (int) Math.floor(sliders.slider_selected_vals.get("radius"));

        game_board = new GameBoard(game, cfg);
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
