package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class FileHandler {

    OrganismGame game;
    public boolean write_mode = false;
    public Random rng;

    public FileHandler(OrganismGame g){
        game = g;
    }

    public void write_cfg(GameConfig cfg, String name, String extension) {
        FileHandle handle = Gdx.files.local( name + "." + extension);
        rng.setSeed(cfg.seed);
        int seed = rng.nextInt();

        String file_content =
            "radius:" + cfg.radius + "\n" +
            "seed:" + seed + "\n" +
            "map_view_size_param:" + cfg.map_view_size_param + "\n" +
            "resources:" + cfg.resources + "\n" +
            "vertex_density:" + cfg.vertex_density + "\n" +
            "resource_layout:" + cfg.layout + "\n" +
            "difficulty:" + cfg.difficulty + "\n" +
            "human_players:" + cfg.human_players + "\n" +
            "bot_players:" + cfg.bot_players;

        handle.writeString(file_content, false);

    }

    public GameConfig read_cfg(String name, String extension) {
        String dir = "map_configs";

        FileHandle handle = Gdx.files.local( dir + "/" +    name + "." + extension);
        GameConfig cfg = new GameConfig();
        HashMap<String, String> vals = new HashMap<>();

        String [] lines = handle.readString().split("\n");
        for (String line : lines){
            String [] fields = line.split(":");
            vals.put(fields[0], fields[1]);
        }

        cfg.radius = Integer.parseInt(vals.get("radius"));
        cfg.seed = Long.parseLong(vals.get("seed"));
        cfg.map_view_size_param = Float.parseFloat(vals.get("map_view_size_param"));
        cfg.resources = Float.parseFloat(vals.get("resources"));
        cfg.vertex_density = Float.parseFloat(vals.get("vertex_density"));
        cfg.layout = vals.get("layout");
        cfg.difficulty = vals.get("difficulty");
        cfg.human_players = Integer.parseInt(vals.get("human_players"));
        cfg.bot_players = Integer.parseInt(vals.get("bot_players"));

        return cfg;
    }

    public void save_model(HMM model, String prefix){
        String dir = "model_configs";
        FileHandle handle = Gdx.files.local( dir + "/" + prefix + ".hmm");
        int states = model.transition_weights.length;
        int inputs = model.transition_weights[0][0].length;

        handle.writeString(states + "\n", true);
        handle.writeString(inputs + "\n", true);

        ArrayList<String> content = new ArrayList<>();

        for (int i=0; i<model.emission_weights.length; i++){
            for (int j=0; j<model.emission_weights[i].length; j++){
                for (int k = 0; k <model.emission_weights[i][j].length; k++){
                    content.add(i + "\t" + j + "\t" + k + "\t" + model.emission_weights[i][j][k]);
                }
            }
        }

        for (int i=0; i<model.transition_weights.length; i++){
            for (int j=0; j<model.transition_weights[i].length; j++){
                for (int k = 0; k <model.transition_weights[i][j].length; k++){
                    content.add(i + "\t" + j + "\t" + k + "\t" + model.transition_weights[i][j][k]);
                }
            }
        }

        String all_lines = String.join("\n", content);
        handle.writeString(all_lines, true);
    }

    public HMM load_model(GameBoard game_board, String prefix){

        String dir = "model_configs";
        FileHandle handle = Gdx.files.local( dir + "/" + prefix + ".hmm");
        String [] content = handle.readString().split("\n");

        int states = Integer.parseInt(content[0]);
        int inputs = Integer.parseInt(content[1]);

        int index = 2;
        HMM model = new HMM(game_board, states, 0, inputs);

        for (int i=0; i<states * 4 * inputs; i++){
            String [] elements = content[i+index].split("\t");
            int a = Integer.parseInt(elements[0]);
            int b = Integer.parseInt(elements[1]);
            int c = Integer.parseInt(elements[2]);
            double w = Double.parseDouble(elements[3]);
            model.emission_weights[a][b][c] = w;
        }

        for (int i=0; i<states * states * inputs; i++){
            String [] elements = content[i+index].split("\t");
            int a = Integer.parseInt(elements[0]);
            int b = Integer.parseInt(elements[1]);
            int c = Integer.parseInt(elements[2]);
            double w = Double.parseDouble(elements[3]);
            model.transition_weights[a][b][c] = w;
        }

        return model;
    }
}
