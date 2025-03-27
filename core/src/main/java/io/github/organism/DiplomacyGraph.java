

package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import io.github.organism.map.MapVertex;
import io.github.organism.player.Player;

enum Relationship {NEUTRAL, ENEMY, ALLY};

public class DiplomacyGraph {

    OrganismGame game;
    GameBoard currentGame;

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
        currentGame = b;
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

        // check for all extract moves.
        handleExtractingPlayers(all_player_moves);

        // check for expand moves
        handleExpandingPlayers(all_player_moves);
    }


    public void handleExtractingPlayers(HashMap<Point, Integer> all_player_moves) {

        // - if one player extracts, previous relationships are kept (overwritten if attacked)
        // - if two players extract they form an alliance
        // - if three players extract, previous relationships are kept (overwritten if attacked)
        // - if any players have empty queues don't update

        ArrayList<Point> extracting_players = new ArrayList<>();
        ArrayList<Point> other_players = new ArrayList<>();

        for (Point m : all_player_moves.keySet()) {
            if (all_player_moves.get(m) != null) {
                if (all_player_moves.get(m) == 1) {
                    extracting_players.add(m);
                }
                else {
                    other_players.add(m);
                }
            }
        }

        // two players extract. alliance formed. other alliances broken
        if (extracting_players.size() == 2) {
            Point p = extracting_players.get(0);
            Point q = extracting_players.get(1);


            relationships.get(p).put(q, Relationship.ALLY);
            relationships.get(q).put(p, Relationship.ALLY);

            currentGame.players.get(p).setAllyId(q);
            currentGame.players.get(q).setAllyId(p);

            if (other_players.size() == 1) {
                Point e = other_players.get(0);
                currentGame.players.get(e).setAllyId(null);

                if (relationships.get(e).get(p) != Relationship.ENEMY) {
                    relationships.get(e).put(p, Relationship.NEUTRAL);
                }

                if (relationships.get(e).get(q) != Relationship.ENEMY) {
                    relationships.get(e).put(q, Relationship.NEUTRAL);
                }
            }
        }
    }

    private void handleExpandingPlayers(HashMap<Point, Integer> allPlayerMoves) {

        // if a player attacks another player they become enemies.
        // if they were allies, alliance is canceled

        for (int i=0; i<3; i++) {

            Point p = currentGame.allPlayerIds.get(i);
            Integer move = allPlayerMoves.get(p);

            if (move != null) {
                if (move == 0 | move == 2) {
                    Point q = null;
                    // attacking clockwise (left)
                    if (move == 0) {
                        q = currentGame.allPlayerIds.get((i + 2) % 3);
                    }
                    // attacking counter-clockwise (right)
                    if (move == 2) {
                        q = currentGame.allPlayerIds.get((i + 1) % 3);
                    }

                    if (relationships.get(p) != null) {
                        relationships.get(p).put(q, Relationship.ENEMY);
                    }

                    if (relationships.get(q) != null) {
                        relationships.get(q).put(p, Relationship.ENEMY);
                    }

                    if (currentGame.players.get(p).getAllyId() == q) {
                        currentGame.players.get(p).setAllyId(null);
                    }
                    if (currentGame.players.get(q).getAllyId() == p) {
                        currentGame.players.get(q).setAllyId(null);
                    }
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
            Point p = currentGame.allPlayerIds.get(i);
            Player player = currentGame.players.get(p);
            player_colors[i] = player.getColor();

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
        for (Point p : currentGame.players.keySet()) {
            relationships.put(p, new HashMap<>());
            for (Point q : currentGame.players.keySet()) {
                if (p != q) {
                    relationships.get(p).put(q, Relationship.NEUTRAL);
                }
            }
        }
    }

    public Float getRemoveCost(MapVertex v, Point player) {

        Player vertex_owner = v.getPlayer();
        if (vertex_owner == null){
            return 0f;
        }

        if (Objects.equals(relationships.get(vertex_owner.getTournamentId()).get(player), Relationship.ENEMY)){
            return currentGame.config.gameplaySettings.get("attack enemy cost");
        }

        if (Objects.equals(relationships.get(vertex_owner.getTournamentId()).get(player), Relationship.NEUTRAL)){
            return currentGame.config.gameplaySettings.get("attack neutral cost");
        }

        if (Objects.equals(relationships.get(vertex_owner.getTournamentId()).get(player), Relationship.ALLY)){
            return currentGame.config.gameplaySettings.get("attack ally cost");
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

        if (game.shapeRenderer == null) {
            return;
        }

        if (!render_params_set) {
            calculate_render_params();
            render_params_set = true;
        }

        // outer relationship arcs
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Color color;

        for (int i=0; i<3; i++) {
            // Draw the arc as a polyline
            color = Color.DARK_GRAY;

            Point p = currentGame.allPlayerIds.get(i);
            Point q = currentGame.allPlayerIds.get((i + 1) % 3);

            Relationship r = relationships.get(p).get(q);
            if (r == Relationship.ALLY) {
                color = game.action_colors[1];
            }

            game.shapeRenderer.setColor(color);
            game.shapeRenderer.polyline(player_outer_arc_vertices[i]);

        }


        // inner relationship lines
        for (int i=0; i<3; i++) {
            // Draw the arc as a polyline
            color = Color.DARK_GRAY;

            Point p = currentGame.allPlayerIds.get(i);
            Point q = currentGame.allPlayerIds.get((i + 2) % 3);

            Relationship r = relationships.get(p).get(q);
            if (r == Relationship.ENEMY) {
                color = game.action_colors[0];
            }

            game.shapeRenderer.setColor(color);
            double end_x =  player_coords[(i+2) % 3][0];
            double end_y =  player_coords[(i+2) % 3][1];
            game.shapeRenderer.line((float) player_coords[i][0], (float) player_coords[i][1], (float) end_x, (float) end_y);

        }
        game.shapeRenderer.end();

        // player circles
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0; i<3; i++) {

            game.shapeRenderer.setColor(player_colors[i]);
            game.shapeRenderer.circle(
                (float) player_coords[i][0],
                (float) player_coords[i][1],
                player_radius);
        }
        game.shapeRenderer.end();
    }

    public void dispose() {
         relationships.clear();
    }


}
