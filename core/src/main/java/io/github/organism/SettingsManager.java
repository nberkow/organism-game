package io.github.organism;

import com.badlogic.gdx.Screen;

import java.util.HashMap;

public class SettingsManager {
    // Resource and expansion settings
    public static final float RESOURCE_UNIT_VALUE = 6f;
    public static final float ENERGY_TO_EXPAND = 6f;
    public static final float VERTEX_COST_TAKE_VERTEX = 3f;
    
    // Energy settings - these can be modified via settings menu
    public static float MAX_ENERGY = 100f;
    public static float DEFAULT_STARTING_ENERGY = 20f; // As percentage of MAX_ENERGY
    public static float BASE_INCOME_PERCENT = 0.5f; // Percentage of MAX_ENERGY (0.5% default)
    public static float VERTEX_ENERGY_COST = 6f; // Cost to claim a vertex

}
