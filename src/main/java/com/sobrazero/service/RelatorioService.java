package com.sobrazero.service;

import com.sobrazero.dao.RelatorioDAO;
import com.sobrazero.dao.RelatorioDAO.ItemResgatado;
import com.sobrazero.model.Relatorio;
import com.sobrazero.model.StatusDoacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class RelatorioService {

    private static final Locale BRASIL = Locale.of("pt", "BR");

    private final RelatorioDAO relatorioDAO = new RelatorioDAO();

    public Relatorio gerar(LocalDate inicio, LocalDate fim) throws RegraNegocioException {
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser depois da data final.");
        }
        Relatorio relatorio = new Relatorio(inicio, fim);
        try {
            preencherPesos(relatorio, relatorioDAO.listarItensResgatados(inicio, fim));
            preencherStatus(relatorio, relatorioDAO.contarDoacoesPorStatus(inicio, fim));
        } catch (SQLException e) {
            throw new RegraNegocioException(
                    "Não foi possível acessar o banco de dados. Verifique se o PostgreSQL está rodando.", e);
        }
        return relatorio;
    }

    private void preencherPesos(Relatorio relatorio, List<ItemResgatado> itens) {
        double total = 0;
        Map<String, Double> porEstabelecimento = new TreeMap<>();
        Map<String, Double> porInstituicao = new TreeMap<>();
        for (ItemResgatado item : itens) {
            double kg = emQuilos(item.quantidade(), item.unidade());
            total += kg;
            porEstabelecimento.merge(item.estabelecimento(), kg, Double::sum);
            porInstituicao.merge(item.instituicao(), kg, Double::sum);
        }
        relatorio.setPesoTotalKg(total);
        relatorio.setPesoPorEstabelecimento(porEstabelecimento);
        relatorio.setPesoPorInstituicao(porInstituicao);
    }

    // Mesma regra de Doacao.calcularPesoTotal(): so kg e g viram peso; un e cx ficam de fora
    private double emQuilos(double quantidade, String unidade) {
        String unidadeNormalizada = unidade == null ? "" : unidade.trim().toLowerCase();
        return switch (unidadeNormalizada) {
            case "kg" -> quantidade;
            case "g" -> quantidade / 1000.0;
            default -> 0;
        };
    }

    // Status sem nenhuma doacao no periodo aparecem com zero, em vez de sumir do relatorio
    private void preencherStatus(Relatorio relatorio, Map<StatusDoacao, Integer> contagemDoBanco) {
        Map<StatusDoacao, Integer> contagem = new EnumMap<>(StatusDoacao.class);
        for (StatusDoacao status : StatusDoacao.values()) {
            contagem.put(status, 0);
        }
        contagem.putAll(contagemDoBanco);
        relatorio.setQuantidadePorStatus(contagem);
    }
}
