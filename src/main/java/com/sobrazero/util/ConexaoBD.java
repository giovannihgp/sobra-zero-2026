package com.sobrazero.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexaoBD {

    private static final String ARQUIVO_CONFIG = "config.properties";

    private static final String URL;
    private static final String USUARIO;
    private static final String SENHA;

    static {
        Properties config = new Properties();
        try (InputStream entrada = ConexaoBD.class.getClassLoader().getResourceAsStream(ARQUIVO_CONFIG)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "Arquivo " + ARQUIVO_CONFIG + " nao encontrado em src/main/resources. "
                        + "Copie o config.properties.example e preencha com seus dados.");
            }
            config.load(entrada);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler " + ARQUIVO_CONFIG, e);
        }
        URL = config.getProperty("db.url");
        USUARIO = config.getProperty("db.user");
        SENHA = config.getProperty("db.password");
    }

    private ConexaoBD() {
    }

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    // Teste provisorio do Bloco 0: remover quando o MenuPrincipal existir
    public static void main(String[] args) {
        try (Connection conexao = getConexao()) {
            System.out.println("conectado: " + conexao.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.out.println("falha ao conectar: " + e.getMessage());
        }
    }
}
