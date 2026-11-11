package com.sobrazero.dao;

import com.sobrazero.model.Coleta;
import com.sobrazero.model.StatusDoacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

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
}
