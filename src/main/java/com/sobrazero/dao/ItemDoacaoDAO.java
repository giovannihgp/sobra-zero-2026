package com.sobrazero.dao;

import com.sobrazero.model.ItemDoacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Itens so existem dentro de uma doacao (composicao). Por isso os metodos deste DAO
// recebem a Connection de quem chama: o DoacaoDAO abre a conexao e controla a transacao.
public class ItemDoacaoDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO itens_doacao (doacao_id, descricao, quantidade, unidade_medida) VALUES (?, ?, ?, ?)";

    private static final String SQL_LISTAR_POR_DOACAO =
            "SELECT id, descricao, quantidade, unidade_medida FROM itens_doacao WHERE doacao_id = ? ORDER BY id";

    // Nao faz commit nem fecha a conexao: isso e responsabilidade do DoacaoDAO
    public void inserirItens(Connection conexao, int doacaoId, List<ItemDoacao> itens) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, new String[] {"id"})) {
            for (ItemDoacao item : itens) {
                comando.setInt(1, doacaoId);
                comando.setString(2, item.getDescricao());
                comando.setDouble(3, item.getQuantidade());
                comando.setString(4, item.getUnidade());
                comando.executeUpdate();

                try (ResultSet chaves = comando.getGeneratedKeys()) {
                    if (chaves.next()) {
                        item.setId(chaves.getInt(1));
                    }
                }
            }
        }
    }

    public List<ItemDoacao> listarPorDoacao(Connection conexao, int doacaoId) throws SQLException {
        List<ItemDoacao> itens = new ArrayList<>();
        try (PreparedStatement comando = conexao.prepareStatement(SQL_LISTAR_POR_DOACAO)) {
            comando.setInt(1, doacaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    itens.add(new ItemDoacao(
                            resultado.getInt("id"),
                            resultado.getString("descricao"),
                            resultado.getDouble("quantidade"),
                            resultado.getString("unidade_medida")));
                }
            }
        }
        return itens;
    }
}
