package com.sobrazero.service;

import com.sobrazero.model.Doacao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorizacaoService {

    public List<Doacao> listarOrdenadas(List<Doacao> doacoes) {
        List<Doacao> ordenadas = new ArrayList<>(doacoes);
        ordenadas.sort(Comparator.comparingInt(Doacao::calcularPrioridade).reversed());
        return ordenadas;
    }
}
