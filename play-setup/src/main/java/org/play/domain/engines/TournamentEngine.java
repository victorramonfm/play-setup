package org.play.domain.engines;

import org.play.domain.models.Tournament;

import java.io.Serializable;

public interface TournamentEngine extends Serializable {

    void generateRound(Tournament tournament);
}
