
package io.github.organism;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.Color;
public interface Player {

    public void queue_move(Integer move);
    public Integer get_move();

    public Color get_color();

    public int get_index();

    public Integer on_empty_queue();

    String get_player_name();

    Organism get_organism();

    LinkedList<Integer> get_move_queue();

    void generate_and_queue();

    int get_most_recent_move();

    void transition();

    void dispose();

    Point get_tournament_id();
}
