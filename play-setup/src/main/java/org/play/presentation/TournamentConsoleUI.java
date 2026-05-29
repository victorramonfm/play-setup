package org.play.presentation;

import org.play.domain.Services.TournamentService;
import org.play.domain.exceptions.BusinessException;
import org.play.domain.models.Match;
import org.play.domain.models.Round;
import org.play.domain.models.Tournament;

import java.util.List;
import java.util.Scanner;

public class TournamentConsoleUI {
    private final TournamentService service;
    private final Scanner scanner;

    public TournamentConsoleUI(TournamentService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            System.out.println("=== GERENCIADOR DE TORNEIOS (MVP) ===");
            System.out.println("1. Criar Novo Torneio");
            System.out.println("2. Listar Meus Torneios / Selecionar");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");

            String option = scanner.nextLine();
            if (option.equals("3")) break;

            try {
                switch (option) {
                    case "1" -> uiCreateTournament();
                    case "2" -> uiListAndSelectTournament();
                    default -> System.out.println("Opção inválida.");
                }
            } catch (BusinessException e) {
                System.out.println("\n[ERRO DE NEGÓCIO] " + e.getMessage() + "\n");
            }
        }
    }

    private void uiCreateTournament() {
        System.out.print("\nEscolha um nome para o seu torneio: ");
        String name = scanner.nextLine();

        System.out.println("Selecione o Modo do torneio:");
        System.out.println("1. Round Robin (Pontos Corridos)");
        System.out.println("2. Torneio Suíço");
        System.out.print("Opção: ");
        String modeOpt = scanner.nextLine();
        String type = modeOpt.equals("2") ? "SWISS" : "ROUND_ROBIN";

        System.out.print("Configure pontos por VITÓRIA (ex: 3): ");
        int win = Integer.parseInt(scanner.nextLine());
        System.out.print("Configure pontos por EMPATE (ex: 1): ");
        int draw = Integer.parseInt(scanner.nextLine());
        System.out.print("Configure pontos por DERROTA (ex: 0): ");
        int loss = Integer.parseInt(scanner.nextLine());

        Tournament t = service.createTournament(name, type, win, draw, loss);
        System.out.println("\nTorneio '" + t.getName() + "' criado com sucesso! ID: " + t.getId() + "\n");
    }

    private void uiListAndSelectTournament() {
        List<Tournament> tournaments = service.getAllTournaments();
        if (tournaments.isEmpty()) {
            System.out.println("\nNenhum torneio cadastrado ainda.\n");
            return;
        }

        System.out.println("\n=== SEUS TORNEIOS ===");
        for (Tournament t : tournaments) {
            System.out.printf("[%s] - %s (%s) - %d Jogadores\n", t.getId(), t.getName(), t.getStatus(), t.getPlayers().size());
        }
        System.out.print("Digite o ID do torneio para gerenciar (ou Enter para voltar): ");
        String id = scanner.nextLine();

        if (id.trim().isEmpty()) return;

        Tournament selected = service.getTournamentById(id);
        if (selected == null) {
            System.out.println("Torneio não encontrado.");
            return;
        }

        manageTournamentMenu(selected.getId());
    }

    private void manageTournamentMenu(String tournamentId) {
        while (true) {
            Tournament t = service.getTournamentById(tournamentId); // Recarrega o estado atualizado
            System.out.println("\n=== GERENCIAR: " + t.getName().toUpperCase() + " [" + t.getStatus() + "] ===");
            System.out.println("1. Adicionar Competidor");
            System.out.println("2. Ver Participantes Inscritos");
            System.out.println("3. Iniciar Torneio (Gerar Rodada 1)");
            System.out.println("4. Ver Rodadas e Lançar Resultados");
            System.out.println("5. Ver Classificação (Standings)");
            System.out.println("6. Avançar Rodada / Finalizar Torneio");
            System.out.println("7. Voltar ao Menu Principal");
            System.out.print("Opção: ");

            String opt = scanner.nextLine();
            if (opt.equals("7")) break;

            try {
                switch (opt) {
                    case "1" -> {
                        System.out.print("Nome do competidor: ");
                        String pName = scanner.nextLine();
                        service.registerPlayerInTournament(tournamentId, pName);
                        System.out.println("Competidor adicionado!");
                    }
                    case "2" -> {
                        System.out.println("\n--- PARTICIPANTES ---");
                        t.getPlayers().forEach(p -> System.out.println("- " + p.getName() + " (ID: " + p.getId() + ")"));
                    }
                    case "3" -> {
                        service.startTournament(tournamentId);
                        System.out.println("O torneio foi iniciado e a primeira rodada gerada!");
                    }
                    case "4" -> uiManageMatches(t);
                    case "5" -> StandingView.render(t);
                    case "6" -> {
                        service.advanceRoundOrFinish(tournamentId);
                        System.out.println("Operação executada! Verifique as rodadas ou a classificação.");
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (BusinessException e) {
                System.out.println("\n[AVISO] " + e.getMessage());
            }
        }
    }

    private void uiManageMatches(Tournament t) {
        if (t.getRounds().isEmpty()) {
            System.out.println("Nenhuma rodada gerada ainda. Inicie o torneio primeiro.");
            return;
        }

        System.out.println("\n--- SELECIONE A RODADA ---");
        for (int i = 0; i < t.getRounds().size(); i++) {
            System.out.println(i + " - Rodada " + (i + 1) + " (Concluída: " + t.getRounds().get(i).isRoundCompleted() + ")");
        }
        System.out.print("Escolha o índice da rodada: ");
        int rIndex = Integer.parseInt(scanner.nextLine());

        Round round = t.getRounds().get(rIndex);
        System.out.println("\n--- PARTIDAS DA RODADA " + (rIndex + 1) + " ---");
        for (int j = 0; j < round.getMatches().size(); j++) {
            System.out.println(j + " -> " + round.getMatches().get(j));
        }

        System.out.print("Escolha o índice da partida para lançar resultado (ou -1 para voltar): ");
        int mIndex = Integer.parseInt(scanner.nextLine());
        if (mIndex == -1) return;

        Match match = round.getMatches().get(mIndex);
        System.out.print("Placar para " + match.getPlayer1().getName() + ": ");
        int s1 = Integer.parseInt(scanner.nextLine());
        System.out.print("Placar para " + match.getPlayer2().getName() + ": ");
        int s2 = Integer.parseInt(scanner.nextLine());

        service.postMatchResult(t.getId(), rIndex, mIndex, s1, s2);
        System.out.println("Resultado gravado e classificação atualizada!");
    }
}