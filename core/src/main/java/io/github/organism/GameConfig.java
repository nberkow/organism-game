package io.github.organism;

import java.util.HashMap;

public class GameConfig {
    public int radius = 7;
    public long seed = 21;
    public float map_view_size_param = 130;

    public float resources = 3;
    public float vertex_density = 4;
    public String layout = "radial";
    public String difficulty = "easy";
    public HashMap<String, Float> gameplaySettings;
    int humanPlayers = 1;
    int botPlayers = 2;
    float playerStartPositions = 0;

}


