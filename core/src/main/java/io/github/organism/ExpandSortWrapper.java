package io.github.organism;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExpandSortWrapper implements Comparable<ExpandSortWrapper> {
    MapHex hex;
    Player current_player;
    Integer resource_value;



    public ExpandSortWrapper(MapHex h, Player p){
        hex = h;
        current_player = p;
        resource_value = 0;
    }
    public void compute_expand_value() {
        int [] resource_priority = current_player.get_organism().get_resource_priority();
        for (int i=0; i<3; i++){
            resource_value += resource_priority[hex.resources[i]];
        }
    }

    public ArrayList<MapVertex> get_target_vertices(){
        ArrayList<MapVertex> target_vertices = new ArrayList<>();
        for (MapVertex v : hex.vertex_list){
            if (v.player != current_player) {
                target_vertices.add(v);
            }
        }
        return target_vertices;
    }

    @Override
    public int compareTo(ExpandSortWrapper other_wrapper) {

        compute_expand_value();
        other_wrapper.compute_expand_value();
        return resource_value.compareTo(other_wrapper.resource_value);

    }


}

