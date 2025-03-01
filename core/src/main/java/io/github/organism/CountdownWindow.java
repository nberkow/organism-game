package io.github.organism;

import com.badlogic.gdx.Screen;

public class CountdownWindow {

    int current_number;
    boolean visible;

    float x_pos;

    float y_pos;

    OrganismGame game;
    Screen screen;

    public CountdownWindow(OrganismGame g, Screen scr) {
        game = g;
        screen = scr;
    }

    public void decrement() {
        current_number -= 1;
    }

    public void set(int i) {
        current_number = i;
    }

    public void render() {

    }
}
