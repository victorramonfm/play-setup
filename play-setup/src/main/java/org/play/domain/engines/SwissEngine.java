package org.play.domain.engines;

import org.play.domain.models.Match;
import org.play.domain.models.Player;
import org.play.domain.models.Round;
import org.play.domain.models.Tournament;
import org.play.domain.models.HumanPlayer;

import java.util.ArrayList;
import java.util.List;

public class SwissEngine implements TournamentEngine {

    public static final String BYE_ID = "PLY-BYE-SYSTEM";

    @Override
    public void generateRound(Tournament tournament) {
        List<Player> sortedPlayers = new ArrayList<>(tournament.getPlayers());

        sortedPlayers.sort((p1, p2) -> {
            int comp = Integer.compare(p2.getPoints(), p1.getPoints());
            if (comp == 0) {
                return Integer.compare(p2.getWins(), p1.getWins());
            }
            return comp;
        });

        int nextRoundNumber = tournament.getRounds().size() + 1;
        Round round = new Round(nextRoundNumber);

        List<Match> generatedMatches = new ArrayList<>();
        Player byePlayerInThisRound = null;

        if (sortedPlayers.size() % 2 != 0) {
            for (int i = sortedPlayers.size() - 1; i >= 0; i--) {
                Player p = sortedPlayers.get(i);
                if (!hasReceivedBye(p, tournament)) {
                    byePlayerInThisRound = p;
                    break;
                }
            }

            if (byePlayerInThisRound == null) {
                byePlayerInThisRound = sortedPlayers.get(sortedPlayers.size() - 1);
            }

            sortedPlayers.remove(byePlayerInThisRound);
        }

        boolean success = findSwissPairings(sortedPlayers, 0, generatedMatches);

        if (!success) {
            throw new IllegalStateException("Conflito crítico no Sistema Suíço: Impossível gerar pareamentos válidos sem repetir oponentes nesta rodada.");
        }

        for (Match match : generatedMatches) {
            round.addMatch(match);
        }

        if (byePlayerInThisRound != null) {
            Player dummyBye = new HumanPlayer(BYE_ID, "BYE (Folga)", "SISTEMA");
            Match byeMatch = new Match(byePlayerInThisRound, dummyBye);

            byeMatch.updatePlayerStats(1, 0, tournament.getPointsWin(), tournament.getPointsDraw(), tournament.getPointsLoss());

            round.addMatch(byeMatch);
        }

        tournament.getRounds().add(round);
    }

    private boolean findSwissPairings(List<Player> players, int index, List<Match> currentMatches) {
        if (index >= players.size()) {
            return true;
        }

        Player p1 = players.get(index);

        if (isAlreadyPaired(p1, currentMatches)) {
            return findSwissPairings(players, index + 1, currentMatches);
        }

        for (int j = index + 1; j < players.size(); j++) {
            Player p2 = players.get(j);

            if (!isAlreadyPaired(p2, currentMatches) && !p1.getEncounteredOpponentsIds().contains(p2.getId())) {

                Match proposedMatch = new Match(p1, p2);
                currentMatches.add(proposedMatch);

                if (findSwissPairings(players, index + 1, currentMatches)) {
                    return true;
                }

                currentMatches.remove(currentMatches.size() - 1);
            }
        }

        return false;
    }

    private boolean isAlreadyPaired(Player p, List<Match> matches) {
        for (Match m : matches) {
            if (m.getPlayer1().getId().equals(p.getId()) || m.getPlayer2().getId().equals(p.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasReceivedBye(Player p, Tournament tournament) {
        for (Round r : tournament.getRounds()) {
            for (Match m : r.getMatches()) {
                if (m.getPlayer1().getId().equals(p.getId()) || m.getPlayer2().getId().equals(p.getId())) {
                    if (m.getPlayer2().getId().equals(BYE_ID) || m.getPlayer1().getId().equals(BYE_ID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}