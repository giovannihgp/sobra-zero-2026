package com.sobrazero.dao;

import com.sobrazero.model.Administrador;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.Instituicao;
import com.sobrazero.model.Usuario;
import com.sobrazero.util.ConexaoBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO usuarios (nome, email, senha_hash, tipo_usuario, documento, endereco, capacidade_kg) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ATUALIZAR =
            "UPDATE usuarios SET nome = ?, email = ?, senha_hash = ?, documento = ?, endereco = ?, capacidade_kg = ? "
            + "WHERE id = ?";

    private static final String SQL_SELECIONAR =
            "SELECT id, nome, email, senha_hash, tipo_usuario, documento, endereco, capacidade_kg FROM usuarios";

    public void inserir(Estabelecimento estabelecimento) throws SQLException {
        inserirUsuario(estabelecimento, estabelecimento.getCnpj(), estabelecimento.getEndereco(), null);
    }

    public void inserir(Instituicao instituicao) throws SQLException {
        inserirUsuario(instituicao, instituicao.getCnpj(), null, instituicao.getCapacidadeKg());
    }

    public void inserir(Administrador administrador) throws SQLException {
        inserirUsuario(administrador, administrador.getMatricula(), null, null);
    }

    public boolean atualizar(Estabelecimento estabelecimento) throws SQLException {
        return atualizarUsuario(estabelecimento, estabelecimento.getCnpj(), estabelecimento.getEndereco(), null);
    }

    public boolean atualizar(Instituicao instituicao) throws SQLException {
        return atualizarUsuario(instituicao, instituicao.getCnpj(), null, instituicao.getCapacidadeKg());
    }

    public boolean atualizar(Administrador administrador) throws SQLException {
        return atualizarUsuario(administrador, administrador.getMatricula(), null, null);
    }

    // Atualiza so as colunas comuns a todos os perfis, por isso aceita qualquer Usuario
    public boolean atualizarDadosBasicos(Usuario usuario) throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(
                     "UPDATE usuarios SET nome = ?, email = ? WHERE id = ?")) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setInt(3, usuario.getId());
            return comando.executeUpdate() > 0;
        }
    }

    // Devolve null quando nao existe usuario com esse id
    public Usuario buscarPorId(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_SELECIONAR + " WHERE id = ?")) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarUsuario(resultado) : null;
            }
        }
    }

    // Devolve null quando nao existe usuario com esse e-mail
    public Usuario buscarPorEmail(String email) throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_SELECIONAR + " WHERE email = ?")) {
            comando.setString(1, email);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? montarUsuario(resultado) : null;
            }
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_SELECIONAR + " ORDER BY nome");
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(montarUsuario(resultado));
            }
        }
        return usuarios;
    }

    public boolean excluir(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        }
    }

    private void inserirUsuario(Usuario usuario, String documento, String endereco, Double capacidadeKg)
            throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_INSERIR, new String[] {"id"})) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenhaHash());
            comando.setString(4, usuario.obterPerfil());
            comando.setString(5, documento);
            comando.setString(6, endereco);
            preencherCapacidade(comando, 7, capacidadeKg);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getInt(1));
                }
            }
        }
    }

    private boolean atualizarUsuario(Usuario usuario, String documento, String endereco, Double capacidadeKg)
            throws SQLException {
        try (Connection conexao = ConexaoBD.getConexao();
             PreparedStatement comando = conexao.prepareStatement(SQL_ATUALIZAR)) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenhaHash());
            comando.setString(4, documento);
            comando.setString(5, endereco);
            preencherCapacidade(comando, 6, capacidadeKg);
            comando.setInt(7, usuario.getId());
            return comando.executeUpdate() > 0;
        }
    }

    private void preencherCapacidade(PreparedStatement comando, int posicao, Double capacidadeKg)
            throws SQLException {
        if (capacidadeKg == null) {
            comando.setNull(posicao, Types.NUMERIC);
        } else {
            comando.setDouble(posicao, capacidadeKg);
        }
    }

    // Unico ponto do sistema que decide por tipo: a coluna tipo_usuario diz qual subclasse instanciar
    private Usuario montarUsuario(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        String nome = resultado.getString("nome");
        String email = resultado.getString("email");
        String senhaHash = resultado.getString("senha_hash");
        String documento = resultado.getString("documento");
        String tipo = resultado.getString("tipo_usuario");

        return switch (tipo) {
            case "ESTABELECIMENTO" ->
                    new Estabelecimento(id, nome, email, senhaHash, documento, resultado.getString("endereco"));
            case "INSTITUICAO" ->
                    new Instituicao(id, nome, email, senhaHash, documento, resultado.getDouble("capacidade_kg"));
            case "ADMINISTRADOR" ->
                    new Administrador(id, nome, email, senhaHash, documento);
            default -> throw new SQLException("tipo_usuario desconhecido: " + tipo);
        };
    }
}
