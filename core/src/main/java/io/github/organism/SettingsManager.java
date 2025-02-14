package io.github.organism;

import com.badlogic.gdx.Screen;

public class SettingsManager {
    final static float VERTEX_COST_REMOVE_ENEMY = 6;
    final static float VERTEX_COST_REMOVE_NEUTRAL = 3;
    final static float VERTEX_COST_REMOVE_ALLY = 0;
    public static float VERTEX_COST_TAKE_VERTEX = 3f;
    public static float BASE_RESOURCE_VALUE = 1f;

    float remove_enemy_cost;
    float remove_neutral_cost;
    float remove_ally_cost;
    float base_resource_value;
    float take_vertex_cost;

    public SettingsManager(OrganismGame g, Screen scr){
        // set defaults
        float remove_enemy_cost = VERTEX_COST_REMOVE_ENEMY;
        float remove_neutral_cost = VERTEX_COST_REMOVE_NEUTRAL;
        float remove_ally_cost = VERTEX_COST_REMOVE_ALLY;
        float base_resource_value = VERTEX_COST_TAKE_VERTEX;
        float take_vertex_cost = BASE_RESOURCE_VALUE;
    }
}
