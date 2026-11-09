package com.sobrazero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Classe abstrata: toda doacao e perecivel ou nao perecivel.
// calcularPrioridade() e descreverManuseio() sao o ponto de polimorfismo:
// o service trabalha com List<Doacao> sem saber o tipo concreto.
public abstract class Doacao {

    private int id;
    private LocalDate dataCadastro;
    private StatusDoacao status;
    // Dono da doacao: o painel da instituicao usa para mostrar de qual mercado ela e.
    // No banco corresponde a doacoes.estabelecimento_id
    private Estabelecimento estabelecimento;
    // Composicao 1:N: os itens so existem dentro da doacao
    private List<ItemDoacao> itens = new ArrayList<>();

    // Mesmos valores padrao do schema.sql (data_cadastro e status)
    protected Doacao() {
        this.dataCadastro = LocalDate.now();
        this.status = StatusDoacao.DISPONIVEL;
    }

    protected Doacao(int id, LocalDate dataCadastro, StatusDoacao status) {
        this.id = id;
        this.dataCadastro = dataCadastro;
        this.status = status;
    }

    public void adicionarItem(ItemDoacao item) {
        if (item == null) {
            throw new IllegalArgumentException("Item nao pode ser nulo.");
        }
        itens.add(item);
    }

    // Soma so o que e medido em peso (kg e g). Itens em "un" nao entram,
    // pois nao ha como converter unidade em quilo.
    public double calcularPesoTotal() {
        double totalKg = 0;
        for (ItemDoacao item : itens) {
            String unidade = item.getUnidade() == null ? "" : item.getUnidade().trim().toLowerCase();
            if (unidade.equals("kg")) {
                totalKg += item.getQuantidade();
            } else if (unidade.equals("g")) {
                totalKg += item.getQuantidade() / 1000.0;
            }
        }
        return totalKg;
    }

    // Quanto maior o numero, mais urgente a coleta
    public abstract int calcularPrioridade();

    // Texto com a orientacao de manuseio, que depende do tipo da doacao
    public abstract String descreverManuseio();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    // Publico porque o service fica em outro pacote. Por regra do projeto,
    // so os metodos de transicao do DoacaoService devem chamar este setter
    // (alem do DAO, ao montar o objeto lido do banco).
    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    public Estabelecimento getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(Estabelecimento estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    // Lista somente leitura: para incluir use adicionarItem()
    public List<ItemDoacao> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void setItens(List<ItemDoacao> itens) {
        this.itens = new ArrayList<>(itens);
    }
}
