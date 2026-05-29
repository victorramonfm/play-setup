package org.play.domain.Services;

import org.play.data.TournamentRepository;
import org.play.domain.engines.RoundRobinEngine;
import org.play.domain.engines.SwissEngine;
import org.play.domain.exceptions.BusinessException;
import org.play.domain.models.Player;
import org.play.domain.models.Tournament;
import org.play.domain.models.TournamentStatus;

import java.util.List;
import java.util.UUID;

public class TournamentService {

    private final TournamentRepository repository;

    public TournamentService(TournamentRepository repository) {
        this.repository = repository;
    }

    public Tournament createTournament(String name, String type, int ptsWin, int ptsDraw, int ptsLoss){
        String id = UUID.randomUUID().toString().substring(0,8);
        Tournament tournament = new Tournament(id, name, ptsWin, ptsDraw, ptsLoss);

        if (type.equalsIgnoreCase("ROUND_ROBIN")) {
            tournament.setEngine(new RoundRobinEngine());
        } else if (type.equalsIgnoreCase("SWISS")) {
            tournament.setEngine(new SwissEngine());
        } else {
            throw new BusinessException("Tipo de torneio não suportado: " + type);
        }

        repository.save(tournament);
        return tournament;
    }

    public void registerPlayerInTournament(String tournamentId, String playerName) {
        Tournament tournament = repository.findById(tournamentId);

        if (tournament == null) {
            throw new BusinessException("Torneio não encontrado com o ID informado.");
        }

        String playerId = UUID.randomUUID().toString().substring(0, 5);
        Player player = new Player(playerId, playerName);

        try {
            tournament.addPlayer(player);
            repository.save(tournament);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public void startTournament(String tournamentId) {
        Tournament tournament = repository.findById(tournamentId);
        if (tournament == null) {
            throw new BusinessException("Torneio não encontrado.");
        }

        try {
            tournament.advanceTournament();
            tournament.generateNextRound();
            repository.save(tournament);
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public void postMatchResult(String tournamentId, int roundIndex, int matchIndex, int score1, int score2) {
        Tournament tournament = repository.findById(tournamentId);
        if (tournament == null) {
            throw new BusinessException("Torneio não encontrado.");
        }

        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new BusinessException("Não é possível lançar resultados. O torneio está: " + tournament.getStatus());
        }

        try {
            var round = tournament.getRounds().get(roundIndex);
            var match = round.getMatches().get(matchIndex);

            match.updatePlayerStats(score1, score2, tournament.getPointsWin(), tournament.getPointsDraw(), tournament.getPointsLoss());
            repository.save(tournament);
        } catch (Exception e) {
            throw new BusinessException("Falha ao registrar placar: " + e.getMessage());
        }
    }

    public void advanceRoundOrFinish(String tournamentId) {
        Tournament tournament = repository.findById(tournamentId);
        if (tournament == null) {
            throw new BusinessException("Torneio não encontrado.");
        }

        try {
            var lastRound = tournament.getRounds().getLast();
            if (!lastRound.isRoundCompleted()) {
                throw new BusinessException("Ainda existem partidas pendentes na rodada atual.");
            }

            if (tournament.getEngine() instanceof RoundRobinEngine) {
                tournament.advanceTournament(); // Muda para FINISHED
            } else {
                tournament.generateNextRound();
            }

            repository.save(tournament);
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public List<Tournament> getAllTournaments() { return repository.findAll(); }

    public Tournament getTournamentById(String id) { return repository.findById(id); }

    public void deleteTournament(String id) { repository.deleteById(id); }
}
