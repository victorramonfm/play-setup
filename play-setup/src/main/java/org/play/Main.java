package org.play;

import org.play.data.FileTournamentRepository;
import org.play.data.TournamentRepository;
import org.play.presentation.TournamentSwingUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        TournamentRepository repository = new FileTournamentRepository();

        SwingUtilities.invokeLater(() -> {
            TournamentSwingUI gui = new TournamentSwingUI(repository);
            gui.setVisible(true);
        });
    }
}