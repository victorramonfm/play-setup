package org.play.domain.engines;

import org.play.domain.models.Match;
import org.play.domain.models.Player;
import org.play.domain.models.Round;
import org.play.domain.models.Tournament;

import java.util.ArrayList;
import java.util.List;

public class RoundRobinEngine implements TournamentEngine {

    @Override
    public void generateRound(Tournament tournament) {
        if (!tournament.getRounds().isEmpty()) {
            throw new IllegalStateException("As rodadas do Round Robin já foram geradas para este torneio.");
        }

        List<Player> players = new ArrayList<>(tournament.getPlayers());

        boolean hasBye = players.size() % 2 != 0;

        int totalPlayers = players.size();
        int totalRounds = totalPlayers - 1;
        int machesPerRound = totalPlayers / 2;

        for (int actualRound = 1; actualRound <= totalRounds; actualRound++) {
            Round round = new Round(actualRound);

            for (int i = 0; i < machesPerRound; i++) {
                Player player1 = players.get(1);
                Player player2 = players.get(totalPlayers - 1 - i);

                if (player1 != null && player2 != null) {
                    Match match = new Match(player1, player2);
                    round.addMatch(match);
                }
            }

            tournament.getRounds().add(round);

            Player lastPlayer = players.get(totalPlayers - 1);

            for (int k = totalPlayers - 1; k > 1; k--) {
                players.set(k, players.get(k - 1));
            }

            players.set(1, lastPlayer);
        }

    }


}
