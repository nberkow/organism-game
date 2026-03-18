package io.github.organism;

import com.badlogic.gdx.Screen;

import java.util.HashMap;

public class SettingsManager {
    public static float MAX_ENERGY = 100f;
    public static float DEFAULT_STARTING_ENERGY = 20f; // As percentage of MAX_ENERGY
    public static float BASE_INCOME_PERCENT = 4f; // Percentage of MAX_ENERGY (2^-1 = 0.5% default)
    public static float VERTEX_ENERGY_COST = 6f; // Cost to claim a vertex

    static {
        // Log initial values when class is loaded
        io.github.organism.hud.DebugLogger logger = io.github.organism.hud.DebugLogger.getInstance();
        logger.log("=== SettingsManager initialized with defaults ===");
        logger.log("  MAX_ENERGY: " + MAX_ENERGY);
        logger.log("  DEFAULT_STARTING_ENERGY: " + DEFAULT_STARTING_ENERGY + "%");
        logger.log("  BASE_INCOME_PERCENT: " + BASE_INCOME_PERCENT + "%");
        logger.log("  VERTEX_ENERGY_COST: " + VERTEX_ENERGY_COST);
    }
}
