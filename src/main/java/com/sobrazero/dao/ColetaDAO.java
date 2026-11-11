package com.sobrazero.dao;

import com.sobrazero.model.Coleta;
import com.sobrazero.model.StatusDoacao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ColetaDAO {

    private static final String SQL_AGENDAR_DOACAO =
            "UPDATE doacoes SET status = ? WHERE id = ? AND status = ?";

    private static final String SQL_INSERIR =
            "INSERT INTO coletas (doacao_id, instituicao_id, data_agendada, status_coleta) "
            + "VALUES (?, ?, ?, ?)";

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
}
