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

    // Stub do Bloco 0: prioridade baixa e constante, definida pelo Gabriel
    @Override
    public int calcularPrioridade() {
        return 0;
    }

    // Stub do Bloco 0: implementado pelo Gabriel
    @Override
    public String descreverManuseio() {
        return "";
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
