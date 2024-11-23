package io.github.organism;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class FileHandler {

    public boolean write_mode = false;

    public final String SAVE_PATH = "";
    public GameConfig handle_cfg(String label, GameConfig config, String extension) {

        FileHandle handle = Gdx.files.local( label + "." + extension);


        if (write_mode) {
            write_cfg(config, handle);
        } else {
            System.out.println("loading");
            return read_cfg(handle);
        }
        return(null);
    }
    public void write_cfg(GameConfig cfg, FileHandle handle) {

        String file_content =
            "radius:" + cfg.radius + "\n" +
            "seed:" + cfg.seed + "\n" +
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

    public GameConfig read_cfg(FileHandle handle) {
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
