package org.play.presentation;

import org.play.domain.models.Tournament;
import org.play.domain.models.Player;
import org.play.domain.models.Round;
import org.play.domain.models.Match;
import org.play.domain.models.MatchStatus;
import org.play.domain.engines.TournamentEngine;
import org.play.domain.engines.RoundRobinEngine;
import org.play.domain.engines.SwissEngine;
import org.play.data.TournamentRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentManagementUI extends JFrame {

    private final Tournament tournament;
    private final TournamentRepository repository;

    private DefaultListModel<String> playersListModel;
    private JTextField playerFieldName;
    private JButton btnAddPlayer;

    private JButton btnAction;
    private JComboBox<String> engineComboBox;
    private JPanel engineSelectionPanel;

    private JTabbedPane tabbedPane;
    private JPanel currentRoundPanel;

    private JTable standingsTable;
    private DefaultTableModel standingsTableModel;

    private JPanel historyTablesContainer;

    private JComboBox<String> roundSelectorComboBox;
    private JPanel matchesContainerPanel;

    public TournamentManagementUI(Tournament tournament, TournamentRepository repository) {
        this.tournament = tournament;
        this.repository = repository;

        setTitle("Gerenciar Torneio: " + tournament.getName());
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel playersPanel = new JPanel(new BorderLayout(5, 5));
        playersPanel.setBorder(BorderFactory.createTitledBorder("Jogadores Inscritos"));
        playersPanel.setPreferredSize(new Dimension(240, 0));

        playersListModel = new DefaultListModel<>();
        JList<String> playersList = new JList<>(playersListModel);
        playersPanel.add(new JScrollPane(playersList), BorderLayout.CENTER);

        JPanel addPlayerPanel = new JPanel(new BorderLayout(5, 5));
        playerFieldName = new JTextField();
        btnAddPlayer = new JButton("Adicionar");
        addPlayerPanel.add(playerFieldName, BorderLayout.CENTER);
        addPlayerPanel.add(btnAddPlayer, BorderLayout.EAST);
        playersPanel.add(addPlayerPanel, BorderLayout.SOUTH);

        add(playersPanel, BorderLayout.WEST);

        tabbedPane = new JTabbedPane();

        currentRoundPanel = new JPanel(new BorderLayout(5, 5));
        JPanel topSelectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topSelectorPanel.add(new JLabel("Visualizar Rodada:"));
        roundSelectorComboBox = new JComboBox<>();
        topSelectorPanel.add(roundSelectorComboBox);
        currentRoundPanel.add(topSelectorPanel, BorderLayout.NORTH);

        matchesContainerPanel = new JPanel();
        matchesContainerPanel.setLayout(new BoxLayout(matchesContainerPanel, BoxLayout.Y_AXIS));
        currentRoundPanel.add(new JScrollPane(matchesContainerPanel), BorderLayout.CENTER);

        setupStandingsTable();

        historyTablesContainer = new JPanel();
        historyTablesContainer.setLayout(new BoxLayout(historyTablesContainer, BoxLayout.Y_AXIS));

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomControlPanel = new JPanel(new BorderLayout(5, 5));
        bottomControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        engineSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        engineSelectionPanel.add(new JLabel("Formato:"));
        String[] tournamentTypes = {"Round Robin (Todos contra todos)", "Sistema Suíço"};
        engineComboBox = new JComboBox<>(tournamentTypes);
        engineSelectionPanel.add(engineComboBox);
        bottomControlPanel.add(engineSelectionPanel, BorderLayout.WEST);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAction = new JButton();
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 12));
        buttonWrapper.add(btnAction);
        bottomControlPanel.add(buttonWrapper, BorderLayout.EAST);

        add(bottomControlPanel, BorderLayout.SOUTH);

        btnAddPlayer.addActionListener(e -> handleAddPlayer());
        btnAction.addActionListener(e -> handleMainAction());

        roundSelectorComboBox.addActionListener(e -> {
            int selectedIndex = roundSelectorComboBox.getSelectedIndex();
            if (selectedIndex != -1) {
                buildMatchesForRound(selectedIndex);
            }
        });

        refreshTournamentDetails();
    }

    private void setupStandingsTable() {
        String[] columns = {"Pos", "Jogador", "Pts", "V", "E", "D", "Pro", "Contra"};
        standingsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        standingsTable = new JTable(standingsTableModel);
        standingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void handleAddPlayer() {
        String playerName = playerFieldName.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite o nome do jogador.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String playerId = "PLY-" + System.currentTimeMillis();
            Player newPlayer = new Player(playerId, playerName);
            tournament.addPlayer(newPlayer);
            playerFieldName.setText("");
            saveAndRefresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Regra de Domínio", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMainAction() {
        try {
            if (tournament.getRounds().isEmpty()) {
                TournamentEngine selectedEngine = engineComboBox.getSelectedItem().equals("Sistema Suíço")
                        ? new SwissEngine() : new RoundRobinEngine();

                tournament.setEngine(selectedEngine);
                tournament.advanceTournament();
                tournament.generateNextRound();
                JOptionPane.showMessageDialog(this, "Torneio Iniciado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                tournament.generateNextRound();
                JOptionPane.showMessageDialog(this, "Próxima rodada gerada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            }
            saveAndRefresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Aviso do Sistema", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveAndRefresh() {
        repository.save(tournament);
        refreshTournamentDetails();
    }

    private void refreshTournamentDetails() {
        boolean isStarted = !tournament.getRounds().isEmpty();

        playersListModel.clear();
        for (Player p : tournament.getPlayers()) {
            playersListModel.addElement(String.format("%s (Pts: %d)", p.getName(), p.getPoints()));
        }
        playerFieldName.setEnabled(!isStarted);
        btnAddPlayer.setEnabled(!isStarted);

        tabbedPane.removeAll();
        if (!isStarted) {
            JPanel welcomePanel = new JPanel(new GridBagLayout());
            welcomePanel.add(new JLabel("<html><center><h2>Aguardando o início do torneio</h2>"
                    + "Insira os competidores ao lado e escolha o formato abaixo.</center></html>"));
            tabbedPane.addTab("Configuração", welcomePanel);

            btnAction.setText("Iniciar Torneio ▶");
            btnAction.setBackground(new Color(40, 167, 69));
            btnAction.setForeground(Color.WHITE);
            btnAction.setVisible(true);
            engineSelectionPanel.setVisible(true);
        } else {
            java.awt.event.ActionListener[] listeners = roundSelectorComboBox.getActionListeners();
            for (java.awt.event.ActionListener l : listeners) roundSelectorComboBox.removeActionListener(l);

            int previousSelection = roundSelectorComboBox.getSelectedIndex();
            roundSelectorComboBox.removeAllItems();
            for (Round r : tournament.getRounds()) {
                roundSelectorComboBox.addItem("Rodada " + r.getRoundNumber());
            }

            if (previousSelection != -1 && previousSelection < tournament.getRounds().size()) {
                roundSelectorComboBox.setSelectedIndex(previousSelection);
            } else {
                roundSelectorComboBox.setSelectedIndex(tournament.getRounds().size() - 1);
            }

            for (java.awt.event.ActionListener l : listeners) roundSelectorComboBox.addActionListener(l);

            buildStandingsTab();
            buildHistoryTab();

            tabbedPane.addTab("Partidas por Rodada ⚔️", currentRoundPanel);
            tabbedPane.addTab("Classificação 🏆", new JScrollPane(standingsTable));
            tabbedPane.addTab("Histórico por Rodada 📖", new JScrollPane(historyTablesContainer));

            buildMatchesForRound(roundSelectorComboBox.getSelectedIndex());

            if (isSwissEngine()) {
                btnAction.setText("Gerar Próxima Rodada ⚄");
                btnAction.setBackground(UIManager.getColor("Button.background"));
                btnAction.setForeground(Color.BLACK);
                btnAction.setVisible(true);
            } else {
                btnAction.setVisible(false);
            }

            engineSelectionPanel.setVisible(false);
        }
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    private boolean isSwissEngine() {
        try {
            java.lang.reflect.Field fieldEngine = Tournament.class.getDeclaredField("engine");
            fieldEngine.setAccessible(true);
            Object activeEngine = fieldEngine.get(tournament);
            return activeEngine instanceof SwissEngine;
        } catch (Exception ex) {
            return false;
        }
    }

    private void buildMatchesForRound(int roundIndex) {
        if (roundIndex < 0 || roundIndex >= tournament.getRounds().size()) return;

        matchesContainerPanel.removeAll();
        matchesContainerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        Round selectedRound = tournament.getRounds().get(roundIndex);

        for (Match match : selectedRound.getMatches()) {
            JPanel matchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
            matchPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

            JLabel lblPlayer1 = new JLabel(match.getPlayer1().getName());
            lblPlayer1.setPreferredSize(new Dimension(140, 20));
            lblPlayer1.setHorizontalAlignment(SwingConstants.RIGHT);

            JTextField txtScore1 = new JTextField(String.valueOf(match.getScorePlayer1()), 3);
            JLabel lblX = new JLabel("x");
            JTextField txtScore2 = new JTextField(String.valueOf(match.getScorePlayer2()), 3);

            JLabel lblPlayer2 = new JLabel(match.getPlayer2().getName());
            lblPlayer2.setPreferredSize(new Dimension(140, 20));
            lblPlayer2.setHorizontalAlignment(SwingConstants.LEFT);

            JButton btnSaveMatch = new JButton();

            boolean isByeMatch = match.getPlayer1().getId().equals(org.play.domain.engines.SwissEngine.BYE_ID) ||
                    match.getPlayer2().getId().equals(org.play.domain.engines.SwissEngine.BYE_ID);

            if (isByeMatch) {
                matchPanel.setBackground(new Color(230, 245, 230));
                txtScore1.setEditable(false);
                txtScore2.setEditable(false);
                btnSaveMatch.setText("Folga (Automático)");
                btnSaveMatch.setEnabled(false);
            } else if (match.getStatus() == MatchStatus.FINISHED) {
                matchPanel.setBackground(new Color(240, 248, 255));
                btnSaveMatch.setText("Atualizar Placar");
            } else {
                matchPanel.setBackground(Color.WHITE);
                btnSaveMatch.setText("Salvar Resultado");
            }

            if (!isByeMatch) {
                btnSaveMatch.addActionListener(e -> {
                    try {
                        int score1 = Integer.parseInt(txtScore1.getText().trim());
                        int score2 = Integer.parseInt(txtScore2.getText().trim());

                        try {
                            java.lang.reflect.Field statusField = Match.class.getDeclaredField("status");
                            statusField.setAccessible(true);
                            statusField.set(match, MatchStatus.SCHEDULED);
                        } catch (Exception ignored) {
                        }

                        match.updatePlayerStats(
                                score1, score2,
                                tournament.getPointsWin(),
                                tournament.getPointsDraw(),
                                tournament.getPointsLoss()
                        );

                        for (Player p : tournament.getPlayers()) {
                            p.resetStats();
                        }

                        for (Round r : tournament.getRounds()) {
                            for (Match m : r.getMatches()) {
                                if (m.getStatus() == MatchStatus.FINISHED) {
                                    boolean isInternalBye = m.getPlayer1().getId().equals(org.play.domain.engines.SwissEngine.BYE_ID) ||
                                            m.getPlayer2().getId().equals(org.play.domain.engines.SwissEngine.BYE_ID);

                                    if (isInternalBye) {
                                        Player realPlayer = m.getPlayer1().getId().equals(org.play.domain.engines.SwissEngine.BYE_ID) ? m.getPlayer2() : m.getPlayer1();
                                        realPlayer.recordMatchResult(tournament.getPointsWin(), true, false, false, 0, 0, null);
                                    } else {
                                        if (m.getScorePlayer1() > m.getScorePlayer2()) {
                                            m.getPlayer1().recordMatchResult(tournament.getPointsWin(), true, false, false, m.getScorePlayer1(), m.getScorePlayer2(), m.getPlayer2().getId());
                                            m.getPlayer2().recordMatchResult(tournament.getPointsLoss(), false, false, true, m.getScorePlayer2(), m.getScorePlayer1(), m.getPlayer1().getId());
                                        } else if (m.getScorePlayer2() > m.getScorePlayer1()) {
                                            m.getPlayer1().recordMatchResult(tournament.getPointsLoss(), false, false, true, m.getScorePlayer1(), m.getScorePlayer2(), m.getPlayer2().getId());
                                            m.getPlayer2().recordMatchResult(tournament.getPointsWin(), true, false, false, m.getScorePlayer2(), m.getScorePlayer1(), m.getPlayer1().getId());
                                        } else {
                                            m.getPlayer1().recordMatchResult(tournament.getPointsDraw(), false, true, false, m.getScorePlayer1(), m.getScorePlayer2(), m.getPlayer2().getId());
                                            m.getPlayer2().recordMatchResult(tournament.getPointsDraw(), false, true, false, m.getScorePlayer2(), m.getScorePlayer1(), m.getPlayer1().getId());
                                        }
                                    }
                                }
                            }
                        }

                        JOptionPane.showMessageDialog(this, "Resultado registrado e classificação atualizada!");
                        saveAndRefresh();
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(this, "Insira números válidos para o placar.", "Erro", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Aviso: " + ex.getMessage(), "Informação", JOptionPane.WARNING_MESSAGE);
                    }
                });
            }

            matchPanel.add(lblPlayer1);
            matchPanel.add(txtScore1);
            matchPanel.add(lblX);
            matchPanel.add(txtScore2);
            matchPanel.add(lblPlayer2);
            matchPanel.add(btnSaveMatch);

            matchesContainerPanel.add(matchPanel);
            matchesContainerPanel.add(Box.createVerticalStrut(10));
        }

        matchesContainerPanel.revalidate();
        matchesContainerPanel.repaint();
    }

    private void buildStandingsTab() {
        standingsTableModel.setRowCount(0);
        List<Player> sortedPlayers = new ArrayList<>(tournament.getPlayers());

        sortedPlayers.sort((p1, p2) -> {
            int comp = Integer.compare(p2.getPoints(), p1.getPoints());
            if (comp == 0) {
                return Integer.compare(p2.getWins(), p1.getWins());
            }
            return comp;
        });

        int pos = 1;
        for (Player p : sortedPlayers) {
            standingsTableModel.addRow(new Object[]{
                    pos++, p.getName(), p.getPoints(), p.getWins(), p.getDraws(), p.getLosses(), p.getPointsFor(), p.getPointsAgainst()
            });
        }
    }

    private void buildHistoryTab() {
        historyTablesContainer.removeAll();
        historyTablesContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Jogador 1", "Placar", "Jogador 2", "Status"};

        for (Round round : tournament.getRounds()) {
            JPanel roundSectionPanel = new JPanel(new BorderLayout(5, 5));
            roundSectionPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(),
                    "⚔️ RODADA " + round.getRoundNumber(),
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 13),
                    new Color(0, 51, 102)
            ));

            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };

            for (Match m : round.getMatches()) {
                String scoreDisplay = (m.getStatus() == MatchStatus.FINISHED)
                        ? String.format("%d x %d", m.getScorePlayer1(), m.getScorePlayer2())
                        : "vs";

                model.addRow(new Object[]{
                        m.getPlayer1().getName(),
                        scoreDisplay,
                        m.getPlayer2().getName(),
                        m.getStatus().toString()
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(22);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getColumnModel().getColumn(1).setPreferredWidth(60);

            roundSectionPanel.add(table.getTableHeader(), BorderLayout.NORTH);
            roundSectionPanel.add(table, BorderLayout.CENTER);

            historyTablesContainer.add(roundSectionPanel);
            historyTablesContainer.add(Box.createVerticalStrut(15));
        }

        historyTablesContainer.revalidate();
        historyTablesContainer.repaint();
    }
}