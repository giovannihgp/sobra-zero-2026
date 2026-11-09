package com.sobrazero.model;

import java.time.LocalDate;

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

    // Stub do Bloco 0: a logica (horas ate o vencimento) e do Gabriel
    @Override
    public int calcularPrioridade() {
        return 0;
    }

    // Stub do Bloco 0: implementado pelo Gabriel
    @Override
    public String descreverManuseio() {
        return "";
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
