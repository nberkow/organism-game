package io.github.organism;
import java.util.LinkedList;

public class ActionHistory {

    LinkedList<Integer> commands = new LinkedList<>();
    LinkedList<Double> energy_level = new LinkedList<>();
    LinkedList<Double> total_assimilation = new LinkedList<>();

    GameBoard game_board;

    public ActionHistory(GameBoard gb){
        game_board = gb;
    }
    public void log_move(Integer c, Double e, Double a){
        commands.add(c);
        energy_level.add(e);
        total_assimilation.add(a);
    }

    public HMM calculate_model(){
        HMM m = new HMM(game_board);
        m.init(0);
        /*
        Calculate the various probabilities from the move history
         */
        return m;
    }
}
