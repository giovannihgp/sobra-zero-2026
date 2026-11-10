package com.sobrazero.view;

import java.io.Console;
import java.util.Scanner;

// Leituras de teclado repetidas em varios menus. Todas recebem o mesmo Scanner do MenuPrincipal.
final class EntradaConsole {

    private EntradaConsole() {
    }

    static String lerObrigatorio(Scanner entrada, String rotulo) {
        while (true) {
            System.out.print(rotulo);
            String valor = entrada.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("Este campo é obrigatório.");
        }
    }

    static String lerSenha(Scanner entrada, String rotulo) {
        Console console = System.console();
        // null quando o programa nao roda num terminal interativo (ex.: botao Run da IDE)
        if (console == null) {
            System.out.print(rotulo);
            return entrada.nextLine();
        }
        char[] senha = console.readPassword(rotulo);
        return senha == null ? "" : new String(senha);
    }

    static String lerSenhaConfirmada(Scanner entrada) {
        while (true) {
            String senha = lerSenha(entrada, "Senha: ");
            String confirmacao = lerSenha(entrada, "Confirme a senha: ");
            if (senha.isEmpty()) {
                System.out.println("A senha é obrigatória.");
            } else if (!senha.equals(confirmacao)) {
                System.out.println("As senhas não conferem. Tente de novo.");
            } else {
                return senha;
            }
        }
    }
}
