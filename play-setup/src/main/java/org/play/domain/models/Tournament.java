package org.play.domain.models;

import org.play.domain.exceptions.BusinessException;
import org.play.domain.engines.TournamentEngine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tournament implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private TournamentStatus status;
    private List<Player> players;
    private List<Round> rounds;
    private transient TournamentEngine engine;

    private int ptsWin;
    private int ptsDraw;
    private int ptsLoss;

    public Tournament(String id, String name, int ptsWin, int ptsDraw, int ptsLoss) {
        this.id = id;
        this.name = name;
        this.ptsWin = ptsWin;
        this.ptsDraw = ptsDraw;
        this.ptsLoss = ptsLoss;
        this.status = TournamentStatus.CREATED;
        this.players = new ArrayList<>();
        this.rounds = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public TournamentStatus getStatus() { return status; }
    public List<Player> getPlayers() { return players; }
    public List<Round> getRounds() { return rounds; }
    public void setEngine(TournamentEngine engine) { this.engine = engine; }
    public TournamentEngine getEngine() { return engine; }
    public int getPointsWin() { return ptsWin; }
    public int getPointsDraw() { return ptsDraw; }
    public int getPointsLoss() { return ptsLoss; }

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

    public void startTournament() {
        if (this.status != TournamentStatus.CREATED) {
            throw new BusinessException("Transição inválida: Não é possível iniciar um torneio que está no estado " + this.status);
        }
        if (this.players.size() < 2) {
            throw new BusinessException("Regra de Negócio: Não é possível iniciar um torneio com menos de 2 jogadores.");
        }
        this.status = TournamentStatus.IN_PROGRESS;
    }

    public void finishTournament() {
        if (this.status != TournamentStatus.IN_PROGRESS) {
            throw new BusinessException("Transição inválida: Apenas torneios EM ANDAMENTO podem ser finalizados.");
        }
        if (!rounds.isEmpty() && !rounds.get(rounds.size() - 1).isRoundCompleted()) {
            throw new BusinessException("Regra de Negócio: Não é possível finalizar o torneio com partidas pendentes na rodada atual.");
        }
        this.status = TournamentStatus.FINISHED;
    }

    public void setStatus(TournamentStatus status) {
        if (status == TournamentStatus.IN_PROGRESS) {
            startTournament();
        } else if (status == TournamentStatus.FINISHED) {
            finishTournament();
        } else {
            this.status = status;
        }
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
            throw new BusinessException("Não é possível gerar uma rodada para um torneio no estado: " + this.status);
        }

        if (this.engine == null) {
            throw new BusinessException("Motor de pareamento (Engine) não configurado para este torneio.");
        }

        this.engine.generateRound(this);
    }
}
