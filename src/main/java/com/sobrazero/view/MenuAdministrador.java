package com.sobrazero.view;

import com.sobrazero.model.Usuario;

import java.util.Scanner;

public class MenuAdministrador {

    private final Scanner entrada;

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
                case "1" -> System.out.println("Listagem ainda não implementada.");
                case "2" -> System.out.println("Cadastro de administrador ainda não implementado.");
                case "3" -> System.out.println("Edição ainda não implementada.");
                case "4" -> System.out.println("Exclusão ainda não implementada.");
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite um número de 0 a 4.");
            }
        }
    }
}
