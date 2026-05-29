package org.play.data;

import org.play.domain.models.Tournament;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileTournamentRepository implements TournamentRepository {

    private static final String FILE_NAME = "tournament_data.dat";
    private Map<String, Tournament> database;

    public FileTournamentRepository() {
        this.database = new HashMap<>();
        loadFromFile();
    }

    @Override
    public void save(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Não é possível salvar um torneio nulo.");
        }
        database.put(tournament.getId(), tournament);
        saveToFile();
    }

    @Override
    public Tournament findById(String id) {
        return database.get(id);
    }

    @Override
    public List<Tournament> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void deleteById(String id) {
        if (database.containsKey(id)) {
            database.remove(id);
            saveToFile();
        }
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(database);
        } catch (IOException e) {
            throw new RuntimeException("Erro crítico ao salvar os dados no arquivo local: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object data = ois.readObject();
            if (data instanceof Map) {
                this.database = (Map<String, Tournament>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[Aviso] Não foi possível carregar os dados anteriores. Criando base nova. Motivo: " + e.getMessage());
            this.database = new HashMap<>();
        }
    }
}
