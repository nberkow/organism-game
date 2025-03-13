package io.github.organism;

import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.LinkedList;

public class DummyPlayer implements Player{
    /**
     * @param move
     */
    @Override
    public void queue_move(Integer move) {

    }

    /**
     * @return
     */
    @Override
    public Integer get_move() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public Color get_color() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public int getIndex() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public Integer on_empty_queue() {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public String getPlayerName() {
        return "";
    }

    /**
     * @return
     */
    @Override
    public Organism getOrganism() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public float[] gather_inputs() {
        return new float[0];
    }

    /**
     * @return
     */
    @Override
    public LinkedList<Integer> get_move_queue() {
        return null;
    }

    /**
     *
     */
    @Override
    public void generate_and_queue() {

    }

    /**
     * @return
     */
    @Override
    public int get_most_recent_move() {
        return 0;
    }

    /**
     *
     */
    @Override
    public void transition() {

    }

    /**
     *
     */
    @Override
    public void dispose() {

    }

    /**
     * @return
     */
    @Override
    public Point getTournamentId() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public Point get_ally_id() {
        return null;
    }

    /**
     * @param p
     */
    @Override
    public void set_ally_id(Point p) {

    }
}
