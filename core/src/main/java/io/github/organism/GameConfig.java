package io.github.organism;

import java.util.HashMap;

public class GameConfig {
    public int radius = 7;
    public long seed = 21;
    public float map_view_size_param = 130;

    public float resources = 1;
    public float vertex_density = 4;
    public String layout = "radial";
    public String difficulty = "easy";
    public HashMap<String, Float> gameplaySettings;
    int humanPlayers = 1;
    int botPlayers = 2;
    float playerStartPositions = 0;

    public GameConfig() {
        // Initialize gameplay settings with default values
        gameplaySettings = new HashMap<>();
        gameplaySettings.put("burn resource value", 6f);
        gameplaySettings.put("energy to expand", 6f);
    }

}


