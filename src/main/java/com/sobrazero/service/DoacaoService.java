package com.sobrazero.service;

import com.sobrazero.dao.DoacaoDAO;
import com.sobrazero.model.Doacao;
import com.sobrazero.model.DoacaoNaoPerecivel;
import com.sobrazero.model.DoacaoPerecivel;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.ItemDoacao;
import com.sobrazero.model.StatusDoacao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class DoacaoService {

    // Unidades aceitas. So kg e g entram no peso total (ver Doacao.calcularPesoTotal)
    private static final Set<String> UNIDADES_VALIDAS = Set.of("kg", "g", "un", "cx");

    // Limites das colunas do schema.sql
    private static final int TAMANHO_MAXIMO_DESCRICAO = 120;
    private static final int TAMANHO_MAXIMO_LOTE = 30;
    private static final double QUANTIDADE_MAXIMA = 99_999_999.99;

    // Codigos de erro (SQLState) do PostgreSQL
    private static final String VALOR_MUITO_LONGO = "22001";
    private static final String VIOLACAO_CHAVE_ESTRANGEIRA = "23503";

    private static final String MENSAGEM_NAO_EDITAVEL =
            "Esta doação não está mais disponível (pode ter sido reservada). Atualize a lista e tente de novo.";

    private final DoacaoDAO doacaoDAO = new DoacaoDAO();

    public DoacaoPerecivel cadastrarPerecivel(Estabelecimento estabelecimento, LocalDate dataValidade,
                                              boolean exigeRefrigeracao, List<ItemDoacao> itens)
            throws RegraNegocioException {
        validarPerecivel(dataValidade);
        validarItens(itens);

        DoacaoPerecivel doacao = new DoacaoPerecivel(dataValidade, exigeRefrigeracao);
        montar(doacao, estabelecimento, itens);
        try {
            doacaoDAO.inserir(doacao);
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return doacao;
    }

    public DoacaoNaoPerecivel cadastrarNaoPerecivel(Estabelecimento estabelecimento, String lote,
                                                    int mesesEstocagem, List<ItemDoacao> itens)
            throws RegraNegocioException {
        validarNaoPerecivel(lote, mesesEstocagem);
        validarItens(itens);

        DoacaoNaoPerecivel doacao = new DoacaoNaoPerecivel(lote.trim(), mesesEstocagem);
        montar(doacao, estabelecimento, itens);
        try {
            doacaoDAO.inserir(doacao);
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
        return doacao;
    }

    // Trabalha com List<Doacao>: o service nao precisa saber o tipo de cada uma
    public List<Doacao> listarDoacoes(Estabelecimento estabelecimento) throws RegraNegocioException {
        exigirEstabelecimento(estabelecimento);
        try {
            return doacaoDAO.listarPorEstabelecimento(estabelecimento);
        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    // Devolve a doacao ja com os itens. Se nao existir ou for de outro estabelecimento, a mensagem e a mesma
    public Doacao buscarDoacao(Estabelecimento estabelecimento, int doacaoId) throws RegraNegocioException {
        exigirEstabelecimento(estabelecimento);
        try {
            Doacao doacao = doacaoDAO.buscarPorId(doacaoId, estabelecimento);
            if (doacao == null) {
                throw new RegraNegocioException("Doação não encontrada.");
            }
            return doacao;
        } catch (SQLException e) {
            throw erroDeBanco(e);
        }
    }

    // A doacao vem de buscarDoacao, entao o dono ja esta nela. Os itens antigos sao substituidos.
    public void editarPerecivel(DoacaoPerecivel doacao, LocalDate dataValidade,
                                boolean exigeRefrigeracao, List<ItemDoacao> itens)
            throws RegraNegocioException {
        exigirEditavel(doacao);
        validarPerecivel(dataValidade);
        validarItens(itens);

        doacao.setDataValidade(dataValidade);
        doacao.setExigeRefrigeracao(exigeRefrigeracao);
        doacao.setItens(itens);
        try {
            if (!doacaoDAO.atualizar(doacao)) {
                throw new RegraNegocioException(MENSAGEM_NAO_EDITAVEL);
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void editarNaoPerecivel(DoacaoNaoPerecivel doacao, String lote,
                                   int mesesEstocagem, List<ItemDoacao> itens)
            throws RegraNegocioException {
        exigirEditavel(doacao);
        validarNaoPerecivel(lote, mesesEstocagem);
        validarItens(itens);

        doacao.setLote(lote.trim());
        doacao.setMesesEstocagem(mesesEstocagem);
        doacao.setItens(itens);
        try {
            if (!doacaoDAO.atualizar(doacao)) {
                throw new RegraNegocioException(MENSAGEM_NAO_EDITAVEL);
            }
        } catch (SQLException e) {
            throw traduzirErro(e);
        }
    }

    public void excluirDoacao(Estabelecimento estabelecimento, int doacaoId) throws RegraNegocioException {
        Doacao doacao = buscarDoacao(estabelecimento, doacaoId);
        exigirEditavel(doacao);
        try {
            if (!doacaoDAO.excluir(doacaoId, estabelecimento)) {
                throw new RegraNegocioException(MENSAGEM_NAO_EDITAVEL);
            }
        } catch (SQLException e) {
            // Chave estrangeira aqui significa que existe uma coleta apontando para a doacao
            if (VIOLACAO_CHAVE_ESTRANGEIRA.equals(e.getSQLState())) {
                throw new RegraNegocioException("Esta doação já foi reservada e não pode ser excluída.", e);
            }
            throw erroDeBanco(e);
        }
    }

    private void montar(Doacao doacao, Estabelecimento estabelecimento, List<ItemDoacao> itens)
            throws RegraNegocioException {
        exigirEstabelecimento(estabelecimento);
        doacao.setEstabelecimento(estabelecimento);
        for (ItemDoacao item : itens) {
            doacao.adicionarItem(item);
        }
    }

    // So doacao DISPONIVEL pode ser editada ou excluida; depois de reservada, o fluxo e da coleta
    private void exigirEditavel(Doacao doacao) throws RegraNegocioException {
        if (doacao.getStatus() != StatusDoacao.DISPONIVEL) {
            throw new RegraNegocioException("Só é possível alterar doações disponíveis. Esta doação está "
                    + doacao.getStatus() + ".");
        }
    }

    private void validarPerecivel(LocalDate dataValidade) throws RegraNegocioException {
        if (dataValidade == null) {
            throw new RegraNegocioException("Doação perecível exige data de validade.");
        }
        if (dataValidade.isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de validade não pode estar no passado.");
        }
    }

    private void validarNaoPerecivel(String lote, int mesesEstocagem) throws RegraNegocioException {
        if (lote == null || lote.isBlank()) {
            throw new RegraNegocioException("Informe o lote da doação.");
        }
        if (lote.trim().length() > TAMANHO_MAXIMO_LOTE) {
            throw new RegraNegocioException("O lote deve ter no máximo " + TAMANHO_MAXIMO_LOTE + " caracteres.");
        }
        if (mesesEstocagem < 0) {
            throw new RegraNegocioException("Os meses de estocagem não podem ser negativos.");
        }
    }

    private void exigirEstabelecimento(Estabelecimento estabelecimento) throws RegraNegocioException {
        if (estabelecimento == null || estabelecimento.getId() <= 0) {
            throw new RegraNegocioException("Estabelecimento inválido.");
        }
    }

    // Valida e padroniza (trim e minusculas) os itens antes de qualquer acesso ao banco
    private void validarItens(List<ItemDoacao> itens) throws RegraNegocioException {
        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("A doação precisa ter pelo menos um item.");
        }
        for (ItemDoacao item : itens) {
            String descricao = item.getDescricao() == null ? "" : item.getDescricao().trim();
            if (descricao.isEmpty()) {
                throw new RegraNegocioException("Todo item precisa de uma descrição.");
            }
            if (descricao.length() > TAMANHO_MAXIMO_DESCRICAO) {
                throw new RegraNegocioException(
                        "A descrição do item deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres.");
            }
            // !(x > 0) em vez de x <= 0: tambem recusa NaN, que o Double.parseDouble aceita
            if (!(item.getQuantidade() > 0)) {
                throw new RegraNegocioException("A quantidade do item \"" + descricao + "\" deve ser maior que zero.");
            }
            if (item.getQuantidade() > QUANTIDADE_MAXIMA) {
                throw new RegraNegocioException("A quantidade do item \"" + descricao + "\" é grande demais.");
            }
            String unidade = item.getUnidade() == null ? "" : item.getUnidade().trim().toLowerCase();
            if (!UNIDADES_VALIDAS.contains(unidade)) {
                throw new RegraNegocioException("Unidade inválida para o item \"" + descricao
                        + "\". Use kg, g, un ou cx.");
            }
            item.setDescricao(descricao);
            item.setUnidade(unidade);
        }
    }

    private RegraNegocioException traduzirErro(SQLException e) {
        if (VALOR_MUITO_LONGO.equals(e.getSQLState())) {
            return new RegraNegocioException("Algum campo é maior do que o permitido. Revise os dados.", e);
        }
        if (VIOLACAO_CHAVE_ESTRANGEIRA.equals(e.getSQLState())) {
            return new RegraNegocioException("Estabelecimento não encontrado no banco de dados.", e);
        }
        return erroDeBanco(e);
    }

    private RegraNegocioException erroDeBanco(SQLException e) {
        return new RegraNegocioException(
                "Não foi possível acessar o banco de dados. Verifique se o PostgreSQL está rodando.", e);
    }
}
