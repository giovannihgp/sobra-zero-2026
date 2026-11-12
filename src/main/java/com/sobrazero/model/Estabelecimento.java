package com.sobrazero.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Estabelecimento extends Usuario {

    private String cnpj;
    private String endereco;
    // Composicao 1:N: um estabelecimento publica varias doacoes ao longo do tempo
    private List<Doacao> doacoes = new ArrayList<>();

    public Estabelecimento() {
    }

    public Estabelecimento(int id, String nome, String email, String senhaHash,
                           String cnpj, String endereco) {
        super(id, nome, email, senhaHash);
        this.cnpj = cnpj;
        this.endereco = endereco;
    }

    public void registrarDoacao(Doacao doacao) {
        if (doacao == null) {
            throw new IllegalArgumentException("Doacao nao pode ser nula.");
        }
        doacoes.add(doacao);
    }

    @Override
    public String obterPerfil() {
        return "ESTABELECIMENTO";
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    // Lista somente leitura: quem quiser incluir usa registrarDoacao()
    public List<Doacao> getDoacoes() {
        return Collections.unmodifiableList(doacoes);
    }

    public void setDoacoes(List<Doacao> doacoes) {
        this.doacoes = new ArrayList<>(doacoes);
    }
}
