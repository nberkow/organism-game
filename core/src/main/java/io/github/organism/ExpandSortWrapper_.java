package io.github.organism;


public class ExpandSortWrapper_ implements Comparable<ExpandSortWrapper_> {
    final int ENERGY_TO_CLAIM_NEUTRAL_VERTEX = 1;
    final int ENERGY_TO_CLAIM_OPPONENT_VERTEX = 1;
    final int ENERGY_TO_BREAK_HEX = 3;
    MapElement map_element;
    Player current_player;
    Integer resource_value;
    Integer energy_cost;

    public ExpandSortWrapper_(MapElement m, Player p){
        map_element = m;
        current_player = p;
        resource_value = 0;
        energy_cost = 0;
    }
    public int compute_hex_value(MapHex hex) {
        int val = 0;

        int [] resource_priority = current_player.getOrganism().get_resource_priority();
        for (int i=0; i<3; i++){
            val += resource_priority[hex.resources[i]] * 6;
        }

        for (MapVertex v : hex.vertex_list) {
            val += compute_vertex_value(v);
        }

        return val;
    }

    public int compute_hex_cost(MapHex hex) {
        int cost = 0;

        if (hex.player != null) {
            cost += ENERGY_TO_BREAK_HEX;
        }
        for (MapVertex v : hex.vertex_list) {
            if (v.player != current_player) {
                cost += ENERGY_TO_CLAIM_NEUTRAL_VERTEX;
                if (v.player != null) {
                    cost += ENERGY_TO_CLAIM_OPPONENT_VERTEX;
                }
            }
        }
        return cost;
    }

    public int compute_vertex_value(MapVertex vertex) {
        int vertex_val = 0;

        int [] resource_priority = current_player.getOrganism().get_resource_priority();
        for (MapHex hex : vertex.adjacent_hexes) {
            if (hex.player != current_player) {
                for (int i = 0; i < hex.total_resources; i++) {
                    vertex_val += resource_priority[hex.resources[i]];
                }
            }
        }
        return vertex_val;
    }

    public int compute_vertex_cost(MapVertex vertex) {
        int cost = 0;

        cost += ENERGY_TO_CLAIM_NEUTRAL_VERTEX;
        if (vertex.player != null) {
            cost += ENERGY_TO_CLAIM_OPPONENT_VERTEX;
        }

        for (MapHex hex : vertex.adjacent_hexes) {
            if (hex.player != null) {
                cost += ENERGY_TO_BREAK_HEX;
            }
        }
        return cost;
    }

    public void compute_value() {
        if (map_element instanceof MapHex) {
            resource_value = compute_hex_value((MapHex) map_element);
        }
        if (map_element instanceof MapVertex) {
            resource_value = compute_vertex_value((MapVertex) map_element);
        }
    }

    public void compute_cost() {
        if (map_element instanceof MapHex) {
            energy_cost = compute_hex_cost((MapHex) map_element);
        }
        if (map_element instanceof MapVertex) {
            energy_cost = compute_vertex_cost((MapVertex) map_element);
        }
    }

    @Override
    public int compareTo(ExpandSortWrapper_ other_wrapper) {
        return resource_value.compareTo(other_wrapper.resource_value);
    }


}

