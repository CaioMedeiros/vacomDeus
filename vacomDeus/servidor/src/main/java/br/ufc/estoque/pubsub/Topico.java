package br.ufc.estoque.pubsub;

public enum Topico {
    SUPLEMENTO_ADICIONADO,
    SUPLEMENTO_REMOVIDO,
    LOTE_ADICIONADO,
    ALERTA_VENCIMENTO,
    ESTOQUE_CONSULTADO;

    public static Topico fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
