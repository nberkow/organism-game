package io.github.organism;

public class GameTimers {

    double action_clock;
    double game_clock;

    GameTimers(){
        action_clock = 0d;
        game_clock = 0.0d;
    }

    public void add_time_delta(float delta_time) {
        action_clock += delta_time;
        game_clock += delta_time;
    }
}


