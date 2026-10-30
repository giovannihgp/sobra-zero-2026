package com.sobrazero.view;

import java.util.Scanner;

public class MenuPrincipal {

    private final Scanner entrada = new Scanner(System.in);

    public static void main(String[] args) {
        new MenuPrincipal().exibir();
    }

    public void exibir() {
        boolean executando = true;
        while (executando) {
            System.out.println();
            System.out.println("=== SOBRA ZERO ===");
            System.out.println("1 - Entrar");
            System.out.println("2 - Cadastrar usuário");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            String opcao = entrada.nextLine().trim();
            switch (opcao) {
                case "1" -> System.out.println("Login ainda não implementado.");
                case "2" -> System.out.println("Cadastro ainda não implementado.");
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite 1, 2 ou 0.");
            }
        }
        System.out.println("Até logo!");
    }
}
