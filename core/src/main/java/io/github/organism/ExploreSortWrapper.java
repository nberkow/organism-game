package io.github.organism;


public class ExploreSortWrapper implements Comparable<ExploreSortWrapper> {
    final int ENERGY_TO_CLAIM_NEUTRAL_VERTEX = 1;
    final int ENERGY_TO_CLAIM_OPPONENT_VERTEX = 1;
    final int ENERGY_TO_BREAK_HEX = 3;
    MapVertex vertex;
    Player current_player;
    Integer resource_value;
    Integer energy_cost;

    public ExploreSortWrapper(MapVertex v, Player p){
        vertex = v;
        current_player = p;
        resource_value = 0;
        energy_cost = 0;
    }


    public int compute_vertex_value(MapVertex vertex) {
        int value = 0;
        int [] resource_priority = current_player.get_organism().get_resource_priority();

        // A vertex is valued by the resources of its most unclaimed hex
        int best_unclaimed_count = 0;
        int total_unclaimed_count = 0;
        int best_hex_val = 0;
        int total_val = 0;
        for (MapHex hex : vertex.adjacent_hexes) {

            // count the number of unclaimed vertexes
            int vertexes_unclaimed = 6;
            for (MapVertex v : hex.vertex_list){
                if (v.player == current_player){
                    vertexes_unclaimed -= 1;
                }
            }
            total_unclaimed_count += vertexes_unclaimed;

            // calculate the score to break ties
            int hex_val = 0;
            for (int i=0; i<hex.total_resources; i++){
                hex_val += resource_priority[hex.resources[i]];
            }
            total_val += hex_val;

            if (vertexes_unclaimed == best_unclaimed_count) {
                if (hex_val > best_hex_val) {
                    best_hex_val = hex_val;
                }
            }

            if (vertexes_unclaimed > best_unclaimed_count) {
                best_unclaimed_count = vertexes_unclaimed;
                best_hex_val = hex_val;
            }
        }
        return 2 * (best_unclaimed_count) * (best_hex_val + 1) + (total_unclaimed_count);
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

    public int compute_value(MapVertex vertex) {
        return compute_vertex_value(vertex);
    }

    public int compute_cost(MapVertex vertex) {
        return compute_vertex_cost(vertex);
    }

    @Override
    public int compareTo(ExploreSortWrapper other_wrapper) {
        return resource_value.compareTo(other_wrapper.resource_value);
    }


}

