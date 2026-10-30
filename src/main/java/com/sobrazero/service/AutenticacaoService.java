package com.sobrazero.service;

import org.mindrot.jbcrypt.BCrypt;

public class AutenticacaoService {

    private static final int CUSTO_HASH = 10;

    private String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt(CUSTO_HASH));
    }

    private boolean senhaConfere(String senha, String hash) {
        return BCrypt.checkpw(senha, hash);
    }
}
