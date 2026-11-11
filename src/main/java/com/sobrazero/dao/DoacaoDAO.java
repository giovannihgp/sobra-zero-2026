package com.sobrazero.dao;

import com.sobrazero.model.Doacao;
import com.sobrazero.model.DoacaoNaoPerecivel;
import com.sobrazero.model.DoacaoPerecivel;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.StatusDoacao;
import com.sobrazero.util.ConexaoBD;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DoacaoDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO doacoes (estabelecimento_id, tipo_doacao, data_cadastro, data_validade, "
            + "exige_refrigeracao, status, lote, meses_estocagem) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_LISTAR_POR_ESTABELECIMENTO =
            "SELECT id, tipo_doacao, data_cadastro, data_validade, exige_refrigeracao, status, lote, meses_estocagem "
            + "FROM doacoes WHERE estabelecimento_id = ? ORDER BY data_cadastro DESC, id DESC";

    private final ItemDoacaoDAO itemDoacaoDAO = new ItemDoacaoDAO();

    // Uma sobrecarga por subclasse, como no UsuarioDAO: assim o DAO nao precisa de instanceof
    public void inserir(DoacaoPerecivel doacao) throws SQLException {
        inserirDoacao(doacao, "PERECIVEL", doacao.getDataValidade(), doacao.isExigeRefrigeracao(), null, null);
    }

    public void inserir(DoacaoNaoPerecivel doacao) throws SQLException {
        inserirDoacao(doacao, "NAO_PERECIVEL", null, false, doacao.getLote(), doacao.getMesesEstocagem());
    }

    // Devolve as doacoes do estabelecimento ja com os itens carregados
    public List<Doacao> listarPorEstabelecimento(Estabelecimento estabelecimento) throws SQLException {
        List<Doacao> doacoes = new ArrayList<>();
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_POR_ESTABELECIMENTO)) {
            comando.setInt(1, estabelecimento.getId());
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Doacao doacao = montarDoacao(resultado);
                    doacao.setEstabelecimento(estabelecimento);
                    // mesma conexao: evita abrir uma nova a cada doacao da lista
                    doacao.setItens(itemDoacaoDAO.listarPorDoacao(conexao, doacao.getId()));
                    doacoes.add(doacao);
                }
            }
        }
        return doacoes;
    }

    // Doacao e itens entram na mesma transacao: ou grava tudo, ou nao grava nada
    private void inserirDoacao(Doacao doacao, String tipo, LocalDate dataValidade,
                               boolean exigeRefrigeracao, String lote, Integer mesesEstocagem)
            throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao()) {
            conexao.setAutoCommit(false);
            try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, new String[] {"id"})) {
                comando.setInt(1, doacao.getEstabelecimento().getId());
                comando.setString(2, tipo);
                comando.setDate(3, Date.valueOf(doacao.getDataCadastro()));
                if (dataValidade == null) {
                    comando.setNull(4, Types.DATE);
                } else {
                    comando.setDate(4, Date.valueOf(dataValidade));
                }
                comando.setBoolean(5, exigeRefrigeracao);
                comando.setString(6, doacao.getStatus().name());
                comando.setString(7, lote);
                if (mesesEstocagem == null) {
                    comando.setNull(8, Types.INTEGER);
                } else {
                    comando.setInt(8, mesesEstocagem);
                }
                comando.executeUpdate();

                int doacaoId;
                try (ResultSet chaves = comando.getGeneratedKeys()) {
                    if (!chaves.next()) {
                        throw new SQLException("O banco nao devolveu o id da doacao.");
                    }
                    doacaoId = chaves.getInt(1);
                }

                itemDoacaoDAO.inserirItens(conexao, doacaoId, doacao.getItens());
                conexao.commit();
                doacao.setId(doacaoId);
            } catch (SQLException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    // Unico ponto que decide por tipo: a coluna tipo_doacao diz qual subclasse instanciar
    private Doacao montarDoacao(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        LocalDate dataCadastro = resultado.getDate("data_cadastro").toLocalDate();
        StatusDoacao status = StatusDoacao.valueOf(resultado.getString("status"));
        String tipo = resultado.getString("tipo_doacao");

        return switch (tipo) {
            case "PERECIVEL" -> {
                Date validade = resultado.getDate("data_validade");
                if (validade == null) {
                    throw new SQLException("Doacao perecivel " + id + " sem data_validade no banco.");
                }
                yield new DoacaoPerecivel(id, dataCadastro, status,
                        validade.toLocalDate(), resultado.getBoolean("exige_refrigeracao"));
            }
            case "NAO_PERECIVEL" ->
                    new DoacaoNaoPerecivel(id, dataCadastro, status,
                            resultado.getString("lote"), resultado.getInt("meses_estocagem"));
            default -> throw new SQLException("tipo_doacao desconhecido: " + tipo);
        };
    }
}
