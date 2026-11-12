package com.sobrazero.service;

import com.sobrazero.dao.UsuarioDAO;
import com.sobrazero.model.Administrador;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.Instituicao;
import com.sobrazero.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class AutenticacaoService {

    private static final int CUSTO_HASH = 10;
    private static final int TAMANHO_MINIMO_SENHA = 8;
    private static final String EMAIL_VALIDO = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";

    // Codigos de erro (SQLState) do PostgreSQL
    private static final String VIOLACAO_UNICIDADE = "23505";
    private static final String VALOR_MUITO_LONGO = "22001";
    private static final String VIOLACAO_CHAVE_ESTRANGEIRA = "23503";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario login(String email, String senha) throws RegraNegocioException {
        Usuario usuario;
        try {
            usuario = usuarioDAO.buscarPorEmail(normalizarEmail(email));
        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
        // Mesma mensagem nos dois casos para nao revelar quais e-mails existem
        if (usuario == null || !usuario.autenticar(senha)) {
            throw new RegraNegocioException("E-mail ou senha inválidos.");
        }
        return usuario;
    }

    public Estabelecimento cadastrarEstabelecimento(String nome, String email, String senha,
                                                    String cnpj, String endereco) throws RegraNegocioException {
        validarCredenciais(email, senha);
        Estabelecimento estabelecimento =
                new Estabelecimento(0, nome, normalizarEmail(email), gerarHash(senha), cnpj, endereco);
        try {
            usuarioDAO.inserir(estabelecimento);
        } catch (SQLException e) {
            throw traduzirErroDeCadastro(e);
        }
        return estabelecimento;
    }

    public Instituicao cadastrarInstituicao(String nome, String email, String senha,
                                            String cnpj, double capacidadeKg) throws RegraNegocioException {
        validarCredenciais(email, senha);
        Instituicao instituicao =
                new Instituicao(0, nome, normalizarEmail(email), gerarHash(senha), cnpj, capacidadeKg);
        try {
            usuarioDAO.inserir(instituicao);
        } catch (SQLException e) {
            throw traduzirErroDeCadastro(e);
        }
        return instituicao;
    }

    public Administrador cadastrarAdministrador(String nome, String email, String senha,
                                                String matricula) throws RegraNegocioException {
        validarCredenciais(email, senha);
        Administrador administrador =
                new Administrador(0, nome, normalizarEmail(email), gerarHash(senha), matricula);
        try {
            usuarioDAO.inserir(administrador);
        } catch (SQLException e) {
            throw traduzirErroDeCadastro(e);
        }
        return administrador;
    }

    public List<Usuario> listarUsuarios() throws RegraNegocioException {
        try {
            return usuarioDAO.listarTodos();
        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    public Usuario buscarUsuario(int id) throws RegraNegocioException {
        Usuario usuario;
        try {
            usuario = usuarioDAO.buscarPorId(id);
        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
        if (usuario == null) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }
        return usuario;
    }

    public void atualizarDadosBasicos(Usuario usuario, String novoNome, String novoEmail)
            throws RegraNegocioException {
        validarEmail(novoEmail);
        usuario.setNome(novoNome);
        usuario.setEmail(normalizarEmail(novoEmail));
        try {
            usuarioDAO.atualizarDadosBasicos(usuario);
        } catch (SQLException e) {
            throw traduzirErroDeCadastro(e);
        }
    }

    public void excluirUsuario(int id, Usuario solicitante) throws RegraNegocioException {
        if (id == solicitante.getId()) {
            throw new RegraNegocioException("Você não pode excluir a própria conta.");
        }
        boolean excluiu;
        try {
            excluiu = usuarioDAO.excluir(id);
        } catch (SQLException e) {
            if (VIOLACAO_CHAVE_ESTRANGEIRA.equals(e.getSQLState())) {
                throw new RegraNegocioException(
                        "Este usuário tem doações ou coletas registradas e não pode ser excluído.", e);
            }
            throw erroDeBanco(e);
        }
        if (!excluiu) {
            throw new RegraNegocioException("Usuário não encontrado.");
        }
    }

    private void validarCredenciais(String email, String senha) throws RegraNegocioException {
        validarEmail(email);
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException("A senha precisa ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    private void validarEmail(String email) throws RegraNegocioException {
        if (!normalizarEmail(email).matches(EMAIL_VALIDO)) {
            throw new RegraNegocioException("E-mail inválido.");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO_HASH));
    }

    private RegraNegocioException traduzirErroDeCadastro(SQLException e) {
        return switch (String.valueOf(e.getSQLState())) {
            case VIOLACAO_UNICIDADE -> new RegraNegocioException("Já existe um usuário com este e-mail.", e);
            case VALOR_MUITO_LONGO -> new RegraNegocioException("Algum campo passou do tamanho permitido.", e);
            default -> erroDeBanco(e);
        };
    }

    private RegraNegocioException erroDeBanco(SQLException e) {
        return new RegraNegocioException(
                "Não foi possível acessar o banco de dados. Verifique se o PostgreSQL está rodando.", e);
    }
}
