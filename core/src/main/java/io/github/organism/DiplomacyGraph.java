

package io.github.organism;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class DiplomacyGraph {



    OrganismGame game;
    GameBoard current_game;

    HashSet<Player> players_counted;

    // rendering
    double [][] player_coords;
    Color [] player_colors;

    boolean render_params_set;


    final int VERTEX_COST_REMOVE_ENEMY_PLAYER = 9;
    final int VERTEX_COST_REMOVE_EXTRACTING_PLAYER = 3;
    final int VERTEX_COST_REMOVE_FLANKED_PLAYER = 1;

    HashMap<Player, HashMap<Player, String>> relationships;

    /* render params */
    float x_pos;
    float y_pos;
    float graph_radius;
    float player_radius;

    float line_midpoint;


    float span = 80; // Angular span in degrees
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
        line_midpoint = 0.7f;

        x_pos = game.VIRTUAL_WIDTH * 0.8f + (game.VIRTUAL_WIDTH / 12f);
        y_pos = game.VIRTUAL_HEIGHT * 0.85f;


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
            int [] p = current_game.all_player_ids.get(i);
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

    public void set_relationship(Player p, Player q, String r) {
        relationships.get(p).put(q, r);
        players_counted.add(p);
    }

    public void set_all_neutral() {
        for (Player p : current_game.players.values()) {
            relationships.put(p, new HashMap<>());
            for (Player q : current_game.players.values()) {
                if (p != q) {
                    relationships.get(p).put(q, "neutral");
                }
            }
        }
    }

    public void cancel_all_ally() {

        boolean all_ally = true;
        for (Player p : current_game.players.values()) {
            for (Player q : current_game.players.values()) {
                String relationship = relationships.get(p).get(q);
                if (!Objects.equals(relationship, "friendly")) {
                    all_ally = false;
                }
            }
        }

        if (all_ally) {
            set_all_neutral();
        }

    }

    public int get_remove_cost(Player player, Player target) {

        // if target player is expanding toward this player (hostile to player)
        if (Objects.equals(relationships.get(target).get(player), "hostile")){
            return VERTEX_COST_REMOVE_ENEMY_PLAYER;
        }

        // target player is extracting (friendly to all)
        if (Objects.equals(relationships.get(target).get(player), "friendly")){
            return VERTEX_COST_REMOVE_EXTRACTING_PLAYER;
        }

        // target player is not friendly, must be attacking other player
        return VERTEX_COST_REMOVE_FLANKED_PLAYER;
    }

    public Player get_ally(Player player) {
        cancel_all_ally();

        for (Player q : relationships.keySet()){
            if (Objects.equals(relationships.get(player).get(q), "friendly")) {
                if (Objects.equals(relationships.get(q).get(player), "friendly")) {

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


        if (!render_params_set) {
            calculate_render_params();
            render_params_set = true;
        }

        // outer relationship arcs
        game.shape_renderer.begin(ShapeRenderer.ShapeType.Line);
        Color color;

        for (int i=0; i<3; i++) {
            // Draw the arc as a polyline
            color = Color.DARK_GRAY;

            int [] p = current_game.all_player_ids.get(i);
            Player player = current_game.players.get(p);

            int [] q = current_game.all_player_ids.get((i + 1) % 3);
            Player opponent = current_game.players.get(q);

            String r = relationships.get(player).get(opponent);

            if (Objects.equals(r, "friendly")) {
                color = Color.BLUE;
            }

            if (Objects.equals(r, "hostile")) {
                color = Color.RED;
            }

            game.shape_renderer.setColor(color);
            game.shape_renderer.polyline(player_outer_arc_vertices[i]);

        }


        // inner relationship lines
        for (int i=0; i<3; i++) {
            // Draw the arc as a polyline
            color = Color.DARK_GRAY;

            int[] p = current_game.all_player_ids.get(i);
            Player player = current_game.players.get(p);

            int[] q = current_game.all_player_ids.get((i + 2) % 3);
            Player opponent = current_game.players.get(q);

            String r = relationships.get(player).get(opponent);

            if (Objects.equals(r, "friendly")) {
                color = Color.BLUE;
            }

            if (Objects.equals(r, "hostile")) {
                color = Color.RED;
            }

            game.shape_renderer.setColor(color);
            double end_x =  (player_coords[i][0] * (1-line_midpoint) + (player_coords[(i+2) % 3][0]) * (line_midpoint));
            double end_y =  (player_coords[i][1] * (1-line_midpoint) + (player_coords[(i+2) % 3][1]) * (line_midpoint));
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
}
