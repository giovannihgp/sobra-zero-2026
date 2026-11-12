package com.sobrazero.dao;

import com.sobrazero.model.StatusDoacao;
import com.sobrazero.util.ConexaoBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RelatorioDAO {

    // Uma linha por item de doacao ja retirada: o service converte a unidade e soma
    public record ItemResgatado(String estabelecimento, String instituicao, double quantidade, String unidade) {
    }

    private static final String SQL_ITENS_RESGATADOS =
            "SELECT e.nome AS estabelecimento, ins.nome AS instituicao, i.quantidade, i.unidade_medida "
            + "FROM coletas c "
            + "JOIN doacoes d ON d.id = c.doacao_id "
            + "JOIN usuarios e ON e.id = d.estabelecimento_id "
            + "JOIN usuarios ins ON ins.id = c.instituicao_id "
            + "JOIN itens_doacao i ON i.doacao_id = d.id "
            + "WHERE c.status_coleta = 'CONCLUIDA' AND CAST(c.data_retirada AS DATE) BETWEEN ? AND ?";

    private static final String SQL_DOACOES_POR_STATUS =
            "SELECT status, COUNT(*) AS quantidade FROM doacoes "
            + "WHERE data_cadastro BETWEEN ? AND ? GROUP BY status";

    public List<ItemResgatado> listarItensResgatados(LocalDate inicio, LocalDate fim) throws SQLException {
        List<ItemResgatado> itens = new ArrayList<>();
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ITENS_RESGATADOS)) {
            comando.setObject(1, inicio);
            comando.setObject(2, fim);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    itens.add(new ItemResgatado(
                            resultado.getString("estabelecimento"),
                            resultado.getString("instituicao"),
                            resultado.getDouble("quantidade"),
                            resultado.getString("unidade_medida")));
                }
            }
        }
        return itens;
    }

    public Map<StatusDoacao, Integer> contarDoacoesPorStatus(LocalDate inicio, LocalDate fim) throws SQLException {
        Map<StatusDoacao, Integer> contagem = new EnumMap<>(StatusDoacao.class);
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_DOACOES_POR_STATUS)) {
            comando.setObject(1, inicio);
            comando.setObject(2, fim);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    contagem.put(StatusDoacao.valueOf(resultado.getString("status")), resultado.getInt("quantidade"));
                }
            }
        }
        return contagem;
    }
}
