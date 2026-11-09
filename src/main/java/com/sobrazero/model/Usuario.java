package com.sobrazero.model;

import org.mindrot.jbcrypt.BCrypt;

// Classe abstrata: nunca existe um "usuario generico", so Estabelecimento,
// Instituicao ou Administrador. Guarda o que e comum aos tres perfis.
public abstract class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senhaHash;

    protected Usuario() {
    }

    protected Usuario(int id, String nome, String email, String senhaHash) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
    }

    // Compara a senha digitada com o hash BCrypt guardado. A senha em texto
    // puro nunca e armazenada: so existe aqui, durante a comparacao.
    public boolean autenticar(String senha) {
        if (senha == null || senhaHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(senha, senhaHash);
        } catch (IllegalArgumentException e) {
            // hash em formato invalido no banco: trata como falha de login
            return false;
        }
    }

    // Cada subclasse devolve o seu perfil, no mesmo texto da coluna tipo_usuario
    public abstract String obterPerfil();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
}
