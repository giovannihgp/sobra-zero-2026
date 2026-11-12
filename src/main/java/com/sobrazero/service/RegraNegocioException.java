package com.sobrazero.service;

// Erro que a view mostra direto ao usuario: a mensagem ja vem pronta para o console
public class RegraNegocioException extends Exception {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    public RegraNegocioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
