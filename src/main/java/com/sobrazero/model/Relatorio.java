package com.sobrazero.model;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

// Dados consolidados de um periodo (RF07). So guarda numeros: quem calcula
// e o RelatorioService e quem exporta o CSV tambem.
public class Relatorio {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private double pesoTotalKg;
    private Map<StatusDoacao, Integer> quantidadePorStatus = new EnumMap<>(StatusDoacao.class);
    private Map<String, Double> pesoPorEstabelecimento = new LinkedHashMap<>();
    private Map<String, Double> pesoPorInstituicao = new LinkedHashMap<>();

    public Relatorio(LocalDate dataInicio, LocalDate dataFim) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public double getPesoTotalKg() {
        return pesoTotalKg;
    }

    public void setPesoTotalKg(double pesoTotalKg) {
        this.pesoTotalKg = pesoTotalKg;
    }

    public Map<StatusDoacao, Integer> getQuantidadePorStatus() {
        return quantidadePorStatus;
    }

    public void setQuantidadePorStatus(Map<StatusDoacao, Integer> quantidadePorStatus) {
        this.quantidadePorStatus = new EnumMap<>(quantidadePorStatus);
    }

    public Map<String, Double> getPesoPorEstabelecimento() {
        return pesoPorEstabelecimento;
    }

    public void setPesoPorEstabelecimento(Map<String, Double> pesoPorEstabelecimento) {
        this.pesoPorEstabelecimento = new LinkedHashMap<>(pesoPorEstabelecimento);
    }

    public Map<String, Double> getPesoPorInstituicao() {
        return pesoPorInstituicao;
    }

    public void setPesoPorInstituicao(Map<String, Double> pesoPorInstituicao) {
        this.pesoPorInstituicao = new LinkedHashMap<>(pesoPorInstituicao);
    }
}
