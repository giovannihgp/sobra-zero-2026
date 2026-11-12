package com.sobrazero.view;

import com.sobrazero.model.Relatorio;
import com.sobrazero.model.Usuario;
import com.sobrazero.service.AutenticacaoService;
import com.sobrazero.service.RegraNegocioException;
import com.sobrazero.service.RelatorioService;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MenuAdministrador {

    private final Scanner entrada;
    private final AutenticacaoService servico = new AutenticacaoService();
    private final RelatorioService relatorioServico = new RelatorioService();

    public MenuAdministrador(Scanner entrada) {
        this.entrada = entrada;
    }

    public void exibir(Usuario usuarioLogado) {
        boolean executando = true;
        while (executando) {
            System.out.println();
            System.out.println("=== ADMINISTRADOR ===");
            System.out.println("1 - Listar usuários");
            System.out.println("2 - Cadastrar administrador");
            System.out.println("3 - Editar usuário");
            System.out.println("4 - Excluir usuário");
            System.out.println("5 - Relatório por período");
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha uma opção: ");

            String opcao = entrada.nextLine().trim();
            switch (opcao) {
                case "1" -> listarUsuarios();
                case "2" -> cadastrarAdministrador();
                case "3" -> editarUsuario();
                case "4" -> excluirUsuario(usuarioLogado);
                case "5" -> gerarRelatorio();
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite um número de 0 a 5.");
            }
        }
    }

    private void listarUsuarios() {
        try {
            List<Usuario> usuarios = servico.listarUsuarios();
            System.out.println();
            System.out.printf("%-4s %-16s %-30s %s%n", "ID", "PERFIL", "NOME", "E-MAIL");
            for (Usuario usuario : usuarios) {
                System.out.printf("%-4d %-16s %-30s %s%n",
                        usuario.getId(), usuario.obterPerfil(), usuario.getNome(), usuario.getEmail());
            }
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cadastrarAdministrador() {
        System.out.println();
        System.out.println("=== NOVO ADMINISTRADOR ===");
        String nome = EntradaConsole.lerObrigatorio(entrada, "Nome: ");
        String email = EntradaConsole.lerObrigatorio(entrada, "E-mail: ");
        String senha = EntradaConsole.lerSenhaConfirmada(entrada);
        String matricula = EntradaConsole.lerObrigatorio(entrada, "Matrícula: ");
        try {
            servico.cadastrarAdministrador(nome, email, senha, matricula);
            System.out.println("Administrador cadastrado.");
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void editarUsuario() {
        int id = EntradaConsole.lerInteiro(entrada, "ID do usuário a editar: ");
        try {
            Usuario usuario = servico.buscarUsuario(id);
            System.out.println("Deixe em branco para manter o valor atual.");
            String nome = EntradaConsole.lerOpcional(entrada, "Nome [" + usuario.getNome() + "]: ");
            String email = EntradaConsole.lerOpcional(entrada, "E-mail [" + usuario.getEmail() + "]: ");
            servico.atualizarDadosBasicos(usuario,
                    nome.isEmpty() ? usuario.getNome() : nome,
                    email.isEmpty() ? usuario.getEmail() : email);
            System.out.println("Usuário atualizado.");
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void excluirUsuario(Usuario usuarioLogado) {
        int id = EntradaConsole.lerInteiro(entrada, "ID do usuário a excluir: ");
        try {
            Usuario usuario = servico.buscarUsuario(id);
            String resposta = EntradaConsole.lerOpcional(entrada,
                    "Excluir " + usuario.getNome() + " (" + usuario.obterPerfil() + ")? (s/N): ");
            if (!resposta.equalsIgnoreCase("s")) {
                System.out.println("Exclusão cancelada.");
                return;
            }
            servico.excluirUsuario(id, usuarioLogado);
            System.out.println("Usuário excluído.");
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void gerarRelatorio() {
        System.out.println();
        System.out.println("=== RELATÓRIO POR PERÍODO ===");
        LocalDate inicio = EntradaConsole.lerData(entrada, "Data inicial (dd/mm/aaaa): ");
        LocalDate fim = EntradaConsole.lerData(entrada, "Data final (dd/mm/aaaa): ");
        try {
            Relatorio relatorio = relatorioServico.gerar(inicio, fim);
            exibirRelatorio(relatorio);
            String resposta = EntradaConsole.lerOpcional(entrada, "Exportar para CSV? (s/N): ");
            if (resposta.equalsIgnoreCase("s")) {
                Path arquivo = relatorioServico.exportarCsv(relatorio);
                System.out.println("Arquivo salvo em: " + arquivo);
            }
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void exibirRelatorio(Relatorio relatorio) {
        System.out.println();
        System.out.printf("Total resgatado: %.2f kg%n", relatorio.getPesoTotalKg());
        System.out.println();
        System.out.println("Doações cadastradas no período, por status:");
        relatorio.getQuantidadePorStatus().forEach((status, quantidade) ->
                System.out.printf("  %-12s %d%n", status, quantidade));
        System.out.println();
        System.out.println("Kg resgatados por estabelecimento:");
        imprimirPesos(relatorio.getPesoPorEstabelecimento());
        System.out.println("Kg resgatados por instituição:");
        imprimirPesos(relatorio.getPesoPorInstituicao());
    }

    private void imprimirPesos(Map<String, Double> pesos) {
        if (pesos.isEmpty()) {
            System.out.println("  (nenhuma retirada no período)");
            return;
        }
        pesos.forEach((nome, kg) -> System.out.printf("  %-30s %10.2f kg%n", nome, kg));
    }
}
