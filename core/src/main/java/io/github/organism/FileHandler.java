package io.github.organism;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Objects;
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

        System.out.println(file_content);
        handle.writeString(file_content, false);

    }

    public GameConfig read_cfg(String name, String extension) {
        String dir = "map_configs";
        if (Objects.equals(extension, "hmm")) {
            dir = "model_configs";
        }
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

}
