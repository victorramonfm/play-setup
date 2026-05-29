package org.play.domain.models;

import org.play.domain.engines.TournamentEngine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tournament implements Serializable {
    private final String id;
    private final String name;
    private final List<Player> players;
    private final List<Round> rounds;

    private int pointsWin;
    private int pointsDraw;
    private int pointsLoss;

    private TournamentStatus status;
    private TournamentEngine engine;

    public Tournament(String id, String name, int pointsWin, int pointsDraw, int pointsLoss) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do torneio não pode ser vazio.");
        }
        this.id = id;
        this.name = name;
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();
        this.pointsWin = pointsWin;
        this.pointsDraw = pointsDraw;
        this.pointsLoss = pointsLoss;
        this.status = TournamentStatus.CREATED;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public int getPointsWin() {
        return pointsWin;
    }

    public int getPointsDraw() {
        return pointsDraw;
    }

    public int getPointsLoss() {
        return pointsLoss;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public TournamentEngine getEngine() {
        return engine;
    }

    public void setEngine(TournamentEngine engine) {
        if (this.status != TournamentStatus.CREATED) {
            throw new IllegalStateException("Não é possível alterar o modo de torneio após o início.");
        }
        this.engine = engine;
    }

    public void addPlayer(Player player) {
        if (this.status != TournamentStatus.CREATED) {
            throw new IllegalStateException("Inscrições fechadas. O torneio está no estado: " + this.status);
        }
        for (Player p : players) {
            if (p.getId().equals(player.getId())) {
                throw new IllegalArgumentException("Jogador já cadastrado com este ID.");
            }
        }
        this.players.add(player);
    }

    public void advanceTournament() {
        boolean allPlayed = true;

        if (this.rounds.isEmpty() && this.status == TournamentStatus.IN_PROGRESS) {
            allPlayed = false;
        } else {
            for (Round r : rounds) {
                if (!r.isRoundCompleted()) {
                    allPlayed = false;
                    break;
                }
            }
        }

        this.status = this.status.next(this.players.size(), allPlayed);
    }

    public void generateNextRound() {
        if (this.status != TournamentStatus.IN_PROGRESS) {
            throw new IllegalStateException("O torneio precisa estar EM ANDAMENTO para gerar rodadas.");
        }
        if (this.engine == null) {
            throw new IllegalStateException("Nenhum motor de torneio (Engine) foi configurado.");
        }
        if (!rounds.isEmpty() && !rounds.get(rounds.size() - 1).isRoundCompleted()) {
            throw new IllegalStateException("A rodada anterior ainda possui partidas pendentes.");
        }

        this.engine.generateRound(this);
    }
}
