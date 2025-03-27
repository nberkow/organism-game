package io.github.organism.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.HashSet;

import io.github.organism.player.Player;

public class MapHex implements MapElement{

    public final float RESOURCE_JITTER = 0.4f;

    public final float RESOURCE_RADIUS = .15f;
    public GridPosition pos;
    public MapVertex [] vertexList;

    public Integer [] resources;
    public int totalResources;;

    public Player player;

    public boolean masked = false;

    public MapHex(GridPosition p){
        pos = p;
        vertexList = new MapVertex [] {null, null, null, null, null, null};
        resources = new Integer[3];
        for (int i=0; i<3; i++) {
            resources[i] = 0;
        }
        totalResources = 0;
    }

    public void add_resource(int res, int amount) {
        for (int i=0; i<amount; i++) {
            if (totalResources < 3) {
                resources[totalResources] = res;
                totalResources++;
            }
        }
    }

    public void add_resource(int resource_type) {
        resources[totalResources] = resource_type;
        totalResources += 1;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void render() {
        if (!masked) {
            render_resources();
            render_players();
        }
    }

    /**
     * @return
     */
    @Override
    public boolean getMasked() {
        return masked;
    }

    public void render_players() {

        Color c;
        pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int v=0; v<6; v++){
            MapVertex v1 = vertexList[v];
            if (!v1.masked) {
                float x1 = (float) ((v1.pos.j * Math.pow(3f, 0.5f) / 2f) - (v1.pos.k * Math.pow(3f, 0.5f) / 2f));
                float y1 = v1.pos.i - v1.pos.j / 2f - v1.pos.k / 2f;

                MapVertex v2 = vertexList[(v + 1) % 6];
                float x2 = (float) ((v2.pos.j * Math.pow(3f, 0.5f) / 2f) - (v2.pos.k * Math.pow(3f, 0.5f) / 2f));
                float y2 = v2.pos.i - v2.pos.j / 2f - v2.pos.k / 2f;

                c = pos.grid.gameBoard.game.backgroundColor;
                if (v1.player != null && v2.player == v1.player) {
                    c = v2.player.getColor();
                } else {
                    if (is_common_hex_masked(v1, v2)) {
                        c = Color.DARK_GRAY;
                    }
                }

                pos.grid.gameBoard.game.shapeRenderer.setColor(c);

                pos.grid.gameBoard.game.shapeRenderer.line(
                    x1 * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                    y1 * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY,
                    x2 * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                    y2 * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY);
            }
        }
        pos.grid.gameBoard.game.shapeRenderer.end();
    }

    private boolean is_common_hex_masked(MapVertex v1, MapVertex v2) {

        boolean masked = false;
        HashSet<MapHex> intersection = new HashSet<>(v1.adjacentHexes);
        intersection.retainAll(v2.adjacentHexes);

        if (v1.adjacentHexes.size() < 3 && v2.adjacentHexes.size() < 3){
            return true;
        }

        for (MapHex hex : new ArrayList<>(intersection)) {
            if (hex.masked) {
                masked = true;
                break;
            }
        }

        return masked;
    }

    public void render_resources(){
        float [] j = {0f, 0f, RESOURCE_JITTER};

        int n=0;
        pos.grid.gameBoard.game.shapeRenderer.end();

        for (int r = 0; r< totalResources; r++){
            pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float i_f = pos.i + j[n];
            float j_f = pos.j + j[(n + 1) % 3];
            float k_f = pos.k + j[(n + 2) % 3];

            float x = (float) ((j_f * Math.pow(3f, 0.5f) / 2f) - (k_f * Math.pow(3f, 0.5f) / 2f));
            float y = i_f - j_f/2f - k_f/2f;

            Color c1 = pos.grid.gameBoard.game.resourceColorsDark[resources[r]];
            Color c2 = pos.grid.gameBoard.game.resourceColorsBright[resources[r]];

            pos.grid.gameBoard.game.shapeRenderer.setColor(c1);
            pos.grid.gameBoard.game.shapeRenderer.circle(
                x * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                y * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY,
                RESOURCE_RADIUS * pos.grid.gameBoard.hexSideLen);

            pos.grid.gameBoard.game.shapeRenderer.end();
            pos.grid.gameBoard.game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            pos.grid.gameBoard.game.shapeRenderer.setColor(c2);
            pos.grid.gameBoard.game.shapeRenderer.circle(
                x * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerX,
                y * pos.grid.gameBoard.hexSideLen + pos.grid.gameBoard.centerY,
                RESOURCE_RADIUS * pos.grid.gameBoard.hexSideLen);

            pos.grid.gameBoard.game.shapeRenderer.end();
            n++;
        }

    }

    public Integer [] get_resources() {
        return resources;
    }


}
