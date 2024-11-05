package io.github.organism;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



public class ExpandSortWrapper implements Comparable<ExpandSortWrapper> {

    final int ENERGY_TO_CLAIM_NEUTRAL_VERTEX = 1;
    final int ENERGY_TO_CLAIM_OPPONENT_VERTEX = 1;
    final int ENERGY_TO_BREAK_HEX = 3;
    MapElement map_element;
    Player current_player;
    Integer resource_value;

    Integer energy_cost;

    public ExpandSortWrapper(MapElement m, Player p){
        map_element = m;
        current_player = p;
        resource_value = 0;
        energy_cost = 0;
    }
    public void compute_hex_value() {
        MapHex hex = (MapHex) map_element;
        resource_value = 0;

        int [] resource_priority = current_player.get_organism().get_resource_priority();
        for (int i=0; i<3; i++){
            resource_value += resource_priority[hex.resources[i]];
        }
    }

    public void compute_hex_cost() {
        energy_cost = 0;
        MapHex hex = (MapHex) map_element;

        if (hex.player != null) {
            energy_cost += ENERGY_TO_BREAK_HEX;
        }
        for (MapVertex v : hex.vertex_list) {
            if (v.player != current_player) {
                energy_cost += ENERGY_TO_CLAIM_NEUTRAL_VERTEX;
                if (v.player != null) {
                    energy_cost += ENERGY_TO_CLAIM_OPPONENT_VERTEX;
                }
            }
        }
    }

    public void compute_vertex_value() {
        MapVertex vertex = (MapVertex) map_element;
        resource_value = 0;

        int [] resource_priority = current_player.get_organism().get_resource_priority();

        for (MapHex hex : vertex.adjacent_hexes) {
            if (hex.player != current_player) {
                for (int i = 0; i < 3; i++) {
                    int val = resource_priority[hex.resources[i]];
                    if (val > resource_value) {
                        resource_value = val; // consider only the best hex
                    }
                }
            }
        }
    }

    public void compute_vertex_cost() {
        energy_cost = 0;
        MapVertex vertex = (MapVertex) map_element;

        energy_cost += ENERGY_TO_CLAIM_NEUTRAL_VERTEX;
        if (vertex.player != null) {
            energy_cost += ENERGY_TO_CLAIM_OPPONENT_VERTEX;
        }

        for (MapHex hex : vertex.adjacent_hexes) {
            if (hex.player != null) {
                energy_cost += ENERGY_TO_BREAK_HEX;
            }
        }
    }


    public void compute_value() {
        if (map_element instanceof MapHex) {
            compute_hex_value();
        }
        if (map_element instanceof MapVertex) {
            compute_vertex_value();
        }
    }

    public void compute_cost() {
        if (map_element instanceof MapHex) {
            compute_hex_cost();
        }
        if (map_element instanceof MapVertex) {
            compute_vertex_cost();
        }
    }

    @Override
    public int compareTo(ExpandSortWrapper other_wrapper) {
        return resource_value.compareTo(other_wrapper.resource_value);
    }


}

