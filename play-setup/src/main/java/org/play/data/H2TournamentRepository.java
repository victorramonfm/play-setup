package org.play.data;

import org.play.domain.models.Tournament;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class H2TournamentRepository implements TournamentRepository {

    private static final String URL = "jdbc:h2:./play_database;DB_CLOSE_DELAY=-1";
    private static final String USER = "dbuser";
    private static final String PASSWORD = "";

    public H2TournamentRepository() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.execute(TournamentQueries.CREATE_TABLE);

        } catch (SQLException e) {
            throw new RuntimeException("Erro crítico ao inicializar o banco de dados H2: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Tournament tournament) {
        if (tournament == null) {
            throw new IllegalArgumentException("Não é possível salvar um torneio nulo.");
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(TournamentQueries.UPSERT_TOURNAMENT)) {

            pstmt.setString(1, tournament.getId());
            pstmt.setString(2, tournament.getName());

            byte[] serializedData = serialize(tournament);
            pstmt.setBytes(3, serializedData);

            pstmt.executeUpdate();

        } catch (SQLException | IOException e) {
            throw new RuntimeException("Erro ao salvar o torneio no banco H2: " + e.getMessage(), e);
        }
    }

    @Override
    public Tournament findById(String id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(TournamentQueries.FIND_BY_ID)) {

            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] serializedData = rs.getBytes("data");
                    return deserialize(serializedData);
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao buscar o torneio por ID no banco H2: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Tournament> findAll() {
        List<Tournament> tournaments = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(TournamentQueries.FIND_ALL)) {

            while (rs.next()) {
                byte[] serializedData = rs.getBytes("data");
                tournaments.add(deserialize(serializedData));
            }

        } catch (SQLException | IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erro ao listar os torneios do banco H2: " + e.getMessage(), e);
        }
        return tournaments;
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(TournamentQueries.DELETE_BY_ID)) {

            pstmt.setString(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar o torneio no banco H2: " + e.getMessage(), e);
        }
    }

    private byte[] serialize(Tournament obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }

    private Tournament deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Tournament) ois.readObject();
        }
    }
}