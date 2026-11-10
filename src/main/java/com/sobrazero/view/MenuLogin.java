package com.sobrazero.view;

import com.sobrazero.model.Usuario;
import com.sobrazero.service.AutenticacaoService;
import com.sobrazero.service.RegraNegocioException;

import java.io.Console;
import java.util.Scanner;

public class MenuLogin {

    private final Scanner entrada;
    private final AutenticacaoService servico = new AutenticacaoService();

    public MenuLogin(Scanner entrada) {
        this.entrada = entrada;
    }

    // Devolve o usuario autenticado, ou null se o login falhar
    public Usuario exibir() {
        System.out.println();
        System.out.println("=== ENTRAR ===");
        System.out.print("E-mail: ");
        String email = entrada.nextLine().trim();
        String senha = lerSenha(entrada, "Senha: ");

        if (email.isEmpty() || senha.isEmpty()) {
            System.out.println("Informe e-mail e senha.");
            return null;
        }
        try {
            Usuario usuario = servico.login(email, senha);
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");
            return usuario;
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
            return null;
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
}
