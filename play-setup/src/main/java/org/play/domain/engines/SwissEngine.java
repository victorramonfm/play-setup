package org.play.domain.engines;

import org.play.domain.models.Match;
import org.play.domain.models.Player;
import org.play.domain.models.Round;
import org.play.domain.models.Tournament;

import java.util.ArrayList;
import java.util.List;

public class SwissEngine implements TournamentEngine {

    @Override
    public void generateRound(Tournament tournament) {
        List<Player> sortedPlayers = new ArrayList<>(tournament.getPlayers());

        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getPoints(), p1.getPoints()));

        int nextRoundNumber = tournament.getRounds().size() + 1;
        Round round = new Round(nextRoundNumber);

        List<Player> playersPairedThisRound = new ArrayList<>();

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p1 = sortedPlayers.get(i);

            if (playersPairedThisRound.contains(p1)) {
                continue;
            }

            Player p2 = null;

            for (int j = i + 1; j < sortedPlayers.size(); j++) {
                Player candidate = sortedPlayers.get(j);

                if (!playersPairedThisRound.contains(candidate) && !p1.getEncounteredOpponentsIds().contains(candidate.getId())) {
                    p2 = candidate;
                    break;
                }
            }

            if(p2 != null){
                Match match = new Match(p1, p2);
                round.addMatch(match);
                playersPairedThisRound.add(p1);
                playersPairedThisRound.add(p2);
            } else {
                throw new IllegalStateException("Conflito no Sistema Suíço: Impossível gerar confrontos sem repetir oponentes para " + p1.getName() + ".");
            }

            tournament.getRounds().add(round);
        }
    }
}
