package com.sobrazero.model;

import java.time.LocalDate;

public class DoacaoNaoPerecivel extends Doacao {

    private String lote;
    private int mesesEstocagem;

    public DoacaoNaoPerecivel() {
        super();
    }

    public DoacaoNaoPerecivel(String lote, int mesesEstocagem) {
        super();
        this.lote = lote;
        this.mesesEstocagem = mesesEstocagem;
    }

    public DoacaoNaoPerecivel(int id, LocalDate dataCadastro, StatusDoacao status,
                              String lote, int mesesEstocagem) {
        super(id, dataCadastro, status);
        this.lote = lote;
        this.mesesEstocagem = mesesEstocagem;
    }

    @Override
    public int calcularPrioridade() {
        final int PRIORIDADE_BAIXA = 10;
        return PRIORIDADE_BAIXA;
    }

    @Override
    public String descreverManuseio() {
        String descricao = "Nao perecivel: guardar em local seco e arejado";
        if (lote != null && !lote.isBlank()) {
            descricao += ", lote " + lote;
        }
        if (mesesEstocagem > 0) {
            descricao += ", estocagem de ate " + mesesEstocagem + " meses";
        }
        return descricao + ".";
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public int getMesesEstocagem() {
        return mesesEstocagem;
    }

    public void setMesesEstocagem(int mesesEstocagem) {
        this.mesesEstocagem = mesesEstocagem;
    }
}
