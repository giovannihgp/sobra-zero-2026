package com.sobrazero.service;

import com.sobrazero.dao.ColetaDAO;
import com.sobrazero.model.Coleta;
import com.sobrazero.model.Doacao;
import com.sobrazero.model.Instituicao;
import com.sobrazero.model.StatusDoacao;
import com.sobrazero.util.ConexaoBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class ColetaService {

    private static final String SQLSTATE_VIOLACAO_UNICA = "23505";

    private final ColetaDAO coletaDAO = new ColetaDAO();

    public Coleta reservar(Doacao doacao, Instituicao instituicao, LocalDate dataAgendada)
            throws ColetaException {
        if (doacao == null || instituicao == null) {
            throw new ColetaException("Doacao e instituicao sao obrigatorias.");
        }
        if (dataAgendada == null || dataAgendada.isBefore(LocalDate.now())) {
            throw new ColetaException("Informe uma data de retirada de hoje em diante.");
        }

        Coleta coleta = instituicao.agendarColeta(doacao, dataAgendada);

        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false);
            try {
                if (!coletaDAO.marcarDoacaoAgendada(conexao, doacao.getId())) {
                    conexao.rollback();
                    throw new ColetaException(
                            "Doacao nao esta disponivel para reserva (ja reservada ou vencida).");
                }
                coletaDAO.inserir(conexao, coleta);
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                if (SQLSTATE_VIOLACAO_UNICA.equals(e.getSQLState())) {
                    throw new ColetaException("Doacao ja reservada.");
                }
                throw new ColetaException("Nao foi possivel reservar a doacao. Tente novamente.");
            }
        } catch (SQLException e) {
            throw new ColetaException("Erro ao acessar o banco de dados. Tente novamente.");
        }

        doacao.setStatus(StatusDoacao.AGENDADA);
        return coleta;
    }
}
