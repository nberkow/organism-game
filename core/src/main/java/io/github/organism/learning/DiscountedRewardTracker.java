package io.github.organism.learning;
import java.util.*;

public class DiscountedRewardTracker {
    private List<Float> recentRewards = new ArrayList<>();
    private List<Integer> recentTurns = new ArrayList<>();
    private float gamma; // Discount factor

    public DiscountedRewardTracker(float gamma) {
        this.gamma = gamma; // Typically 0.9-0.99
    }

    // Add a reward with turn timestamp
    public void addReward(float reward, int turn) {
        recentRewards.add(reward);
        recentTurns.add(turn);

        // Keep only recent history for efficiency
        if (recentRewards.size() > 100) {
            recentRewards.remove(0);
            recentTurns.remove(0);
        }
    }

    // Calculate discounted return for recent actions
    public float getDiscountedReturn(int currentTurn, int horizon) {
        float total = 0;
        float discount = 1.0f;

        for (int i = recentRewards.size() - 1; i >= 0; i--) {
            int turnsAgo = currentTurn - recentTurns.get(i);
            if (turnsAgo > horizon) break;

            total += recentRewards.get(i) * discount;
            discount *= gamma;

            if (discount < 0.01f) break; // Negligible
        }

        return total;
    }

    // For reporting
    public String getRecentRewardSummary() {
        if (recentRewards.isEmpty()) return "No rewards yet";

        float sum = 0;
        float max = Float.NEGATIVE_INFINITY;
        float min = Float.POSITIVE_INFINITY;

        for (float r : recentRewards) {
            sum += r;
            max = Math.max(max, r);
            min = Math.min(min, r);
        }

        float avg = sum / recentRewards.size();

        return String.format("Rewards: avg=%.3f min=%.3f max=%.3f n=%d",
            avg, min, max, recentRewards.size());
    }
}
