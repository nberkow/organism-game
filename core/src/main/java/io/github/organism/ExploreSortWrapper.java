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


    public void compute_vertex_value() {
        resource_value = 0;
        int [] resource_priority = current_player.get_organism().get_resource_priority();

        int vertexes_already_claimed;
        for (MapHex hex : vertex.adjacent_hexes) {
            vertexes_already_claimed = 0;
            for (MapVertex v : hex.vertex_list){
                if (v.player == current_player){
                    vertexes_already_claimed += 1;
                }
            }
            //System.out.println("adjacent hex " + h + " tot: " + hex.total_resources);

            // ignore value of hexes that already have 2 owned vertices
            if (vertexes_already_claimed <= 3) {
                for (int i = 0; i < hex.total_resources; i++) {
                    int val = resource_priority[hex.resources[i]];
                    resource_value += val * (6 - vertexes_already_claimed); // consider all adjacent hexes
                }
            }
            else {
                resource_value = -1;
            }
        }
    }

    public void compute_vertex_cost() {
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
        compute_vertex_value();
    }

    public void compute_cost() {
        compute_vertex_cost();
    }

    @Override
    public int compareTo(ExploreSortWrapper other_wrapper) {
        return resource_value.compareTo(other_wrapper.resource_value);
    }


}

