package br.ufc.estoque.controller;

import br.ufc.estoque.pubsub.Evento;
import br.ufc.estoque.pubsub.EventBroker;
import br.ufc.estoque.pubsub.Topico;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Expõe o Broker Pub-Sub via HTTP.
 *
 * GET /api/eventos/topicos
 *     → lista os tópicos disponíveis
 *
 * GET /api/eventos/subscribe/{topico}?id={subscriberId}
 *     → abre stream SSE; entrega eventos pendentes (replay) e fica aberto
 *
 * GET /api/eventos/historico/{topico}
 *     → retorna o histórico JSON de um tópico (para debug)
 *
 * POST /api/eventos/publicar/{topico}?origem={origem}&payload={payload}
 *     → publica manualmente um evento (útil para testes)
 */
@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventBroker broker;

    public EventoController(EventBroker broker) {
        this.broker = broker;
    }

    @GetMapping("/topicos")
    public Topico[] topicos() {
        return Topico.values();
    }

    @GetMapping(value = "/subscribe/{topico}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable String topico,
            @RequestParam(defaultValue = "anonimo") String id) {
        return broker.assinar(Topico.fromString(topico), id);
    }

    @GetMapping("/historico/{topico}")
    public List<Evento> historico(@PathVariable String topico) {
        return broker.historico(Topico.fromString(topico));
    }

    @PostMapping("/publicar/{topico}")
    public void publicar(
            @PathVariable String topico,
            @RequestParam String origem,
            @RequestParam String payload) {
        broker.publicar(Topico.fromString(topico), origem, payload);
    }
}
