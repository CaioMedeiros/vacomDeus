package br.ufc.estoque.service;

import br.ufc.estoque.model.Lote;
import br.ufc.estoque.model.Suplemento;
import br.ufc.estoque.pubsub.EventBroker;
import br.ufc.estoque.pubsub.Topico;
import br.ufc.estoque.repository.EstoqueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Objeto distribuído 3 — serviço de lotes e relatórios de validade.
 * Publica alertas de vencimento automaticamente ao detectar itens próximos.
 */
@Service
public class LoteService {

    private final EstoqueRepository repository;
    private final EventBroker        broker;

    public LoteService(EstoqueRepository repository, EventBroker broker) {
        this.repository = repository;
        this.broker     = broker;
    }

    public Suplemento adicionarLote(String nomeSuplemento, Lote lote) {
        Suplemento s = repository.findByNome(nomeSuplemento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Suplemento não encontrado: " + nomeSuplemento));
        s.adicionarLote(lote);
        broker.publicar(Topico.LOTE_ADICIONADO, "LoteService",
                "Lote " + lote.getCodigo() + " adicionado a " + nomeSuplemento
                + " | Vence: " + lote.getDataVencimento());
        return s;
    }

    public List<Lote> listarLotes(String nomeSuplemento) {
        return repository.findByNome(nomeSuplemento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Suplemento não encontrado: " + nomeSuplemento))
                .getLotes();
    }

    public List<Suplemento> listarVencidos() {
        return repository.findAll().stream()
                .filter(s -> !s.estaValido())
                .collect(Collectors.toList());
    }

    public List<Suplemento> listarProximosAoVencer(int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        List<Suplemento> result = repository.findAll().stream()
                .filter(s -> {
                    LocalDate v = s.getDataVencimento();
                    return v != null && !v.isBefore(LocalDate.now()) && !v.isAfter(limite);
                })
                .collect(Collectors.toList());

        // publica alerta para cada item encontrado
        result.forEach(s -> broker.publicar(Topico.ALERTA_VENCIMENTO, "LoteService",
                "ALERTA: " + s.getNome() + " vence em " + s.getDiasParaVencer() + " dias"));

        return result;
    }
}
