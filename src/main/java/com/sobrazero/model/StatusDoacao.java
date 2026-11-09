package com.sobrazero.model;

// Ciclo de vida da doacao. Espelha o CHECK da coluna doacoes.status no schema.sql
public enum StatusDoacao {
    DISPONIVEL,
    AGENDADA,
    CONCLUIDA,
    EXPIRADA
}
