package org.play;

import org.play.data.FileTournamentRepository;
import org.play.data.TournamentRepository;
import org.play.domain.Services.TournamentService;
import org.play.presentation.TournamentConsoleUI;

public class Main {
    public static void main(String[] args) {
        TournamentRepository repository = new FileTournamentRepository();
        TournamentService service = new TournamentService(repository);
        TournamentConsoleUI ui = new TournamentConsoleUI(service);

        ui.start();
    }
}