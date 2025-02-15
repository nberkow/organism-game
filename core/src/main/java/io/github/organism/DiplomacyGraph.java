

package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

enum Relationship {NEUTRAL, ENEMY, ALLY};

public class DiplomacyGraph {

    OrganismGame game;
    GameBoard current_game;

    SettingsManager settings_manager;
    HashSet<Player> players_counted;


    // rendering
    double [][] player_coords;
    Color [] player_colors;

    boolean render_params_set;

    HashMap<Point, HashMap<Point, Relationship>> relationships;

    /* render params */
    float x_pos;
    float y_pos;
    float graph_radius;
    float player_radius;
    float span = 120; // Angular span in degrees
    int segments = 30;

    float[][] player_outer_arc_vertices;
    double [] player_start_degrees;
    public DiplomacyGraph(OrganismGame g, GameBoard b) {

        game = g;
        current_game = b;
        render_params_set = false;

        // initialize relationships
        relationships = new HashMap<>();
        set_all_neutral();

        players_counted = new HashSet<>();

        graph_radius = game.VIRTUAL_WIDTH / 15f;
        player_radius = graph_radius / 3;

        x_pos = game.VIRTUAL_WIDTH * 0.8f + (game.VIRTUAL_WIDTH / 12f);
        y_pos = game.VIRTUAL_HEIGHT * 0.85f;

    }

    public void update_diplomacy(HashMap<Point, Integer> all_player_moves) {

        // get colors for this turn's actions
        set_minor_edge_colors(all_player_moves);

        // check for all extract moves.
        handle_extracting_players(all_player_moves);

        // check for expand moves
        handle_expanding_players(all_player_moves);
    }

    public void set_minor_edge_colors(HashMap<Point, Integer> all_player_moves) {

    }

    public void handle_extracting_players(HashMap<Point, Integer> all_player_moves) {

        // - if one player extracts, previous relationships are kept (overwritten if attacked)
        // - if two players extract they form an alliance
        // - if three players extract, previous relationships are kept (overwritten if attacked)
        // - if any players have empty queues don't update

        ArrayList<Point> extracting_players = new ArrayList<>();
        ArrayList<Point> expanding_players = new ArrayList<>();

        for (Point m : all_player_moves.keySet()) {
            if (all_player_moves.get(m) == null){
                return;
            }

            if (all_player_moves.get(m) == 1) {
                extracting_players.add(m);
            }
            else {
                expanding_players.add(m);
            }
        }

        // two players extract. alliance formed. other alliances broken
        if (extracting_players.size() == 2) {
            Point p = extracting_players.get(0);
            Point q = extracting_players.get(1);

            relationships.get(p).put(q, Relationship.ALLY);
            relationships.get(q).put(p, Relationship.ALLY);

            current_game.players.get(p).set_ally_id(q);
            current_game.players.get(q).set_ally_id(p);

            Point e = expanding_players.get(0);
            relationships.get(e).put(q, Relationship.NEUTRAL);
            relationships.get(e).put(p, Relationship.NEUTRAL);
        }
    }

    private void handle_expanding_players(HashMap<Point, Integer> all_player_moves) {

        // if a player attacks another player they become enemies.
        // if they were allies, alliance is canceled

        for (Integer m : all_player_moves.values()){
            if (m == null) {
                return;
            }
        }


        for (int i=0; i<3; i++) {

            Point p = current_game.all_player_ids.get(i);
            Integer move = all_player_moves.get(p);
            Point q = null;
            if (move == 0 | move == 2){

                // attacking clockwise (left)
                if (move == 0) {
                    q = current_game.all_player_ids.get((i + 2) % 3);
                }
                // attacking counter-clockwise (right)
                if (move == 2) {
                    q = current_game.all_player_ids.get((i + 2) % 3);
                }

                relationships.get(p).put(q, Relationship.ENEMY);
                relationships.get(q).put(p, Relationship.ENEMY);

                if (current_game.players.get(p).get_ally_id() == q) {
                    current_game.players.get(p).set_ally_id(null);
                }
                if (current_game.players.get(q).get_ally_id() == p) {
                    current_game.players.get(q).set_ally_id(null);
                }
            }
        }
    }

