package com.sobrazero.model;

import java.time.LocalDate;

public class Instituicao extends Usuario {

    private String cnpj;
    private double capacidadeKg;

    public Instituicao() {
    }

    public Instituicao(int id, String nome, String email, String senhaHash,
                       String cnpj, double capacidadeKg) {
        super(id, nome, email, senhaHash);
        this.cnpj = cnpj;
        this.capacidadeKg = capacidadeKg;
    }

    // Monta o objeto Coleta em memoria. Quem valida a reserva (doacao ainda
    // DISPONIVEL), muda o status e grava no banco e o ColetaService.
    public Coleta agendarColeta(Doacao doacao, LocalDate dataAgendada) {
        if (doacao == null) {
            throw new IllegalArgumentException("Doacao nao pode ser nula.");
        }
        return new Coleta(doacao, this, dataAgendada);
    }

    @Override
    public String obterPerfil() {
        return "INSTITUICAO";
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public double getCapacidadeKg() {
        return capacidadeKg;
    }

    public void setCapacidadeKg(double capacidadeKg) {
        this.capacidadeKg = capacidadeKg;
    }
}
