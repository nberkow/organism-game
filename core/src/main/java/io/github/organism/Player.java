
package io.github.organism;

import java.util.LinkedList;

public interface Player {

    public void queue_move(Integer move);
    public Integer get_move();

    public Integer on_empty_queue();

    String get_player_name();

    Organism get_organism();

    LinkedList<Integer> get_move_queue();

    void generate_and_queue();
}
