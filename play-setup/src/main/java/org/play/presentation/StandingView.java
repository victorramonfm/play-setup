package org.play.presentation;

import org.play.domain.models.Player;
import org.play.domain.models.Tournament;
import java.util.ArrayList;
import java.util.List;

public class StandingView {

    public static void render(Tournament tournament) {
        List<Player> sortedList = new ArrayList<>(tournament.getPlayers());

        // Ordena os jogadores para a tabela: Maior pontuação primeiro.
        // Se empatar em pontos, usa o saldo de "Pontos Feitos (PF)" como critério de desempate
        sortedList.sort((p1, p2) -> {
            int comp = Integer.compare(p2.getPoints(), p1.getPoints());
            if (comp == 0) {
                return Integer.compare(p2.getPointsFor(), p1.getPointsFor());
            }
            return comp;
        });

        System.out.println("\n=================================================================");
        System.out.printf("   TABELA DE CLASSIFICAÇÃO - %s (%s)\n", tournament.getName().toUpperCase(), tournament.getStatus());
        System.out.println("=================================================================");
        System.out.printf("%-4s | %-18s | %-3s | %-2s | %-2s | %-2s | %-3s | %-3s\n",
                "Pos", "Nome", "Pt", "W", "D", "L", "PF", "PA");
        System.out.println("-----------------------------------------------------------------");

        for (int i = 0; i < sortedList.size(); i++) {
            Player p = sortedList.get(i);
            System.out.printf("#%-3d | %-18s | %-3d | %-2d | %-2d | %-2d | %-3d | %-3d\n",
                    (i + 1),
                    p.getName(),
                    p.getPoints(),
                    p.getWins(),
                    p.getDraws(),
                    p.getLosses(),
                    p.getPointsFor(),
                    p.getPointsAgainst()
            );
        }
        System.out.println("=================================================================\n");
    }
}