    private void calculate_render_params(){

        player_colors = new Color[3];
        player_coords = new double[3][2];

        player_start_degrees = new double[3];
        player_outer_arc_vertices = new float[3][segments * 2];

        double theta;

        for (int i=0; i<3; i++) {

            // calculate the player circle coordinates
            theta = ((i + 2.25) * 2d/3 * Math.PI);
            double [] coords = new double[] {
                Math.cos(theta) * graph_radius + x_pos,
                Math.sin(theta) * graph_radius + y_pos
            };
            player_coords[i] = coords;

            // record the player colors in order
            Point p = current_game.all_player_ids.get(i);
            Player player = current_game.players.get(p);
            player_colors[i] = player.get_color();

            // record the vertices of each player's arc
            double start = Math.toDegrees(theta);
            for (int j = 0; j < segments; j++) {
                float angle = (float) (start + (j / (float) (segments - 1)) * span); // Interpolate angle
                float angle_rad = (float) Math.toRadians(angle); // Convert to radians

                // Calculate the x and y positions (interleaved for polyline input)
                player_outer_arc_vertices[i][j * 2] = (float) (x_pos + Math.cos(angle_rad) * graph_radius);
                player_outer_arc_vertices[i][j * 2 + 1] = (float) (y_pos + Math.sin(angle_rad) * graph_radius);

            }
        }
    }

    public void set_all_neutral() {
        for (Point p : current_game.players.keySet()) {
            relationships.put(p, new HashMap<>());
            for (Point q : current_game.players.keySet()) {
                if (p != q) {
                    relationships.get(p).put(q, Relationship.NEUTRAL);
                }
            }
        }
    }

    public Float get_remove_cost(Point player, Point target) {

        if (Objects.equals(relationships.get(target).get(player), Relationship.ENEMY)){
            return current_game.config.gameplay_settings.get("attack enemy cost");
        }

        if (Objects.equals(relationships.get(target).get(player), Relationship.NEUTRAL)){
            return current_game.config.gameplay_settings.get("attack neutral cost");
        }

        if (Objects.equals(relationships.get(target).get(player), Relationship.ALLY)){
            return current_game.config.gameplay_settings.get("attack ally cost");
        }

        return null;
    }

    public Point get_ally(Point p) {

        for (Point q : relationships.keySet()){
            if (Objects.equals(relationships.get(p).get(q), Relationship.ALLY)) {
                if (Objects.equals(relationships.get(q).get(p), Relationship.ALLY)) {
                    return q;
                }
            }
        }
        return null;
    }

    public void set_pos(float x, float y) {
        x_pos = x;
        y_pos = y;
    }

    public void render(){

        if (game.shape_renderer == null) {
            return;
        }

        if (!render_params_set) {
            calculate_render_params();
            render_params_set = true;
        }

        // outer relationship arcs
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        Color color;

        for (int i=0; i<3; i++) {
            // Draw the arc as a poly rectline
            color = Color.DARK_GRAY;

            Point p = current_game.all_player_ids.get(i);
            Point q = current_game.all_player_ids.get((i + 1) % 3);

            Relationship r = relationships.get(p).get(q);
            if (r == Relationship.ALLY) {
                color = game.action_colors[1];
            }

            game.shape_renderer.setColor(color);
            game.shape_renderer.polyline(player_outer_arc_vertices[i]);

        }


        // inner relationship lines
        for (int i=0; i<3; i++) {
            // Draw the arc as a poly rectline
            color = Color.DARK_GRAY;

            Point p = current_game.all_player_ids.get(i);
            Point q = current_game.all_player_ids.get((i + 2) % 3);

            Relationship r = relationships.get(p).get(q);
            if (r == Relationship.ENEMY) {
                color = game.action_colors[0];
            }

            game.shape_renderer.setColor(color);
            double end_x =  player_coords[(i+2) % 3][0];
            double end_y =  player_coords[(i+2) % 3][1];
            game.shape_renderer.line((float) player_coords[i][0], (float) player_coords[i][1], (float) end_x, (float) end_y);

        }
        game.shape_renderer.end();

        // player circles
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0; i<3; i++) {

            game.shape_renderer.setColor(player_colors[i]);
            game.shape_renderer.circle(
                (float) player_coords[i][0],
                (float) player_coords[i][1],
                player_radius);
        }
        game.shape_renderer.end();
    }

    public void dispose() {
         relationships.clear();
    }


}
