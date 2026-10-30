package com.sobrazero.view;

import java.io.Console;
import java.util.Scanner;

public class MenuLogin {

    private final Scanner entrada;

    public MenuLogin(Scanner entrada) {
        this.entrada = entrada;
    }

    public void exibir() {
        System.out.println();
        System.out.println("=== ENTRAR ===");
        System.out.print("E-mail: ");
        String email = entrada.nextLine().trim();
        String senha = lerSenha("Senha: ");

        if (email.isEmpty() || senha.isEmpty()) {
            System.out.println("Informe e-mail e senha.");
            return;
        }
        System.out.println("Autenticação ainda não implementada.");
    }

    private String lerSenha(String rotulo) {
        Console console = System.console();
        // null quando o programa nao roda num terminal interativo (ex.: botao Run da IDE)
        if (console == null) {
            System.out.print(rotulo);
            return entrada.nextLine();
        }
        char[] senha = console.readPassword(rotulo);
        return senha == null ? "" : new String(senha);
    }
}
