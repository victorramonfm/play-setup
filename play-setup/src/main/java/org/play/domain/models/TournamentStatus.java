package org.play.domain.models;

public enum TournamentStatus {
    CREATED,
    IN_PROGRESS,
    FINISHED;

    public TournamentStatus next(int playerCount, boolean allMatchesPlayed) {
        return switch (this) {
            case CREATED -> {
                if (playerCount < 2) {
                    throw new IllegalStateException("Não é possível iniciar o torneio com menos de 2 jogadores.");
                }
                yield IN_PROGRESS;
            }
            case IN_PROGRESS -> {
                if (!allMatchesPlayed) {
                    throw new IllegalStateException("Não é possível finalizar o torneio. Ainda há partidas pendentes.");
                }
                yield FINISHED;
            }
            case FINISHED -> throw new IllegalStateException("O torneio já está finalizado.");
        };
    }
}
