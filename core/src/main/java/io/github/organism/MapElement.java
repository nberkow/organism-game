package io.github.organism;

import com.badlogic.gdx.graphics.Color;

public interface MapElement {

    public char get_type();

    public Player get_player();

    void render();

}
