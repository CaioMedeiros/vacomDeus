package br.ufc.estoque.pubsub;

import java.time.LocalDateTime;

public record Evento(
        Topico        topico,
        String        origem,
        String        payload,
        LocalDateTime timestamp
) {
    public Evento(Topico topico, String origem, String payload) {
        this(topico, origem, payload, LocalDateTime.now());
    }

    /** Formata o evento como linha SSE: "data: <json>\n\n" */
    public String toSseLine() {
        String json = String.format(
                "{\"topico\":\"%s\",\"origem\":\"%s\",\"payload\":\"%s\",\"timestamp\":\"%s\"}",
                topico, origem, payload.replace("\"", "\\\""), timestamp);
        return "data: " + json + "\n\n";
    }
}
