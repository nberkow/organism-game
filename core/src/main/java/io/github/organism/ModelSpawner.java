package io.github.organism;

import java.awt.Point;

public class ModelSpawner {
    
    private int nextPrimaryIndex;
    
    public ModelSpawner(int startingIndex) {
        this.nextPrimaryIndex = startingIndex;
    }
    
    /**
     * Create a new model with random weights
     * @return A new Model with random initialization
     */
    public Model createRandomModel() {
        Model model = new Model();
        model.init_random_weights();
        
        Point playerId = new Point(nextPrimaryIndex, 0);
        model.setPlayerTournamentId(playerId);
        nextPrimaryIndex++;
        
        return model;
    }
    
    /**
     * Get the next available primary index
     */
    public int getNextPrimaryIndex() {
        return nextPrimaryIndex;
    }
    
    /**
     * Set the primary index (useful for loading saved state)
     */
    public void setNextPrimaryIndex(int index) {
        this.nextPrimaryIndex = index;
    }
}
