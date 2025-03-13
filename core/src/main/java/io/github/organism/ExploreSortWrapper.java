package io.github.organism;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ExploreSortWrapper implements Comparable<ExploreSortWrapper> {
    final int ENERGY_TO_CLAIM_NEUTRAL_VERTEX = 1;
    final int ENERGY_TO_CLAIM_OPPONENT_VERTEX = 1;
    final int ENERGY_TO_BREAK_HEX = 3;
    MapVertex vertex;
    Player current_player;
    Integer resource_value; // takes into account unclaimed-ness of hexes
    Integer energy_cost;

    Integer unclaimed_neighbors;
    Integer undiscovered_value;


    public ExploreSortWrapper(MapVertex v, Player p){
        vertex = v;
        current_player = p;
        energy_cost = 0;
        unclaimed_neighbors = 0;
        undiscovered_value = 0;
    }


    public void compute_and_store_vertex_properties(MapVertex vertex) {

        // count up the number of unclaimed vertexes in the surrounding hexes

        // count the resources in hexes with zero owned vertexes

        int [] resource_priority = current_player.getOrganism().get_resource_priority();
        Set<MapVertex> unique_neighbors = new HashSet<>();
        resource_value = 0;

        int undiscovered_vertexes;
        for (MapHex hex : vertex.adjacent_hexes) {

            undiscovered_vertexes = 0;
            for (MapVertex v : hex.vertex_list) {
                if (v.player != current_player && !v.masked) {
                    unique_neighbors.add(v);
                    undiscovered_vertexes++;
                }
            }

            if (hex.masked) {
                for (int i = 0; i < hex.total_resources; i++) {
                    resource_value += (int) Math.pow(resource_priority[hex.resources[i]], undiscovered_vertexes);
                }
            }
        }
        unclaimed_neighbors = Math.max(9, unique_neighbors.size());
    }

    public int compute_vertex_cost(MapVertex vertex) {
        int cost = ENERGY_TO_CLAIM_NEUTRAL_VERTEX;
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

    public int compute_cost(MapVertex vertex) {
        return compute_vertex_cost(vertex);
    }

    @Override
    public int compareTo(ExploreSortWrapper other_wrapper) {
        if (!Objects.equals(unclaimed_neighbors, other_wrapper.unclaimed_neighbors)) {
            return unclaimed_neighbors.compareTo(other_wrapper.unclaimed_neighbors);
        }
        return resource_value.compareTo(other_wrapper.resource_value);
    }


}

