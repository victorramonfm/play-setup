package org.play.domain.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Round implements Serializable {

    public final int roundNumber;
    private final List<Match> matches;

    public Round(int roundNumber) {
        if (roundNumber <= 0) {
            throw new IllegalArgumentException("O número da rodada deve ser maior que zero.");
        }

        this.roundNumber = roundNumber;
        this.matches = new ArrayList<>();
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void addMatch(Match match) {
        if (match == null) {
            throw new IllegalArgumentException("Não é possível adicionar uma partida nula à rodada.");
        }
        this.matches.add(match);
    }

    public boolean isRoundCompleted() {
        for (Match match : matches) {
            if (match.getStatus() != MatchStatus.FINISHED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Rodada " + roundNumber + " [" + matches.size() + " partida(s)]";
    }
}
