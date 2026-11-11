package com.sobrazero.service;

import com.sobrazero.dao.ColetaDAO;
import com.sobrazero.model.Coleta;
import com.sobrazero.model.Doacao;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.Instituicao;
import com.sobrazero.model.StatusDoacao;
import com.sobrazero.util.ConexaoBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    public void confirmarRetirada(Coleta coleta, Estabelecimento estabelecimento)
            throws ColetaException {
        if (coleta == null || estabelecimento == null) {
            throw new ColetaException("Coleta e estabelecimento sao obrigatorios.");
        }
        if (!Coleta.STATUS_AGENDADA.equals(coleta.getStatusColeta())) {
            throw new ColetaException("Esta coleta nao esta agendada.");
        }

        String statusAnterior = coleta.getStatusColeta();
        LocalDateTime retiradaAnterior = coleta.getDataRetirada();
        coleta.confirmarRetirada();

        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false);
            try {
                if (!coletaDAO.concluirColeta(conexao, coleta, estabelecimento.getId())) {
                    conexao.rollback();
                    throw new ColetaException(
                            "Coleta nao encontrada, ja concluida ou de outro estabelecimento.");
                }
                if (!coletaDAO.marcarDoacaoConcluida(conexao, coleta.getDoacao().getId())) {
                    conexao.rollback();
                    throw new ColetaException("A doacao desta coleta nao esta agendada.");
                }
                conexao.commit();
            } catch (SQLException e) {
                conexao.rollback();
                throw new ColetaException("Nao foi possivel confirmar a retirada. Tente novamente.");
            }
        } catch (SQLException e) {
            desfazerConfirmacao(coleta, statusAnterior, retiradaAnterior);
            throw new ColetaException("Erro ao acessar o banco de dados. Tente novamente.");
        } catch (ColetaException e) {
            desfazerConfirmacao(coleta, statusAnterior, retiradaAnterior);
            throw e;
        }

        coleta.getDoacao().setStatus(StatusDoacao.CONCLUIDA);
    }

    private void desfazerConfirmacao(Coleta coleta, String statusAnterior, LocalDateTime retiradaAnterior) {
        coleta.setStatusColeta(statusAnterior);
        coleta.setDataRetirada(retiradaAnterior);
    }

    public int expirarVencidas() throws ColetaException {
        try (Connection conexao = ConexaoBD.getConexao()) {
            return coletaDAO.expirarVencidas(conexao);
        } catch (SQLException e) {
            throw new ColetaException("Nao foi possivel atualizar as doacoes vencidas.");
        }
    }

    public List<Doacao> listarDisponiveis() throws ColetaException {
        try (Connection conexao = ConexaoBD.getConexao()) {
            return coletaDAO.listarDisponiveis(conexao);
        } catch (SQLException e) {
            throw new ColetaException("Nao foi possivel carregar as doacoes disponiveis.");
        }
    }

    public List<Coleta> listarAgendadas(int estabelecimentoId) throws ColetaException {
        try (Connection conexao = ConexaoBD.getConexao()) {
            return coletaDAO.listarAgendadas(conexao, estabelecimentoId);
        } catch (SQLException e) {
            throw new ColetaException("Nao foi possivel carregar as coletas agendadas.");
        }
    }
}
