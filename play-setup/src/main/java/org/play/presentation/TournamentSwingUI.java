package org.play.presentation;

import org.play.domain.models.Tournament;
import org.play.data.TournamentRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TournamentSwingUI extends JFrame {

    private final TournamentRepository repository;
    private JTable tournamentTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;

    public TournamentSwingUI(TournamentRepository repository) {
        this.repository = repository;

        setTitle("Play Setup - Painel de Torneios");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Criar Novo Torneio"));
        inputPanel.add(new JLabel("Nome do Torneio:"));
        nameField = new JTextField(25);
        inputPanel.add(nameField);
        JButton btnCreate = new JButton("Criar Torneio");
        inputPanel.add(btnCreate);
        add(inputPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Nome do Torneio"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tournamentTable = new JTable(tableModel);
        tournamentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tournamentTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnManage = new JButton("Gerenciar Torneio ⚙️");
        JButton btnDelete = new JButton("Apagar Selecionado");
        btnManage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        actionPanel.add(btnManage);
        actionPanel.add(btnDelete);
        add(actionPanel, BorderLayout.SOUTH);

        btnCreate.addActionListener(e -> handleCreateTournament());
        btnManage.addActionListener(e -> handleManageTournament());
        btnDelete.addActionListener(e -> handleDeleteTournament());

        refreshTable();
    }

    private void handleCreateTournament() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do torneio não pode estar vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = "TRN-" + System.currentTimeMillis();

        Tournament tournament = new Tournament(id, name, 3, 1, 0);

        repository.save(tournament);
        nameField.setText("");
        refreshTable();
        JOptionPane.showMessageDialog(this, "Torneio criado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleManageTournament() {
        int selectedRow = tournamentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um torneio para gerenciar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        Tournament selectedTournament = repository.findById(id);

        if (selectedTournament != null) {
            SwingUtilities.invokeLater(() -> {
                TournamentManagementUI managementUI = new TournamentManagementUI(selectedTournament, repository);
                managementUI.setVisible(true);
            });
        }
    }

    private void handleDeleteTournament() {
        int selectedRow = tournamentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um torneio para apagar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja apagar este torneio?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            repository.deleteById(id);
            refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Tournament> tournaments = repository.findAll();
        for (Tournament t : tournaments) {
            tableModel.addRow(new Object[]{t.getId(), t.getName()});
        }
    }
}