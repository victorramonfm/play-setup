package org.play.data;

import org.play.domain.models.Tournament;

import java.util.List;

public interface TournamentRepository {

    void save(Tournament tournament);

    Tournament findById(String id);

    List<Tournament> findAll();

    void deleteById(String id);
};