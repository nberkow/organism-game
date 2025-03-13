
package io.github.organism;

import java.awt.Point;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.Color;
public interface Player {

    public void queue_move(Integer move);
    public Integer get_move();

    public Color get_color();

    public int getIndex();

    public Integer on_empty_queue();

    String getPlayerName();

    Organism getOrganism();

    public float [] gather_inputs();

    LinkedList<Integer> get_move_queue();

    void generate_and_queue();

    int get_most_recent_move();

    void transition();

    void dispose();

    Point getTournamentId();

    Point get_ally_id();

    void set_ally_id(Point p);
}
