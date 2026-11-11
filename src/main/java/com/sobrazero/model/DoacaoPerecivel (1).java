package com.sobrazero.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoacaoPerecivel extends Doacao {

    private LocalDate dataValidade;
    private boolean exigeRefrigeracao;

    // Nao existe construtor sem data de validade: perecivel sem validade
    // e erro de modelagem (RF02) e tem que falhar na hora de criar.
    public DoacaoPerecivel(LocalDate dataValidade, boolean exigeRefrigeracao) {
        super();
        this.dataValidade = exigirValidade(dataValidade);
        this.exigeRefrigeracao = exigeRefrigeracao;
    }

    public DoacaoPerecivel(int id, LocalDate dataCadastro, StatusDoacao status,
                           LocalDate dataValidade, boolean exigeRefrigeracao) {
        super(id, dataCadastro, status);
        this.dataValidade = exigirValidade(dataValidade);
        this.exigeRefrigeracao = exigeRefrigeracao;
    }

    @Override
    public int calcularPrioridade() {
        final int PRIORIDADE_VENCIDA = 1000;
        final int PRIORIDADE_BASE = 100;
        final int JANELA_HORAS = 720;
        final int BONUS_REFRIGERACAO = 50;

        LocalDateTime fimDoDia = dataValidade.plusDays(1).atStartOfDay();
        long horasRestantes = Duration.between(LocalDateTime.now(), fimDoDia).toHours();

        if (horasRestantes <= 0) {
            return PRIORIDADE_VENCIDA;
        }

        int prioridade = PRIORIDADE_BASE + (JANELA_HORAS - (int) Math.min(horasRestantes, JANELA_HORAS));
        if (exigeRefrigeracao) {
            prioridade += BONUS_REFRIGERACAO;
        }
        return prioridade;
    }

    @Override
    public String descreverManuseio() {
        String limite = dataValidade.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        if (exigeRefrigeracao) {
            return "Perecivel: manter refrigerado ate a retirada e retirar antes de " + limite + ".";
        }
        return "Perecivel: manter em local fresco e retirar antes de " + limite + ".";
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = exigirValidade(dataValidade);
    }

    private static LocalDate exigirValidade(LocalDate dataValidade) {
        if (dataValidade == null) {
            throw new IllegalArgumentException("Doacao perecivel exige data de validade.");
        }
        return dataValidade;
    }

    public boolean isExigeRefrigeracao() {
        return exigeRefrigeracao;
    }

    public void setExigeRefrigeracao(boolean exigeRefrigeracao) {
        this.exigeRefrigeracao = exigeRefrigeracao;
    }
}
