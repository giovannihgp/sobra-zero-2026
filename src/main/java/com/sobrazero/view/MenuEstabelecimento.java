package com.sobrazero.view;

import com.sobrazero.model.Coleta;
import com.sobrazero.model.Doacao;
import com.sobrazero.model.DoacaoNaoPerecivel;
import com.sobrazero.model.DoacaoPerecivel;
import com.sobrazero.model.Estabelecimento;
import com.sobrazero.model.ItemDoacao;
import com.sobrazero.model.Usuario;
import com.sobrazero.service.ColetaService;
import com.sobrazero.service.DoacaoService;
import com.sobrazero.service.RegraNegocioException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuEstabelecimento {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner entrada;
    private final DoacaoService servico = new DoacaoService();
    private final ColetaService coletaServico = new ColetaService();

    public MenuEstabelecimento(Scanner entrada) {
        this.entrada = entrada;
    }

    // Recebe Usuario para ter a mesma assinatura dos outros menus (o MenuPrincipal liga por perfil)
    public void exibir(Usuario usuarioLogado) {
        Estabelecimento estabelecimento = (Estabelecimento) usuarioLogado;
        boolean executando = true;
        while (executando) {
            System.out.println();
            System.out.println("=== ESTABELECIMENTO ===");
            System.out.println("1 - Cadastrar doação");
            System.out.println("2 - Listar minhas doações");
            System.out.println("3 - Editar doação");
            System.out.println("4 - Excluir doação");
            System.out.println("5 - Confirmar retirada");
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha uma opção: ");

            String opcao = entrada.nextLine().trim();
            switch (opcao) {
                case "1" -> cadastrarDoacao(estabelecimento);
                case "2" -> listarDoacoes(estabelecimento);
                case "3" -> editarDoacao(estabelecimento);
                case "4" -> excluirDoacao(estabelecimento);
                case "5" -> confirmarRetirada(estabelecimento);
                case "0" -> executando = false;
                default -> System.out.println("Opção inválida. Digite um número de 0 a 5.");
            }
        }
    }

    // RF05: lista as coletas agendadas deste estabelecimento e confirma a que foi retirada
    private void confirmarRetirada(Estabelecimento estabelecimento) {
        try {
            List<Coleta> coletas = coletaServico.listarAgendadas(estabelecimento.getId());
            if (coletas.isEmpty()) {
                System.out.println("Nenhuma coleta agendada no momento.");
                return;
            }
            System.out.println();
            for (int i = 0; i < coletas.size(); i++) {
                Coleta coleta = coletas.get(i);
                System.out.printf("[%d] Doação #%d | %s | retirada agendada para %s | %.2f kg%n",
                        i + 1, coleta.getDoacao().getId(), coleta.getInstituicao().getNome(),
                        coleta.getDataAgendada().format(FORMATO_DATA), coleta.getDoacao().calcularPesoTotal());
            }
            int escolha = EntradaConsole.lerInteiro(entrada, "Número da coleta retirada (0 para cancelar): ");
            if (escolha == 0) {
                System.out.println("Confirmação cancelada.");
                return;
            }
            if (escolha < 1 || escolha > coletas.size()) {
                System.out.println("Número inválido.");
                return;
            }
            Coleta escolhida = coletas.get(escolha - 1);
            coletaServico.confirmarRetirada(escolhida, estabelecimento);
            System.out.println("Retirada confirmada em " + escolhida.getDataRetirada().format(FORMATO_DATA_HORA) + ".");
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cadastrarDoacao(Estabelecimento estabelecimento) {
        System.out.println();
        System.out.println("=== NOVA DOAÇÃO ===");
        System.out.println("1 - Perecível");
        System.out.println("2 - Não perecível");
        String tipo = EntradaConsole.lerObrigatorio(entrada, "Tipo da doação: ");

        try {
            switch (tipo) {
                case "1" -> cadastrarPerecivel(estabelecimento);
                case "2" -> cadastrarNaoPerecivel(estabelecimento);
                default -> System.out.println("Tipo inválido. Digite 1 ou 2.");
            }
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cadastrarPerecivel(Estabelecimento estabelecimento) throws RegraNegocioException {
        var dataValidade = EntradaConsole.lerData(entrada, "Data de validade (dd/mm/aaaa): ");
        boolean refrigeracao = lerSimNao("Exige refrigeração? (s/n): ");
        List<ItemDoacao> itens = lerItens();
        var doacao = servico.cadastrarPerecivel(estabelecimento, dataValidade, refrigeracao, itens);
        System.out.println("Doação #" + doacao.getId() + " cadastrada com sucesso.");
    }

    private void cadastrarNaoPerecivel(Estabelecimento estabelecimento) throws RegraNegocioException {
        String lote = EntradaConsole.lerObrigatorio(entrada, "Lote: ");
        int meses = EntradaConsole.lerInteiro(entrada, "Meses de estocagem: ");
        List<ItemDoacao> itens = lerItens();
        var doacao = servico.cadastrarNaoPerecivel(estabelecimento, lote, meses, itens);
        System.out.println("Doação #" + doacao.getId() + " cadastrada com sucesso.");
    }

    // Pede itens ate o usuario dizer que acabou. A validacao em si e do service.
    private List<ItemDoacao> lerItens() {
        List<ItemDoacao> itens = new ArrayList<>();
        boolean outroItem = true;
        while (outroItem) {
            System.out.println();
            System.out.println("--- Item " + (itens.size() + 1) + " ---");
            String descricao = EntradaConsole.lerObrigatorio(entrada, "Descrição: ");
            double quantidade = lerDecimal("Quantidade: ");
            String unidade = EntradaConsole.lerObrigatorio(entrada, "Unidade (kg, g, un ou cx): ");
            itens.add(new ItemDoacao(descricao, quantidade, unidade));
            outroItem = lerSimNao("Adicionar outro item? (s/n): ");
        }
        return itens;
    }

    // Devolve true se havia doacoes para mostrar (editar e excluir usam isso para nao pedir id a toa)
    private boolean listarDoacoes(Estabelecimento estabelecimento) {
        try {
            List<Doacao> doacoes = servico.listarDoacoes(estabelecimento);
            if (doacoes.isEmpty()) {
                System.out.println("Você ainda não cadastrou nenhuma doação.");
                return false;
            }
            for (Doacao doacao : doacoes) {
                imprimirDoacao(doacao);
            }
            return true;
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void editarDoacao(Estabelecimento estabelecimento) {
        if (!listarDoacoes(estabelecimento)) {
            return;
        }
        System.out.println();
        int id = EntradaConsole.lerInteiro(entrada, "ID da doação que deseja editar: ");
        try {
            Doacao doacao = servico.buscarDoacao(estabelecimento, id);
            // Unico ponto da view que olha o tipo: os campos pedidos mudam de um tipo para o outro.
            // A regra de negocio continua no service, sem instanceof.
            if (doacao instanceof DoacaoPerecivel perecivel) {
                editarPerecivel(perecivel);
            } else if (doacao instanceof DoacaoNaoPerecivel naoPerecivel) {
                editarNaoPerecivel(naoPerecivel);
            }
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void editarPerecivel(DoacaoPerecivel doacao) throws RegraNegocioException {
        System.out.println();
        System.out.println("=== EDITAR DOAÇÃO #" + doacao.getId() + " (perecível) ===");
        System.out.println("Validade atual: " + doacao.getDataValidade().format(FORMATO_DATA));
        var dataValidade = EntradaConsole.lerData(entrada, "Nova data de validade (dd/mm/aaaa): ");
        boolean refrigeracao = lerSimNao("Exige refrigeração? (s/n): ");
        List<ItemDoacao> itens = lerItensDaEdicao(doacao);
        servico.editarPerecivel(doacao, dataValidade, refrigeracao, itens);
        System.out.println("Doação #" + doacao.getId() + " atualizada com sucesso.");
    }

    private void editarNaoPerecivel(DoacaoNaoPerecivel doacao) throws RegraNegocioException {
        System.out.println();
        System.out.println("=== EDITAR DOAÇÃO #" + doacao.getId() + " (não perecível) ===");
        System.out.println("Lote atual: " + doacao.getLote() + " | Meses de estocagem: " + doacao.getMesesEstocagem());
        String lote = EntradaConsole.lerObrigatorio(entrada, "Novo lote: ");
        int meses = EntradaConsole.lerInteiro(entrada, "Meses de estocagem: ");
        List<ItemDoacao> itens = lerItensDaEdicao(doacao);
        servico.editarNaoPerecivel(doacao, lote, meses, itens);
        System.out.println("Doação #" + doacao.getId() + " atualizada com sucesso.");
    }

    // Na edicao os itens sao todos substituidos; o usuario pode manter os atuais
    private List<ItemDoacao> lerItensDaEdicao(Doacao doacao) {
        if (lerSimNao("Manter os itens atuais? (s/n): ")) {
            return new ArrayList<>(doacao.getItens());
        }
        System.out.println("Informe os novos itens. Os atuais serão substituídos.");
        return lerItens();
    }

    private void excluirDoacao(Estabelecimento estabelecimento) {
        if (!listarDoacoes(estabelecimento)) {
            return;
        }
        System.out.println();
        int id = EntradaConsole.lerInteiro(entrada, "ID da doação que deseja excluir: ");
        if (!lerSimNao("Excluir a doação #" + id + "? Isso não pode ser desfeito. (s/n): ")) {
            System.out.println("Exclusão cancelada.");
            return;
        }
        try {
            servico.excluirDoacao(estabelecimento, id);
            System.out.println("Doação #" + id + " excluída com sucesso.");
        } catch (RegraNegocioException e) {
            System.out.println(e.getMessage());
        }
    }

    private void imprimirDoacao(Doacao doacao) {
        System.out.println();
        System.out.printf("Doação #%d | cadastrada em %s | %s | peso total: %.2f kg%n",
                doacao.getId(), doacao.getDataCadastro().format(FORMATO_DATA),
                doacao.getStatus(), doacao.calcularPesoTotal());
        for (ItemDoacao item : doacao.getItens()) {
            System.out.printf("  - %s: %.2f %s%n", item.getDescricao(), item.getQuantidade(), item.getUnidade());
        }
        // Polimorfismo: cada subclasse descreve o proprio manuseio (vazio ate o Gabriel implementar)
        String manuseio = doacao.descreverManuseio();
        if (!manuseio.isEmpty()) {
            System.out.println("  Manuseio: " + manuseio);
        }
    }

    // O EntradaConsole nao tem leitura de decimal nem de sim/nao, entao ficam aqui
    private double lerDecimal(String rotulo) {
        while (true) {
            String valor = EntradaConsole.lerObrigatorio(entrada, rotulo).replace(',', '.');
            try {
                return Double.parseDouble(valor);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número, como 2.5 ou 10.");
            }
        }
    }

    private boolean lerSimNao(String rotulo) {
        while (true) {
            String valor = EntradaConsole.lerObrigatorio(entrada, rotulo).toLowerCase();
            if (valor.equals("s") || valor.equals("sim")) {
                return true;
            }
            if (valor.equals("n") || valor.equals("nao") || valor.equals("não")) {
                return false;
            }
            System.out.println("Responda com s ou n.");
        }
    }
}
