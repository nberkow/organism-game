package io.github.organism;

import java.util.ArrayList;
import java.util.Random;

public class GradientSet {

    ArrayList<int []> centers;

    double decay = .8d;
    int number;

    GameBoard game_board;

    GradientSet(GameBoard gb, int radius, int n){
        game_board = gb;
        number = n;
        centers = new ArrayList<>();
        Random rng = new Random(game_board.seed);

        for (int i=0; i<n; i++) {
            int a = radius - (rng.nextInt(radius) * 2);
            int b = radius - (rng.nextInt(radius) * 2);
            int c = radius - (rng.nextInt(radius) * 2);

            centers.add(new int[]{a, b, c});
            centers.add(new int[]{c, a, b});
            centers.add(new int[]{b, c, a});
        }
    }

    public double get_gradient_score(MapVertex h) {
        double score = 0d;



        return score;
    }
}
