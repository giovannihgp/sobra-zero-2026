package com.sobrazero.model;

// Item que compoe uma Doacao. Nao existe fora dela (composicao forte):
// no banco, itens_doacao.doacao_id tem ON DELETE CASCADE.
public class ItemDoacao {

    private int id;
    private String descricao;
    private double quantidade;
    private String unidade;

    public ItemDoacao() {
    }

    public ItemDoacao(String descricao, double quantidade, String unidade) {
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.unidade = unidade;
    }

    public ItemDoacao(int id, String descricao, double quantidade, String unidade) {
        this(descricao, quantidade, unidade);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
}
