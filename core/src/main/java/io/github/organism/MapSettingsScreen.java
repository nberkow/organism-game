package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashMap;

public class MapSettingsScreen implements Screen {

    final int DEFAULT_SIZE = 6;
    OrganismGame game;
    GameBoard game_board;
    MapSettingsInputProcessor input_processor;
    GameConfig cfg;
    float controls_x;
    float controls_y;
    float controls_w;
    float controls_h;
    float bar_x;
    float bar_width;
    float bar_height;
    float slider_width;
    float slider_height;
    float bar_spacing;
    HashMap<String, float []> parameters;
    ArrayList<String> label_order;
    public MapSettingsScreen(OrganismGame g){
        game = g;
        cfg = new GameConfig();
        game_board = new GameBoard(game, cfg);
        game_board.radius = DEFAULT_SIZE;
        game_board.center_x = this.game.VIRTUAL_WIDTH / 3.5f;
        game_board.center_y = this.game.VIRTUAL_HEIGHT / 2f;
        controls_y = game_board.center_y;
        controls_x = this.game.VIRTUAL_WIDTH * 3 / 4f;
        controls_w = this.game.VIRTUAL_WIDTH / 2.5f;
        controls_h = this.game.VIRTUAL_HEIGHT / 1.2f;


        bar_width = controls_w * .9f;
        bar_height = 4;
        bar_x = controls_x - (controls_w/2) + (controls_w - bar_width)/2;

        slider_width = bar_width * .1f;
        slider_height = bar_height * 3;

        parameters = new HashMap<>();
        parameters.put("radius", new float[]{2f, 30f, 1f, 4f});
        parameters.put("humans", new float[]{0, 2, 1, 1});
        parameters.put("players", new float[]{3, 6, 3, 0});
        parameters.put("resources", new float[]{0f, 3, 1/3f, 1});
        parameters.put("density", new float[]{1, 6, 1, 3f});

        label_order = new ArrayList<>();
        label_order.add("radius");
        label_order.add("humans");
        label_order.add("players");
        label_order.add("resources");
        label_order.add("density");

        bar_spacing = controls_h / (1 + parameters.size());

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
        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (GridPosition pos : game_board.universe_map.hex_grid) {
            if (pos.content != null) pos.content.render();
        }
        for (GridPosition pos : game_board.universe_map.vertex_grid) {
            if (pos.content != null) pos.content.render();
        }

        render_bars();
    }

    public void render_bars(){
        float y = controls_y + (controls_h/2f) - bar_spacing;
        float slider_x;
        int ticks;
        float tick_spacing;
        ArrayList<Float> label_y_vals = new ArrayList<>();

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (String p : label_order){

            label_y_vals.add(y + slider_height * 2);

            // lower bound, upper bound, step size, current value
            float [] values = parameters.get(p);

            ticks = (int) ((values[1] - values[0]) / values[2]);
            tick_spacing = bar_width / ticks;
            for (int i=0; i<=ticks; i++){
                float tick_x = bar_x + tick_spacing * i;
                game_board.shape_renderer.setColor(Color.WHITE);
                game_board.shape_renderer.rect(tick_x-1, y - slider_height * .6f, 2, slider_height * 1.2f);
            }

            game_board.shape_renderer.setColor(Color.DARK_GRAY);
            game_board.shape_renderer.rect(
                bar_x,
                y - bar_height/2,
                bar_width,
                bar_height
            );

            slider_x = values[3] / (values[1] - values[0]) * bar_width;

            game_board.shape_renderer.setColor(Color.GRAY);
            game_board.shape_renderer.rect(
                bar_x + slider_x - slider_width/2,
                y - slider_height/2,
                slider_width,
                slider_height
            );

            y -= bar_spacing;
        }
        game_board.shape_renderer.end();

        game_board.batch.begin();
        for (int i=0; i<label_y_vals.size(); i++){
            String label = label_order.get(i);
            Float label_y = label_y_vals.get(i);
            game_board.font.getData().setScale(1f);
            game_board.font.draw(
                game_board.batch,
                label,
                bar_x,
                label_y);
        }

        game_board.batch.end();


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
