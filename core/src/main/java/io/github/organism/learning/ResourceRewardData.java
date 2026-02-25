package io.github.organism.learning;
import io.github.organism.Organism;
import io.github.organism.map.MapHex;
import io.github.organism.map.MapVertex;
import io.github.organism.player.Player;

public class ResourceRewardData {
    // Lightweight snapshot for delta calculation
    public int territoryVertices;
    public int[] resources; // [type0, type1, type2]
    public float energy;
    public int claimedHexes;
    public int resourceHexes; // Hexes with at least one resource

    public ResourceRewardData(Player player) {

        //FIXME - re-implement all of this with functions in organism
        Organism org = player.getOrganism();
        this.territoryVertices = org.getTerritoryVertex().size();

        this.resources = new int[3];
        System.arraycopy(org.resources, 0, this.resources, 0, 3);

        this.energy = org.energy;
        this.claimedHexes = org.getTerritoryHex().size();
        this.resourceHexes = countResourceHexes(org);
    }

    private int countResourceHexes(Organism org) {
        int count = 0;
        return count;
    }

    // Calculate reward based on deltas
    public static float calculateDeltaReward(ResourceRewardData before,
                                             ResourceRewardData after,
                                             float discountFactor) {
        float reward = 0;

        // 1. Territory growth (discounted)
        int newVertices = after.territoryVertices - before.territoryVertices;
        reward += newVertices * 0.1f * discountFactor;

        // 2. Resource collection (discounted)
        for (int i = 0; i < 3; i++) {
            int newResources = after.resources[i] - before.resources[i];
            reward += newResources * 0.05f * discountFactor;
        }

        // 3. Energy gain/loss
        float energyDelta = after.energy - before.energy;
        reward += energyDelta * 0.01f;

        // 4. Hex completion bonus (undiscounted - immediate)
        int newHexes = after.claimedHexes - before.claimedHexes;
        reward += newHexes * 0.2f; // Bigger bonus for completing hexes

        // 5. Resource hex acquisition
        int newResourceHexes = after.resourceHexes - before.resourceHexes;
        reward += newResourceHexes * 0.3f; // Biggest bonus

        // 6. Small penalty for stagnation
        if (newVertices == 0 && newResourceHexes == 0) {
            reward -= 0.05f;
        }

        // Clip to reasonable range
        return Math.max(-1.0f, Math.min(1.0f, reward));
    }

    // For reporting
    public String toString() {
        return String.format("V:%d R:[%d,%d,%d] E:%.1f H:%d RH:%d",
            territoryVertices, resources[0], resources[1], resources[2],
            energy, claimedHexes, resourceHexes);
    }
}
