package io.github.organism.map;

import io.github.organism.player.Player;

public interface MapElement {


    public Player getPlayer();

    void render();

    boolean getMasked();
}
