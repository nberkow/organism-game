package io.github.organism;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.HashMap;

public class MoveLogger {

    ArrayList<String> moves;
    String [] cols = {
        "game",
        "turn",
        "player1_move",
        "player2_move",
        "player3_move",
        "player1_territory",
        "player2_territory",
        "player3_territory"
    };

    public MoveLogger(OrganismGame game) {
        moves = new ArrayList<>();
        moves.add(String.join("\t", cols));
    }

    public void log_move(HashMap<String, String> move_stats) {
        ArrayList<String> line_elements = new ArrayList<>();
        for (String c : cols) {
            line_elements.add(move_stats.get(c));
        }
        String move_line = String.join("\t", line_elements);

        moves.add(move_line);
    }

    public void write_moves(String name) {
        FileHandle handle = Gdx.files.local( "move_logs/" + name + ".log");
        String all_lines = String.join("\n", moves);
        handle.writeString(all_lines, false);
    }
}
