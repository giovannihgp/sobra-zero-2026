package com.sobrazero.dao;

import com.sobrazero.model.Coleta;
import com.sobrazero.model.Doacao;
import com.sobrazero.model.DoacaoNaoPerecivel;
import com.sobrazero.model.DoacaoPerecivel;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.ItemDoacao;
import com.sobrazero.model.StatusDoacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ColetaDAO {

    private static final String SQL_AGENDAR_DOACAO =
            "UPDATE doacoes SET status = ? WHERE id = ? AND status = ?";

    private static final String SQL_INSERIR =
            "INSERT INTO coletas (doacao_id, instituicao_id, data_agendada, status_coleta) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SQL_CONCLUIR_COLETA =
            "UPDATE coletas SET data_retirada = ?, status_coleta = ? "
            + "WHERE id = ? AND status_coleta = ? "
            + "AND doacao_id IN (SELECT id FROM doacoes WHERE estabelecimento_id = ?)";

    private static final String SQL_EXPIRAR_VENCIDAS =
            "UPDATE doacoes SET status = ? "
            + "WHERE tipo_doacao = ? AND status = ? AND data_validade < CURRENT_DATE";

    private static final String SQL_LISTAR_DISPONIVEIS =
            "SELECT d.id, d.tipo_doacao, d.data_cadastro, d.data_validade, d.exige_refrigeracao, "
            + "d.lote, d.meses_estocagem, u.id AS estabelecimento_id, u.nome, u.email, "
            + "u.documento, u.endereco "
            + "FROM doacoes d JOIN usuarios u ON u.id = d.estabelecimento_id "
            + "WHERE d.status = ? ORDER BY d.id";

    private static final String SQL_ITENS_DISPONIVEIS =
            "SELECT i.id, i.doacao_id, i.descricao, i.quantidade, i.unidade_medida "
            + "FROM itens_doacao i JOIN doacoes d ON d.id = i.doacao_id "
            + "WHERE d.status = ? ORDER BY i.doacao_id, i.id";

    private static final String SQL_CONCLUIR_DOACAO =
            "UPDATE doacoes SET status = ? WHERE id = ? AND status = ?";

    public boolean marcarDoacaoAgendada(Connection conexao, int doacaoId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(SQL_AGENDAR_DOACAO)) {
            ps.setString(1, StatusDoacao.AGENDADA.name());
            ps.setInt(2, doacaoId);
            ps.setString(3, StatusDoacao.DISPONIVEL.name());
            return ps.executeUpdate() == 1;
        }
    }

    public void inserir(Connection conexao, Coleta coleta) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, coleta.getDoacao().getId());
            ps.setInt(2, coleta.getInstituicao().getId());
            ps.setDate(3, Date.valueOf(coleta.getDataAgendada()));
            ps.setString(4, coleta.getStatusColeta());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    coleta.setId(chaves.getInt(1));
                }
            }
        }
    }

    public boolean concluirColeta(Connection conexao, Coleta coleta, int estabelecimentoId)
            throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(SQL_CONCLUIR_COLETA)) {
            ps.setTimestamp(1, Timestamp.valueOf(coleta.getDataRetirada()));
            ps.setString(2, Coleta.STATUS_CONCLUIDA);
            ps.setInt(3, coleta.getId());
            ps.setString(4, Coleta.STATUS_AGENDADA);
            ps.setInt(5, estabelecimentoId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean marcarDoacaoConcluida(Connection conexao, int doacaoId) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(SQL_CONCLUIR_DOACAO)) {
            ps.setString(1, StatusDoacao.CONCLUIDA.name());
            ps.setInt(2, doacaoId);
            ps.setString(3, StatusDoacao.AGENDADA.name());
            return ps.executeUpdate() == 1;
        }
    }

    public int expirarVencidas(Connection conexao) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(SQL_EXPIRAR_VENCIDAS)) {
            ps.setString(1, StatusDoacao.EXPIRADA.name());
            ps.setString(2, "PERECIVEL");
            ps.setString(3, StatusDoacao.DISPONIVEL.name());
            return ps.executeUpdate();
        }
    }

    public List<Doacao> listarDisponiveis(Connection conexao) throws SQLException {
        Map<Integer, Doacao> doacoes = new LinkedHashMap<>();

        try (PreparedStatement ps = conexao.prepareStatement(SQL_LISTAR_DISPONIVEIS)) {
            ps.setString(1, StatusDoacao.DISPONIVEL.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Doacao doacao = montarDoacao(rs);
                    if (doacao != null) {
                        doacoes.put(doacao.getId(), doacao);
                    }
                }
            }
        }

        try (PreparedStatement ps = conexao.prepareStatement(SQL_ITENS_DISPONIVEIS)) {
            ps.setString(1, StatusDoacao.DISPONIVEL.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Doacao doacao = doacoes.get(rs.getInt("doacao_id"));
                    if (doacao != null) {
                        doacao.adicionarItem(new ItemDoacao(
                                rs.getInt("id"),
                                rs.getString("descricao"),
                                rs.getDouble("quantidade"),
                                rs.getString("unidade_medida")));
                    }
                }
            }
        }

        return new ArrayList<>(doacoes.values());
    }

    private Doacao montarDoacao(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        LocalDate dataCadastro = rs.getDate("data_cadastro").toLocalDate();
        Date validade = rs.getDate("data_validade");

        Doacao doacao;
        switch (rs.getString("tipo_doacao")) {
            case "PERECIVEL":
                if (validade == null) {
                    return null;
                }
                doacao = new DoacaoPerecivel(id, dataCadastro, StatusDoacao.DISPONIVEL,
                        validade.toLocalDate(), rs.getBoolean("exige_refrigeracao"));
                break;
            case "NAO_PERECIVEL":
                doacao = new DoacaoNaoPerecivel(id, dataCadastro, StatusDoacao.DISPONIVEL,
                        rs.getString("lote"), rs.getInt("meses_estocagem"));
                break;
            default:
                return null;
        }

        doacao.setEstabelecimento(new Estabelecimento(
                rs.getInt("estabelecimento_id"),
                rs.getString("nome"),
                rs.getString("email"),
                null,
                rs.getString("documento"),
                rs.getString("endereco")));
        return doacao;
    }
}
