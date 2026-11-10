package com.sobrazero.view;

import com.sobrazero.model.Usuario;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

public class MenuPrincipal {

    private final Scanner entrada = new Scanner(System.in);
    private final MenuLogin menuLogin = new MenuLogin(entrada);
    private final MenuCadastro menuCadastro = new MenuCadastro(entrada);
    private final MenuAdministrador menuAdministrador = new MenuAdministrador(entrada);

    // Liga o perfil devolvido por obterPerfil() ao menu daquele perfil, sem if/instanceof por tipo
    private final Map<String, Consumer<Usuario>> menusPorPerfil = Map.of(
            "ADMINISTRADOR", menuAdministrador::exibir,
            "ESTABELECIMENTO", usuario -> System.out.println("O menu do estabelecimento ainda está em desenvolvimento."),
            "INSTITUICAO", usuario -> System.out.println("O menu da instituição ainda está em desenvolvimento."));

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
                case "1" -> entrar();
                case "2" -> menuCadastro.exibir();
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite 1, 2 ou 0.");
            }
        }
        System.out.println("Até logo!");
    }

    private void entrar() {
        Usuario usuario = menuLogin.exibir();
        if (usuario != null) {
            menusPorPerfil.get(usuario.obterPerfil()).accept(usuario);
        }
    }
}
