package org.play.data;

public final class TournamentQueries {

    private TournamentQueries() {
        throw new UnsupportedOperationException("Esta é uma classe de constantes e não deve ser instanciada.");
    }

    public static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS tournament (
            id VARCHAR(50) PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            data BLOB NOT NULL
        );
        """;

    public static final String UPSERT_TOURNAMENT = """
        MERGE INTO tournament (id, name, data)
        KEY(id)
        VALUES (?, ?, ?);
        """;

    public static final String FIND_BY_ID = """
        SELECT data
        FROM tournament
        WHERE id = ?;
        """;

    public static final String FIND_ALL = """
        SELECT data
        FROM tournament;
        """;

    public static final String DELETE_BY_ID = """
        DELETE FROM tournament
        WHERE id = ?;
        """;
}