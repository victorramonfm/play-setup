package org.play.domain.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private final String id;
    private final String name;
    private final List<String> encounteredOpponentsIds;

    private int points;
    private int wins;
    private int draws;
    private int losses;
    private int pointsFor;
    private int pointsAgainst;

    public Player(String id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do jogador não pode ser vazio");
        }

        this.id = id;
        this.name = name;
        this.encounteredOpponentsIds = new ArrayList<>();
        this.points = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.pointsFor = 0;
        this.pointsAgainst = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getPointsFor() {
        return pointsFor;
    }

    public int getPointsAgainst() {
        return pointsAgainst;
    }

    public List<String> getEncounteredOpponentsIds() {
        return encounteredOpponentsIds;
    }

    public void recordMatchResult(
            int earnedPoints,
            boolean isWin,
            boolean isDraw,
            boolean isLoss,
            int scoreFor,
            int scoreAgainst,
            String opponentId
    ) {
        this.points += earnedPoints;
        if (isWin) this.wins++;
        if (isDraw) this.draws++;
        if (isLoss) this.losses++;

        this.pointsFor = scoreFor;
        this.pointsAgainst = scoreAgainst;

        if (opponentId != null) {
            this.encounteredOpponentsIds.add(opponentId);
        }
    }

    @Override
    public String toString() {
        return String.format("%s [Pts: %d | V: %d - E: %d - D: %d]", name, points, wins, draws, losses);
    }
}
