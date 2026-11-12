package com.sobrazero.view;

import com.sobrazero.model.Coleta;
import com.sobrazero.model.Doacao;
import com.sobrazero.model.Instituicao;
import com.sobrazero.model.ItemDoacao;
import com.sobrazero.service.ColetaException;
import com.sobrazero.service.ColetaService;
import com.sobrazero.service.PriorizacaoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuInstituicao {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final Instituicao instituicao;
    private final Scanner entrada;
    private final ColetaService coletaService = new ColetaService();
    private final PriorizacaoService priorizacaoService = new PriorizacaoService();

    public MenuInstituicao(Instituicao instituicao) {
        this(instituicao, new Scanner(System.in));
    }

    public MenuInstituicao(Instituicao instituicao, Scanner entrada) {
        this.instituicao = instituicao;
        this.entrada = entrada;
    }

    public void exibir() {
        expirarVencidas();

        int opcao;
        do {
            System.out.println();
            System.out.println("=== Painel da instituicao: " + instituicao.getNome() + " ===");
            System.out.println("1 - Ver doacoes disponiveis (por urgencia)");
            System.out.println("2 - Reservar doacao");
            System.out.println("0 - Voltar");
            System.out.print("Opcao: ");
            opcao = lerInteiro();

            switch (opcao) {
                case 1:
                    mostrarPainel();
                    break;
                case 2:
                    reservar();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opcao invalida.");
            }
        } while (opcao != 0);
    }

    private void expirarVencidas() {
        try {
            int expiradas = coletaService.expirarVencidas();
            if (expiradas > 0) {
                System.out.println(expiradas + " doacao(oes) vencida(s) marcada(s) como expirada(s).");
            }
        } catch (ColetaException e) {
            System.out.println(e.getMessage());
        }
    }

    private List<Doacao> carregarPainel() {
        try {
            return priorizacaoService.listarOrdenadas(coletaService.listarDisponiveis());
        } catch (ColetaException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    private void mostrarPainel() {
        List<Doacao> painel = carregarPainel();
        if (painel.isEmpty()) {
            System.out.println("Nenhuma doacao disponivel no momento.");
            return;
        }
        imprimirPainel(painel);
    }

    private void imprimirPainel(List<Doacao> painel) {
        for (int i = 0; i < painel.size(); i++) {
            Doacao doacao = painel.get(i);
            System.out.println();
            System.out.println("[" + (i + 1) + "] Doacao " + doacao.getId()
                    + " - " + nomeEstabelecimento(doacao)
                    + " | Prioridade: " + doacao.calcularPrioridade());
            System.out.println("    Itens: " + descreverItens(doacao));
            System.out.println("    Manuseio: " + doacao.descreverManuseio());
        }
    }

    private void reservar() {
        List<Doacao> painel = carregarPainel();
        if (painel.isEmpty()) {
            System.out.println("Nenhuma doacao disponivel no momento.");
            return;
        }
        imprimirPainel(painel);

        System.out.println();
        System.out.print("Numero da doacao a reservar (0 para cancelar): ");
        int escolha = lerInteiro();
        if (escolha == 0) {
            return;
        }
        if (escolha < 1 || escolha > painel.size()) {
            System.out.println("Numero invalido.");
            return;
        }

        System.out.print("Data prevista de retirada (dd/MM/aaaa): ");
        LocalDate dataRetirada;
        try {
            dataRetirada = LocalDate.parse(entrada.nextLine().trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            System.out.println("Data invalida. Use o formato dd/MM/aaaa.");
            return;
        }

        try {
            Coleta coleta = coletaService.reservar(painel.get(escolha - 1), instituicao, dataRetirada);
            System.out.println("Reserva confirmada. Retirada agendada para "
                    + coleta.getDataAgendada().format(FORMATO_DATA) + ".");
        } catch (ColetaException e) {
            System.out.println(e.getMessage());
        }
    }

    private String nomeEstabelecimento(Doacao doacao) {
        if (doacao.getEstabelecimento() == null) {
            return "estabelecimento nao informado";
        }
        return doacao.getEstabelecimento().getNome();
    }

    private String descreverItens(Doacao doacao) {
        StringBuilder texto = new StringBuilder();
        for (ItemDoacao item : doacao.getItens()) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(item.getDescricao())
                    .append(" (")
                    .append(formatarQuantidade(item.getQuantidade()))
                    .append(" ")
                    .append(item.getUnidade())
                    .append(")");
        }
        return texto.length() == 0 ? "sem itens" : texto.toString();
    }

    private String formatarQuantidade(double quantidade) {
        if (quantidade == Math.floor(quantidade)) {
            return String.valueOf((long) quantidade);
        }
        return String.valueOf(quantidade);
    }

    private int lerInteiro() {
        try {
            return Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
