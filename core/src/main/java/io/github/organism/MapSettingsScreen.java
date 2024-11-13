package io.github.organism;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class MapSettingsScreen implements Screen {

    final int DEFAULT_SIZE = 6;
    final float TICK_WIDTH = 2f;
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
    HashMap<String, float []> slider_coords;
    HashMap<String, float []> bar_coords;
    HashMap<String, float [][]> bar_tick_coords;
    HashMap<String, float []> label_coords;
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
        bar_coords = new HashMap<>();
        label_coords = new HashMap<>();
        slider_coords = new HashMap<>();
        bar_tick_coords = new HashMap<>();
        load_initial_positions();
    }

    private void load_initial_positions() {
        float y = controls_y + (controls_h/2f) - bar_spacing;
        float slider_x;
        int ticks;
        float tick_spacing;

        // rect coordinates in order
        float [] bar_coord;
        float [] label_coord;
        float [] slider_coord;

        // rect coordinates in order, per tick
        float [][] bar_tick_coord;

        for (String p : label_order){

            label_coord = new float[] {bar_x, y + slider_height * 2};
            label_coords.put(p, label_coord);

            // lower bound, upper bound, step size, current value
            float [] values = parameters.get(p);

            ticks = (int) ((values[1] - values[0]) / values[2]);
            bar_tick_coord = new float [ticks+1][4];

            tick_spacing = bar_width / ticks;
            for (int i=0; i<=ticks; i++){
                bar_tick_coord[i][0] = bar_x + tick_spacing * i - 1;
                bar_tick_coord[i][1] = y - slider_height * .6f;
                bar_tick_coord[i][2] = TICK_WIDTH;
                bar_tick_coord[i][3] = slider_height * 1.2f;
            }
            bar_tick_coords.put(p, bar_tick_coord);

            bar_coord = new float []{
                bar_x,
                y - bar_height / 2,
                bar_width,
                bar_height
            };
            bar_coords.put(p, bar_coord);

            slider_x = values[3] / (values[1] - values[0]) * bar_width;
            slider_coord = new float [] {
                bar_x + slider_x - slider_width/2,
                y - slider_height/2,
                slider_width,
                slider_height
            };
            slider_coords.put(p, slider_coord);

            y -= bar_spacing;
        }
    }
    public String poll_sliders(float screenX, float screenY) {

        String r = null;
        for (String p : label_order){
            float [] coord = slider_coords.get(p);
            if (screenX > coord[0] && screenX < coord[0] + coord[2]){
                if (screenY > coord[1] && screenY < coord[1] + coord[3]){
                    r = p;
                }
            }
        }
        return r;
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

        render_bars();
    }

    public void render_bars(){

        game_board.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);

        for (String p : label_order){

            float[][] tick_coord = bar_tick_coords.get(p);

            for (float[] rect_coords : tick_coord) {
                game_board.shape_renderer.setColor(Color.WHITE);
                game_board.shape_renderer.rect(
                    rect_coords[0],
                    rect_coords[1],
                    rect_coords[2],
                    rect_coords[3]
                );
            }

            game_board.shape_renderer.setColor(Color.DARK_GRAY);
            float[] bar_coord = bar_coords.get(p);
            game_board.shape_renderer.rect(
                bar_coord[0],
                bar_coord[1],
                bar_coord[2],
                bar_coord[3]
            );

            game_board.shape_renderer.setColor(Color.GRAY);
            float[] slider_coord = slider_coords.get(p);
            game_board.shape_renderer.rect(
                slider_coord[0],
                slider_coord[1],
                slider_coord[2],
                slider_coord[3]
            );
        }

        game_board.shape_renderer.end();

        game_board.batch.begin();

        for (String p : label_order){
            float[] label_coord = label_coords.get(p);
            game_board.font.getData().setScale(1f);
            game_board.font.draw(
                game_board.batch,
                p,
                label_coord[0],
                label_coord[1]);
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
