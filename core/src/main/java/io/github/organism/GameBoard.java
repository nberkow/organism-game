package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Disposable;

public class GameBoard implements Disposable {

    Color background_color = Color.BLACK;
    GridWindow grid_window;
    UniversalHexGrid grid;
    Organism test_organism;
    InputPanel input_panel;

    FitViewport viewport;     // Now passed from Main
    OrthographicCamera camera; // Camera reference (if needed for further use)

    ShapeRenderer shape_renderer;

    Main main;

    public GameBoard(Main main) {
        this.main = main;

        shape_renderer = new ShapeRenderer();

        // Initialize other game objects here
        grid = new UniversalHexGrid(5);
        grid_window = new GridWindow(this, 2);
        input_panel = new InputPanel(this);

        test_organism = new Organism(this);
        test_organism.assimilate_hex(0, -1, 1);
        test_organism.assimilate_hex(1, -2, 1);
        test_organism.assimilate_hex(1, -1, 0);
    }

    public void render() {
        ScreenUtils.clear(background_color);

        shape_renderer.setProjectionMatrix(main.camera.combined); // Ensure ShapeRenderer uses the correct projection
        grid_window.render(); // Make sure grid_window uses the correct projection too
        input_panel.render();  // Make sure input_panel uses the correct projection too
    }

    @Override
    public void dispose() {
        // Dispose of resources properly
        if (shape_renderer != null) shape_renderer.dispose();

        // Clean up any other resources like textures or sounds here
    }
}
