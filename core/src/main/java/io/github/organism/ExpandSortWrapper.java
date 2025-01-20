package io.github.organism;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;


public class ExpandSortWrapper implements Comparable<ExpandSortWrapper> {


    MapVertex vertex;
    Player player;

    Integer adjacent_hex_completeness;
    Double best_enemy_distance;
    Integer total_adjacent_hex_value;
    Integer remove_player_cost;

    public ExpandSortWrapper(MapVertex v, Player p){
        vertex = v;
        player = p;
    }

    @Override
    public int compareTo(ExpandSortWrapper other_wrapper) {

        /*
        - prioritize
         - partially owned hexes
         - hexes with resources
         - direction of enemy
        - claim until out of budget
         */



        // higher is better
        if (!Objects.equals(adjacent_hex_completeness, other_wrapper.adjacent_hex_completeness)){
            return other_wrapper.adjacent_hex_completeness.compareTo(adjacent_hex_completeness);
        }

        // higher is better
        if (!Objects.equals(total_adjacent_hex_value, other_wrapper.total_adjacent_hex_value)){
            return other_wrapper.total_adjacent_hex_value.compareTo(total_adjacent_hex_value);
        }

        // lower is better
        if (!Objects.equals(best_enemy_distance, other_wrapper.best_enemy_distance)){
            return best_enemy_distance.compareTo(other_wrapper.best_enemy_distance);
        }

        // lower is better
        if (!Objects.equals(remove_player_cost, other_wrapper.remove_player_cost)){
            return remove_player_cost.compareTo(other_wrapper.remove_player_cost);
        }

        return 0;
    }


}

