package com.sobrazero.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Reserva de uma doacao por uma instituicao. No banco, coletas.doacao_id
// e UNIQUE: uma doacao nunca tem duas coletas (RF04).
public class Coleta {

    public static final String STATUS_AGENDADA = "AGENDADA";
    public static final String STATUS_CONCLUIDA = "CONCLUIDA";
    public static final String STATUS_CANCELADA = "CANCELADA";

    private int id;
    private Doacao doacao;
    private Instituicao instituicao;
    private LocalDate dataAgendada;
    private LocalDateTime dataRetirada;
    private String statusColeta;

    public Coleta() {
        this.statusColeta = STATUS_AGENDADA;
    }

    public Coleta(Doacao doacao, Instituicao instituicao, LocalDate dataAgendada) {
        this();
        this.doacao = doacao;
        this.instituicao = instituicao;
        this.dataAgendada = dataAgendada;
    }

    // Registra data e hora da retirada (RF05). Mudar o status da Doacao para
    // CONCLUIDA continua sendo trabalho do service.
    public void confirmarRetirada() {
        this.dataRetirada = LocalDateTime.now();
        this.statusColeta = STATUS_CONCLUIDA;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Doacao getDoacao() {
        return doacao;
    }

    public void setDoacao(Doacao doacao) {
        this.doacao = doacao;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public LocalDate getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDate dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public String getStatusColeta() {
        return statusColeta;
    }

    public void setStatusColeta(String statusColeta) {
        this.statusColeta = statusColeta;
    }
}
