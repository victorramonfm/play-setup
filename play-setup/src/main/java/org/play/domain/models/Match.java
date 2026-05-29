package org.play.domain.models;

import java.io.Serializable;

public class Match implements Serializable {

    private final Player player1;
    private final Player player2;
    private int scorePlayer1;
    private int scorePlayer2;
    private MatchStatus status;

    public Match(Player player1, Player player2){
        if (player1 == null || player2 == null) {
            throw new IllegalArgumentException("Uma partida precisa de dois jogadores válidos.");
        }
        if (player1.getId().equals(player2.getId())) {
            throw new IllegalArgumentException("Um jogador não pode enfrentar a si mesmo.");
        }

        this.player1 = player1;
        this.player2 = player2;
        this.scorePlayer1 = 0;
        this.scorePlayer2 = 0;
        this.status = MatchStatus.SCHEDULED;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public int getScorePlayer1() {
        return scorePlayer1;
    }

    public int getScorePlayer2() {
        return scorePlayer2;
    }

    public MatchStatus getStatus(){
        return status;
    }

    public void updatePlayerStats(int score1, int score2, int ptsWin, int ptsDraw, int ptsLoss) {
        if (this.status == MatchStatus.FINISHED) {
            throw new IllegalStateException("O resultado desta partida já foi finalizado.");
        }
        if (score1 < 0 || score2 < 0) {
            throw new IllegalArgumentException("Os placares não podem ser negativos.");
        }

        this.scorePlayer1 = score1;
        this.scorePlayer2 = score2;
        this.status = MatchStatus.FINISHED;

        if (score1 > score2) {
            player1.recordMatchResult(ptsWin, true, false, false, score1, score2, player2.getId());
            player2.recordMatchResult(ptsLoss, false, false, true, score2, score1, player1.getId());
        } else if (score2 > score1) {
            player1.recordMatchResult(ptsLoss, false, false, true, score1, score2, player2.getId());
            player2.recordMatchResult(ptsWin, true, false, false, score2, score1, player1.getId());
        } else {
            player1.recordMatchResult(ptsDraw, false, true, false, score1, score2, player2.getId());
            player2.recordMatchResult(ptsDraw, false, true, false, score2, score1, player1.getId());
        }
    }

    @Override
    public String toString() {
        if (status == MatchStatus.FINISHED) {
            return String.format("%s %d x %d %s", player1.getName(), scorePlayer1, scorePlayer2, player2.getName());
        }
        return String.format("%s vs %s [%s]", player1.getName(), player2.getName(), status);
    }
}
