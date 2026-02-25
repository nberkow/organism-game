package io.github.organism.learning;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.organism.Organism;
import io.github.organism.player.Player;

public class PerformanceReporter {
    private String logFile;
    private SimpleDateFormat dateFormat;
    private int gameTurn = 0;
    private int totalVerticesClaimed = 0;
    private int totalResourcesCollected = 0;
    private long gameStartTime;

    // For map fill tracking
    private int initialUnclaimedVertices;
    private int currentUnclaimedVertices;

    public PerformanceReporter(String basePath) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
        String timestamp = dateFormat.format(new Date());
        this.logFile = basePath + "/rl_performance_" + timestamp + ".csv";

        // Create CSV header
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println("timestamp,turn,vertices_claimed,total_resources,energy," +
                "unclaimed_vertices,time_since_start,avg_reward");
        } catch (IOException e) {
            System.err.println("Could not create log file: " + e.getMessage());
        }
    }

    public void initGame(int totalVertices) {
        gameTurn = 0;
        totalVerticesClaimed = 0;
        totalResourcesCollected = 0;
        initialUnclaimedVertices = totalVertices;
        currentUnclaimedVertices = totalVertices;
        gameStartTime = System.currentTimeMillis();
    }

    public void recordTurn(Player player, float avgReward) {
        gameTurn++;
        Organism org = player.getOrganism();

        int verticesThisTurn = org.getTerritoryVertex().size();
        int resourcesThisTurn = org.resources[0] + org.resources[1] + org.resources[2];

        // Update totals
        totalVerticesClaimed = verticesThisTurn;
        totalResourcesCollected = resourcesThisTurn;
        currentUnclaimedVertices = initialUnclaimedVertices - verticesThisTurn;

        // Calculate time
        long elapsedMs = System.currentTimeMillis() - gameStartTime;
        float elapsedSeconds = elapsedMs / 1000.0f;

        // Log to CSV
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.printf("%s,%d,%d,%d,%.1f,%d,%.1f,%.3f%n",
                dateFormat.format(new Date()),
                gameTurn,
                totalVerticesClaimed,
                totalResourcesCollected,
                org.energy,
                currentUnclaimedVertices,
                elapsedSeconds,
                avgReward
            );
        } catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }

        // Also print to console every N turns
        if (gameTurn % 50 == 0) {
            System.out.println(getTurnSummary(player, avgReward));
        }
    }

    public String getTurnSummary(Player player, float avgReward) {
        Organism org = player.getOrganism();
        long elapsedMs = System.currentTimeMillis() - gameStartTime;
        float elapsedSeconds = elapsedMs / 1000.0f;

        float fillPercentage = (float) totalVerticesClaimed / initialUnclaimedVertices * 100;
        float verticesPerSecond = totalVerticesClaimed / elapsedSeconds;

        return String.format(
            "=== Turn %d ===\n" +
                "Fill: %.1f%% (%d/%d)\n" +
                "Resources: %d total\n" +
                "Energy: %.1f\n" +
                "Rate: %.2f vertices/sec\n" +
                "Avg Reward: %.3f\n" +
                "Time: %.1f seconds",
            gameTurn,
            fillPercentage, totalVerticesClaimed, initialUnclaimedVertices,
            totalResourcesCollected,
            org.energy,
            verticesPerSecond,
            avgReward,
            elapsedSeconds
        );
    }

    public String getFinalReport() {
        long elapsedMs = System.currentTimeMillis() - gameStartTime;
        float elapsedSeconds = elapsedMs / 1000.0f;
        float verticesPerSecond = totalVerticesClaimed / elapsedSeconds;

        return String.format(
            "=== GAME COMPLETE ===\n" +
                "Total turns: %d\n" +
                "Final fill: %.1f%%\n" +
                "Total resources: %d\n" +
                "Average rate: %.2f vertices/sec\n" +
                "Total time: %.1f seconds",
            gameTurn,
            (float) totalVerticesClaimed / initialUnclaimedVertices * 100,
            totalResourcesCollected,
            verticesPerSecond,
            elapsedSeconds
        );
    }
}
