package com.livraria;

import com.livraria.config.ConnectionFactory;
import com.livraria.controller.LivroController;
import com.livraria.controller.RecomendacaoController;
import com.livraria.dao.GeneroDAO;
import com.livraria.dao.LogAuditoriaDAO;
import com.livraria.dao.UsuarioDAO;
import com.livraria.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação (Interface Console / Terminal).
 * Demonstra a arquitetura MVC, chamadas de VIEW, Triggers de auditoria e Stored Procedure.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final LivroController livroController = new LivroController();
    private static final RecomendacaoController recomendacaoController = new RecomendacaoController();
    private static final GeneroDAO generoDAO = new GeneroDAO();
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final LogAuditoriaDAO auditoriaDAO = new LogAuditoriaDAO();

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("   UNIUBE - SISTEMA DE RECOMENDAÇÃO DE LIVROS POR GÊNERO (MVC)");
        System.out.println("================================================================");
        System.out.println("Testando conexão com o MySQL em: " + ConnectionFactory.getUrl());

        boolean online = ConnectionFactory.testarConexao();
        if (!online) {
            System.err.println("\n[!] AVISO: Não foi possível conectar ao MySQL agora.");
            System.err.println("Certifique-se de iniciar o banco de dados (ex: 'docker compose up -d')");
            System.err.println("Configurações atuais: " + ConnectionFactory.getUrl() + " (Usuário: " + ConnectionFactory.getUser() + ")\n");
            System.err.println("Você pode ajustar as credenciais no arquivo 'livros/src/main/resources/db.properties'.");
        } else {
            System.out.println("[✓] Conexão com o banco de dados estabelecida com sucesso!\n");
        }

        // Se passar argumento --auto-test, executa a demonstração automatizada
        if (args.length > 0 && "--auto-test".equalsIgnoreCase(args[0])) {
            if (online) {
                executarDemonstracaoAutomatica();
            }
            ConnectionFactory.encerrarDriver();
            return;
        }

        // Loop interativo
        boolean executando = true;
        while (executando) {
            exibirMenu();
            System.out.print("Escolha uma opção: ");
            String opcao = scanner.nextLine().trim();

            try {
                switch (opcao) {
                    case "1":
                        listarLivrosDetalhados();
                        break;
                    case "2":
                        cadastrarLivro();
                        break;
                    case "3":
                        atualizarLivro();
                        break;
                    case "4":
                        excluirLivro();
                        break;
                    case "5":
                        obterRecomendacoes();
                        break;
                    case "6":
                        listarLogsAuditoria();
                        break;
                    case "7":
                        listarUsuarios();
                        break;
                    case "8":
                        listarGeneros();
                        break;
                    case "9":
                        executarDemonstracaoAutomatica();
                        break;
                    case "0":
                        executando = false;
                        System.out.println("Encerrando a aplicação. Até logo!");
                        break;
                    default:
                        System.out.println("[!] Opção inválida. Tente novamente.");
                }
            } catch (Exception e) {
                System.err.println("[Erro] " + e.getMessage());
            }
            System.out.println();
        }

        ConnectionFactory.encerrarDriver();
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("\n----------------- MENU PRINCIPAL -----------------");
        System.out.println("1. Listar Livros (Via VIEW 'vw_livros_detalhados')");
        System.out.println("2. Cadastrar Livro (CRUD - Inserir)");
        System.out.println("3. Atualizar Livro (CRUD - Update / Dispara Trigger)");
        System.out.println("4. Excluir Livro (CRUD - Delete / Dispara Trigger)");
        System.out.println("5. Obter Recomendações (Stored Procedure)");
        System.out.println("6. Ver Logs de Auditoria (Gerados pelas Triggers)");
        System.out.println("7. Listar Usuários e Preferências");
        System.out.println("8. Listar Gêneros Cadastrados");
        System.out.println("9. Executar Demonstração Automática Completa");
        System.out.println("0. Sair");
        System.out.println("--------------------------------------------------");
    }

    private static void listarLivrosDetalhados() throws SQLException {
        System.out.println("\n--- LISTAGEM DE LIVROS (CONSULTA À VIEW: vw_livros_detalhados) ---");
        List<LivroDetalhadoDTO> livros = livroController.listarLivrosDetalhados();
        if (livros.isEmpty()) {
            System.out.println("Nenhum livro cadastrado.");
        } else {
            System.out.printf("%-4s | %-32s | %-22s | %-6s | %s\n", "ID", "TÍTULO", "AUTOR", "ANO", "GÊNERO");
            System.out.println("-----------------------------------------------------------------------------------------");
            for (LivroDetalhadoDTO l : livros) {
                System.out.printf("%-4d | %-32s | %-22s | %-6s | %s\n",
                        l.getLivroId(),
                        limitarTamanho(l.getTitulo(), 32),
                        limitarTamanho(l.getAutor(), 22),
                        l.getAnoPublicacao() != null ? l.getAnoPublicacao() : "-",
                        l.getGeneroNome());
            }
        }
    }

    private static void cadastrarLivro() throws SQLException {
        System.out.println("\n--- CADASTRAR NOVO LIVRO ---");
        System.out.print("Título: ");
        String titulo = scanner.nextLine();

        System.out.print("Autor: ");
        String autor = scanner.nextLine();

        System.out.print("ISBN (opcional): ");
        String isbn = scanner.nextLine();

        System.out.print("Ano de Publicação (ex: 2023): ");
        String anoStr = scanner.nextLine().trim();
        Integer ano = anoStr.isEmpty() ? null : Integer.parseInt(anoStr);

        listarGeneros();
        System.out.print("ID do Gênero: ");
        int generoId = Integer.parseInt(scanner.nextLine().trim());

        Livro criado = livroController.cadastrarLivro(titulo, autor, isbn, ano, generoId);
        System.out.println("[✓] Livro cadastrado com sucesso! ID gerado: " + criado.getId());
    }

    private static void atualizarLivro() throws SQLException {
        System.out.println("\n--- ATUALIZAR LIVRO (DISPARA TRIGGER DE UPDATE) ---");
        System.out.print("ID do livro a atualizar: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Optional<Livro> opt = livroController.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("[!] Livro com ID " + id + " não encontrado.");
            return;
        }

        Livro existente = opt.get();
        System.out.println("Dados atuais: " + existente);

        System.out.print("Novo Título (ENTER para manter '" + existente.getTitulo() + "'): ");
        String titulo = scanner.nextLine().trim();
        if (titulo.isEmpty()) titulo = existente.getTitulo();

        System.out.print("Novo Autor (ENTER para manter '" + existente.getAutor() + "'): ");
        String autor = scanner.nextLine().trim();
        if (autor.isEmpty()) autor = existente.getAutor();

        System.out.print("Novo ISBN (ENTER para manter '" + existente.getIsbn() + "'): ");
        String isbn = scanner.nextLine().trim();
        if (isbn.isEmpty()) isbn = existente.getIsbn();

        System.out.print("Novo Ano de Publicação (ENTER para manter '" + existente.getAnoPublicacao() + "'): ");
        String anoStr = scanner.nextLine().trim();
        Integer ano = anoStr.isEmpty() ? existente.getAnoPublicacao() : Integer.parseInt(anoStr);

        System.out.print("Novo Gênero ID (ENTER para manter '" + existente.getGeneroId() + "'): ");
        String generoStr = scanner.nextLine().trim();
        int generoId = generoStr.isEmpty() ? existente.getGeneroId() : Integer.parseInt(generoStr);

        livroController.atualizarLivro(id, titulo, autor, isbn, ano, generoId);
        System.out.println("[✓] Livro atualizado com sucesso! A trigger 'trg_depois_atualizar_livro' registrou a alteração no log.");
    }

    private static void excluirLivro() throws SQLException {
        System.out.println("\n--- EXCLUIR LIVRO (DISPARA TRIGGER DE DELETE) ---");
        System.out.print("ID do livro a excluir: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Optional<Livro> opt = livroController.buscarPorId(id);
        if (opt.isEmpty()) {
            System.out.println("[!] Livro com ID " + id + " não encontrado.");
            return;
        }

        System.out.print("Tem certeza que deseja excluir o livro \"" + opt.get().getTitulo() + "\" (s/n)? ");
        String confirma = scanner.nextLine().trim();
        if ("s".equalsIgnoreCase(confirma)) {
            livroController.excluirLivro(id);
            System.out.println("[✓] Livro excluído! A trigger 'trg_depois_deletar_livro' gravou o evento no log de auditoria.");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private static void obterRecomendacoes() throws SQLException {
        System.out.println("\n--- RECOMENDAÇÕES POR USUÁRIO (STORED PROCEDURE: sp_obter_recomendacoes_usuario) ---");
        listarUsuarios();
        System.out.print("Informe o ID do usuário para obter recomendações: ");
        int usuarioId = Integer.parseInt(scanner.nextLine().trim());

        List<LivroDetalhadoDTO> recomendacoes = recomendacaoController.obterRecomendacoes(usuarioId);
        if (recomendacoes.isEmpty()) {
            System.out.println("Nenhum livro recomendado para os gêneros preferidos deste usuário.");
        } else {
            System.out.println("\nRecomendações encontradas via Stored Procedure:");
            for (LivroDetalhadoDTO r : recomendacoes) {
                System.out.printf("  • %-30s | Autor: %-20s | Gênero: %s\n",
                        r.getTitulo(), r.getAutor(), r.getGeneroNome());
            }
        }
    }

    private static void listarLogsAuditoria() throws SQLException {
        System.out.println("\n--- LOG DE ALTERAÇÕES DE LIVROS (ALIMENTADO PELAS TRIGGERS) ---");
        List<LogAlteracaoLivro> logs = auditoriaDAO.listarLogs();
        if (logs.isEmpty()) {
            System.out.println("Nenhum registro de auditoria encontrado.");
        } else {
            for (LogAlteracaoLivro log : logs) {
                System.out.println(log);
            }
        }
    }

    private static void listarUsuarios() throws SQLException {
        System.out.println("\n--- USUÁRIOS CADASTRADOS ---");
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        for (Usuario u : usuarios) {
            System.out.println(u);
            if (!u.getGenerosPreferidos().isEmpty()) {
                System.out.print("    Gêneros preferidos: ");
                for (Genero g : u.getGenerosPreferidos()) {
                    System.out.print("[" + g.getNome() + "] ");
                }
                System.out.println();
            }
        }
    }

    private static void listarGeneros() throws SQLException {
        System.out.println("\n--- GÊNEROS LITERÁRIOS ---");
        List<Genero> generos = generoDAO.listarTodos();
        for (Genero g : generos) {
            System.out.println(g);
        }
    }

    private static void executarDemonstracaoAutomatica() {
        try {
            System.out.println("\n=== INICIANDO DEMONSTRAÇÃO COMPLETA DE RECURSOS ===");
            System.out.println("1. Consultando livros detalhados via VIEW:");
            listarLivrosDetalhados();

            System.out.println("\n2. Executando Stored Procedure de Recomendação para Usuário 1 (Aluno ADS):");
            List<LivroDetalhadoDTO> recs = recomendacaoController.obterRecomendacoes(1);
            for (LivroDetalhadoDTO r : recs) {
                System.out.println("   -> " + r.getTitulo() + " (" + r.getGeneroNome() + ")");
            }

            System.out.println("\n=== DEMONSTRAÇÃO CONCLUÍDA ===");
        } catch (Exception e) {
            System.err.println("Falha na demonstração: " + e.getMessage());
        }
    }

    private static String limitarTamanho(String texto, int tamanho) {
        if (texto == null) return "";
        if (texto.length() <= tamanho) return texto;
        return texto.substring(0, tamanho - 3) + "...";
    }
}
