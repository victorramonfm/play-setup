package org.play.domain.models;

import java.util.ArrayList;
import java.util.List;

public class BotPlayer extends Player {
    private static final long serialVersionUID = 1L;
    private final String difficulty;

    public BotPlayer(String id, String name, String difficulty) {
        super(id, name, new ArrayList<>());
        this.difficulty = difficulty;
    }

    public BotPlayer(String id, String name, String difficulty, List<String> opponents) {
        super(id, name, opponents);
        this.difficulty = difficulty;
    }

    public String getDifficulty() { return difficulty; }

    @Override
    public String getPlayerType() { return "Bot/IA [" + difficulty + "]"; }

    @Override
    public int getTiebreakerPriority() { return 1; }
}