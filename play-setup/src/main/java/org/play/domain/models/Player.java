package org.play.domain.models;

import java.io.Serializable;
import java.util.List;

public abstract class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final List<String> encounteredOpponentsIds;

    private int points;
    private int wins;
    private int draws;
    private int losses;
    private int pointsFor;
    private int pointsAgainst;

    public Player(String id, String name, List<String> encounteredOpponentsIds) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do jogador inválido.");
        }
        this.id = id;
        this.name = name;
        this.encounteredOpponentsIds = encounteredOpponentsIds;
    }

    public abstract String getPlayerType();
    public abstract int getTiebreakerPriority();

    public void recordMatchResult(int pointsGained, boolean isWin, boolean isDraw, boolean isLoss, int pf, int pa, String opponentId) {
        this.points += pointsGained;
        this.pointsFor += pf;
        this.pointsAgainst += pa;
        if (isWin) this.wins++;
        if (isDraw) this.draws++;
        if (isLoss) this.losses++;
        if (opponentId != null && !encounteredOpponentsIds.contains(opponentId)) {
            this.encounteredOpponentsIds.add(opponentId);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getEncounteredOpponentsIds() { return encounteredOpponentsIds; }
    public int getPoints() { return points; }
    public int getWins() { return wins; }
    public int getDraws() { return draws; }
    public int getLosses() { return losses; }
    public int getPointsFor() { return pointsFor; }
    public int getPointsAgainst() { return pointsAgainst; }

    public void setStatsManual(int points, int wins, int draws, int losses, int pointsFor, int pointsAgainst) {
        this.points = points;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.pointsFor = pointsFor;
        this.pointsAgainst = pointsAgainst;
    }

    public void resetStats() {
        this.points = 0;
        this.wins = 0;
        this.draws = 0;
        this.losses = 0;
        this.pointsFor = 0;
        this.pointsAgainst = 0;
        this.encounteredOpponentsIds.clear();
    }
}