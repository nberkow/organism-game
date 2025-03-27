package io.github.organism.player;

import com.badlogic.gdx.graphics.Color;

import java.awt.Point;
import java.util.LinkedList;

import io.github.organism.GameBoard;
import io.github.organism.Model;
import io.github.organism.Organism;
import io.github.organism.Simulation;

public class BotPlayer implements Player {

    public LinkedList<Integer> move_queue;
    //public ActionHistory move_history;

    public GameBoard gameBoard;
    public int gameIndex; // index within a single game
    public Point tournament_id; // id in tournament or other large player collection

    public Color color;
    public String playerName;

    public Model model;

    public Organism organism;

    int most_recent_move;

    Point allyId;

    public BotPlayer(GameBoard gb, String name, int idx, Point id, Organism org, Model mod, Color c){

        gameBoard = gb;
        color = c;
        playerName = name;
        tournament_id = id;
        gameIndex = idx;
        organism = org;
        model = mod;
        move_queue = new LinkedList<>();
        allyId = null;

    }


    public void queue_move(Integer move) {
        move_queue.add(move);
    }

    public Integer compute_move() {
        float [] hmm_inputs = gatherInputs();
        return model.emit(hmm_inputs);
    }

    public void transition(){
        float [] hmm_inputs = gatherInputs();
        model.transition(hmm_inputs);
    }

    /**
     *
     */


    public float [] gatherInputs(){
        /*
        float [] hmm_inputs = new float [Simulation.MODEL_INPUTS];

        // how many moves to consider from each players queue
        int move_queue_depth = 6;

        // expand targets are not visible in player queues, so this can be expand, extract or null
        int option_per_move = 3;

        // move queue depth plus two states for each player's energy and territory
        int register_size = (move_queue_depth * option_per_move) + 2;

        // add player stats in order, starting with self
        for (int i=0; i<3; i++) {
            int p = ((i + gameIndex) % 3);
            int register_index = p * (register_size);

            Point player_id = gameBoard.allPlayerIds.get(p);
            Player player = gameBoard.players.get(player_id);
            Organism organism = player.getOrganism();

            // player's energy
            hmm_inputs[register_index] = (float) organism.energy / organism.MAX_ENERGY;

            // player's territory
            hmm_inputs[register_index + 1] = organism.territory_vertex.get_unmasked_vertices();

            // indicator variable for each players move queue
            // 0 or 1 for each possibility (0,1 or Null), for
            // -- 0 and 2 both mean expand (register 0)
            // -- 1 means extract

            LinkedList<Integer> move_queue = player.get_move_queue();
            int pos;

            int move_indicator = 2; // value for null
            for (int q=0; q<move_queue_depth; q++) {
                if (q < move_queue.size()) {
                    move_indicator = move_queue.get(q) % 2; // 0 and 2 both mean expand. 1 means extract
                }
                for (int m=0; m<option_per_move; m++) {
                    pos = register_index + 2 + (q * option_per_move) + m;
                    hmm_inputs[pos] = 0;
                    if (move_indicator == m) {
                        hmm_inputs[pos] = 1;
                    }
                }
            }
        }

        return hmm_inputs;

         */
        return new float [3];
    }



    /**
     * @return
     */
    @Override
    public Color getColor() {
        return color;
    }

    /**
     * @return
     */
    @Override
    public int getIndex() {
        return gameIndex;
    }



    @Override
    public String getPlayerName() {
        return playerName;
    }

    public Organism getOrganism(){
        return organism;
    }

    /**
     * @return
     */
    @Override
    public int getMostRecentMove() {
        return most_recent_move;
    }

    @Override
    public void dispose() {
        move_queue.clear();
        gameBoard = null;
        model.dispose();
        organism.dispose();
    }

    /**
     * @return
     */
    @Override
    public Point getTournamentId() {
        return tournament_id;
    }

    /**
     * @return
     */
    @Override
    public Point getAllyId() {
        return allyId;
    }

    public void setAllyId(Point p) {
        allyId = p;
    }

    /**
     *
     */
    @Override
    public void makeMove() {

    }
}
