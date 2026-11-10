package com.sobrazero.view;

import com.sobrazero.service.AutenticacaoService;
import com.sobrazero.service.RegraNegocioException;

import java.util.Scanner;

public class MenuCadastro {

    private final Scanner entrada;
    private final AutenticacaoService servico = new AutenticacaoService();

    public MenuCadastro(Scanner entrada) {
        this.entrada = entrada;
    }

    public void exibir() {
        System.out.println();
        System.out.println("=== CADASTRAR USUÁRIO ===");
        System.out.println("1 - Estabelecimento");
        System.out.println("2 - Instituição");
        System.out.print("Perfil: ");
        String perfil = entrada.nextLine().trim();
        if (!perfil.equals("1") && !perfil.equals("2")) {
            System.out.println("Perfil inválido. Digite 1 ou 2.");
            return;
        }

        String nome = lerObrigatorio("Nome: ");
        String email = lerObrigatorio("E-mail: ");
        String senha = lerSenhaConfirmada();
        String cnpj = lerObrigatorio("CNPJ: ");

        try {
            if (perfil.equals("1")) {
                String endereco = lerObrigatorio("Endereço: ");
                servico.cadastrarEstabelecimento(nome, email, senha, cnpj, endereco);
            } else {
                double capacidadeKg = lerCapacidadeKg();
                servico.cadastrarInstituicao(nome, email, senha, cnpj, capacidadeKg);
            }
            System.out.println("Cadastro realizado! Agora é só entrar com seu e-mail e senha.");
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private String lerObrigatorio(String rotulo) {
        while (true) {
            System.out.print(rotulo);
            String valor = entrada.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("Este campo é obrigatório.");
        }
    }

    private String lerSenhaConfirmada() {
        while (true) {
            String senha = MenuLogin.lerSenha(entrada, "Senha: ");
            String confirmacao = MenuLogin.lerSenha(entrada, "Confirme a senha: ");
            if (senha.isEmpty()) {
                System.out.println("A senha é obrigatória.");
            } else if (!senha.equals(confirmacao)) {
                System.out.println("As senhas não conferem. Tente de novo.");
            } else {
                return senha;
            }
        }
    }

    private double lerCapacidadeKg() {
        while (true) {
            String valor = lerObrigatorio("Capacidade de recebimento (kg): ").replace(',', '.');
            try {
                double capacidade = Double.parseDouble(valor);
                if (Double.isFinite(capacidade) && capacidade > 0) {
                    return capacidade;
                }
            } catch (NumberFormatException e) {
                // texto que nao e numero recebe a mesma mensagem de valor invalido
            }
            System.out.println("Informe um número maior que zero, como 150 ou 72,5.");
        }
    }
}
