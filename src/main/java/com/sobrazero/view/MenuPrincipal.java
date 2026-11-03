package com.sobrazero.view;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuPrincipal {

    private final Scanner entrada = new Scanner(System.in);
    private final MenuLogin menuLogin = new MenuLogin(entrada);
    private final MenuCadastro menuCadastro = new MenuCadastro(entrada);

    public static void main(String[] args) {
        try {
            new MenuPrincipal().exibir();
        } catch (NoSuchElementException e) {
            // o Scanner lanca esta excecao quando a entrada termina (ex.: Ctrl+Z)
            System.out.println();
            System.out.println("Entrada encerrada. Até logo!");
        }
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
                case "1" -> menuLogin.exibir();
                case "2" -> menuCadastro.exibir();
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite 1, 2 ou 0.");
            }
        }
        System.out.println("Até logo!");
    }
}
