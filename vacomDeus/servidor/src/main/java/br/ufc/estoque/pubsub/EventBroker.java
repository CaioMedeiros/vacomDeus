package br.ufc.estoque.pubsub;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Broker Pub-Sub via Server-Sent Events (SSE).
 *
 * Desacoplamento espacial:  o publisher (SuplementoService/LoteService)
 *   chama apenas publicar(topico, payload) — não conhece IP, porta
 *   nem identidade de nenhum subscriber.
 *
 * Desacoplamento temporal:  eventos gerados enquanto não há subscriber
 *   são armazenados no histórico por tópico. Ao conectar, o subscriber
 *   recebe os eventos perdidos via replay automático.
 */
@Component
public class EventBroker {

    // tópico → lista de emitters (conexões SSE ativas)
    private final Map<Topico, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // histórico persistente por tópico (desacoplamento temporal)
    private final Map<Topico, Queue<Evento>> historico = new ConcurrentHashMap<>();

    public EventBroker() {
        for (Topico t : Topico.values()) {
            emitters.put(t, new CopyOnWriteArrayList<>());
            historico.put(t, new LinkedBlockingQueue<>());
        }
    }

    /**
     * Subscriber conecta e recebe um SseEmitter.
     * Eventos pendentes no histórico são entregues imediatamente (replay).
     */
    public SseEmitter assinar(Topico topico, String subscriberId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // replay dos eventos acumulados (desacoplamento temporal)
        Queue<Evento> pendentes = historico.get(topico);
        for (Evento e : pendentes) {
            try {
                emitter.send(SseEmitter.event()
                        .name("evento")
                        .data(e.toSseLine()));
            } catch (IOException ex) {
                break;
            }
        }

        emitters.get(topico).add(emitter);

        // remove o emitter ao encerrar ou dar erro
        emitter.onCompletion(() -> emitters.get(topico).remove(emitter));
        emitter.onTimeout(()    -> emitters.get(topico).remove(emitter));
        emitter.onError(e       -> emitters.get(topico).remove(emitter));

        return emitter;
    }

    /**
     * Publisher publica um evento em um tópico.
     * Entrega para todos os subscribers ativos e persiste no histórico.
     */
    public void publicar(Topico topico, String origem, String payload) {
        Evento evento = new Evento(topico, origem, payload);

        // persiste no histórico para futuros subscribers (desacoplamento temporal)
        historico.get(topico).add(evento);

        // entrega para subscribers ativos
        List<SseEmitter> lista = emitters.get(topico);
        List<SseEmitter> mortos = new ArrayList<>();

        for (SseEmitter emitter : lista) {
            try {
                emitter.send(SseEmitter.event()
                        .name("evento")
                        .data(evento.toSseLine()));
            } catch (IOException e) {
                mortos.add(emitter);
            }
        }
        lista.removeAll(mortos);
    }

    /** Retorna o histórico completo de um tópico. */
    public List<Evento> historico(Topico topico) {
        return new ArrayList<>(historico.get(topico));
    }
}
