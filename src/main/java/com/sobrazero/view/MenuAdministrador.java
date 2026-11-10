package com.sobrazero.view;

import com.sobrazero.model.Usuario;
import com.sobrazero.service.AutenticacaoService;
import com.sobrazero.service.RegraNegocioException;

import java.util.List;
import java.util.Scanner;

public class MenuAdministrador {

    private final Scanner entrada;
    private final AutenticacaoService servico = new AutenticacaoService();

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
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha uma opção: ");

            String opcao = entrada.nextLine().trim();
            switch (opcao) {
                case "1" -> listarUsuarios();
                case "2" -> cadastrarAdministrador();
                case "3" -> editarUsuario();
                case "4" -> excluirUsuario(usuarioLogado);
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite um número de 0 a 4.");
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
}
