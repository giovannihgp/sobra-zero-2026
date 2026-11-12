package com.sobrazero.model;

import java.time.LocalDate;

public class Administrador extends Usuario {

    private String matricula;

    public Administrador() {
    }

    public Administrador(int id, String nome, String email, String senhaHash, String matricula) {
        super(id, nome, email, senhaHash);
        this.matricula = matricula;
    }

    // Devolve o relatorio do periodo ainda vazio. Os numeros vem do banco,
    // entao quem preenche e o RelatorioService (o model nao acessa o DAO).
    public Relatorio gerarRelatorio(LocalDate inicio, LocalDate fim) {
        return new Relatorio(inicio, fim);
    }

    @Override
    public String obterPerfil() {
        return "ADMINISTRADOR";
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}
