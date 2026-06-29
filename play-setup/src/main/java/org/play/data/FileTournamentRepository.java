package org.play.data;

import org.play.domain.engines.RoundRobinEngine;
import org.play.domain.engines.SwissEngine;
import org.play.domain.models.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileTournamentRepository implements TournamentRepository {
    private final String filePath = "torneios.txt";
    private List<Tournament> tournaments;

    public FileTournamentRepository() {
        this.tournaments = loadFromFile();
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Tournament t : tournaments) {
                String engineType = (t.getEngine() instanceof SwissEngine) ? "SWISS" : "ROUND_ROBIN";
                writer.println("TO:" + t.getId() + "\t" + t.getName() + "\t" + t.getStatus() + "\t" +
                        t.getPointsWin() + "\t" + t.getPointsDraw() + "\t" + t.getPointsLoss() + "\t" + engineType);

                for (Player p : t.getPlayers()) {
                    String playerType = p instanceof HumanPlayer ? "HUMAN" : "BOT";
                    String extraParam = (p instanceof HumanPlayer) ? ((HumanPlayer) p).getNickname() : ((BotPlayer) p).getDifficulty();
                    String opponentsCSV = p.getEncounteredOpponentsIds().isEmpty() ? "NONE" :
                            String.join(",", p.getEncounteredOpponentsIds());

                    writer.println("PL:" + t.getId() + "\t" + p.getId() + "\t" + p.getName() + "\t" +
                            p.getPoints() + "\t" + p.getWins() + "\t" + p.getDraws() + "\t" +
                            p.getLosses() + "\t" + p.getPointsFor() + "\t" + p.getPointsAgainst() + "\t" +
                            playerType + "\t" + extraParam + "\t" + opponentsCSV);
                }

                for (Round r : t.getRounds()) {
                    writer.println("RO:" + t.getId() + "\t" + r.getRoundNumber() + ";true");

                    for (Match m : r.getMatches()) {
                        writer.println("MA:" + t.getId() + "\t" + r.getRoundNumber() + "\t" +
                                m.getPlayer1().getId() + "\t" + m.getPlayer2().getId() + "\t" +
                                m.getScorePlayer1() + "\t" + m.getScorePlayer2() + "\t" + m.getStatus());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados no arquivo TXT: " + e.getMessage());
        }
    }

    private List<Tournament> loadFromFile() {
        List<Tournament> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Tournament currentTournament = null;
            Round currentRound = null;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(":", 2);
                String prefix = parts[0];
                String[] tokens = parts[1].split("\t");

                switch (prefix) {
                    case "TO":
                        currentTournament = new Tournament(tokens[0], tokens[1],
                                Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5]));
                        currentTournament.setStatus(TournamentStatus.valueOf(tokens[2]));
                        if (tokens[6].equals("SWISS")) {
                            currentTournament.setEngine(new SwissEngine());
                        } else {
                            currentTournament.setEngine(new RoundRobinEngine());
                        }
                        list.add(currentTournament);
                        break;

                    case "PL":
                        if (currentTournament != null && currentTournament.getId().equals(tokens[0])) {
                            List<String> opponents = tokens[11].equals("NONE") ? new ArrayList<>() :
                                    new ArrayList<>(Arrays.asList(tokens[11].split(",")));

                            Player p;
                            if (tokens[9].equals("HUMAN")) {
                                p = new HumanPlayer(tokens[1], tokens[2], tokens[10], opponents);
                            } else {
                                p = new BotPlayer(tokens[1], tokens[2], tokens[10], opponents);
                            }
                            p.setStatsManual(
                                    Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]),
                                    Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6]),
                                    Integer.parseInt(tokens[7]), Integer.parseInt(tokens[8])
                            );
                            currentTournament.getPlayers().add(p);
                        }
                        break;

                    case "RO":
                        if (currentTournament != null && currentTournament.getId().equals(tokens[0])) {
                            currentRound = new Round(Integer.parseInt(tokens[1]));
                            currentTournament.getRounds().add(currentRound);
                        }
                        break;

                    case "MA":
                        if (currentTournament != null && currentRound != null && currentTournament.getId().equals(tokens[0])) {
                            String p1Id = tokens[2];
                            String p2Id = tokens[3];

                            Player p1 = currentTournament.getPlayers().stream().filter(p -> p.getId().equals(p1Id)).findFirst().orElse(null);
                            Player p2 = currentTournament.getPlayers().stream().filter(p -> p.getId().equals(p2Id)).findFirst().orElse(null);

                            if (p1 != null && p2 != null) {
                                Match m = new Match(p1, p2);
                                int score1 = Integer.parseInt(tokens[4]);
                                int score2 = Integer.parseInt(tokens[5]);
                                MatchStatus matchStatus = MatchStatus.valueOf(tokens[6]);

                                if (matchStatus == MatchStatus.FINISHED) {
                                    m.updatePlayerStats(score1, score2, currentTournament.getPointsWin(),
                                            currentTournament.getPointsDraw(), currentTournament.getPointsLoss());
                                }
                                currentRound.getMatches().add(m);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar dados do arquivo TXT: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void save(Tournament tournament) {
        tournaments.removeIf(t -> t.getId().equals(tournament.getId()));
        tournaments.add(tournament);
        saveToFile();
    }

    @Override
    public Tournament findById(String id) {
        return tournaments.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Tournament> findAll() {
        return new ArrayList<>(tournaments);
    }

    @Override
    public void deleteById(String id) {
        if (tournaments.removeIf(t -> t.getId().equals(id))) {
            saveToFile();
        }
    }
}
