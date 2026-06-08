package br.ufc.estoque.service;

import br.ufc.estoque.model.Suplemento;
import br.ufc.estoque.pubsub.EventBroker;
import br.ufc.estoque.pubsub.Topico;
import br.ufc.estoque.repository.EstoqueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Objeto distribuído 2 — serviço de suplementos.
 * A cada operação relevante publica um evento no EventBroker
 * sem conhecer quem está ouvindo (desacoplamento espacial).
 */
@Service
public class SuplementoService {

    private final EstoqueRepository repository;
    private final EventBroker        broker;

    public SuplementoService(EstoqueRepository repository, EventBroker broker) {
        this.repository = repository;
        this.broker     = broker;
    }

    public List<Suplemento> listarTodos() {
        broker.publicar(Topico.ESTOQUE_CONSULTADO, "SuplementoService",
                "Listagem completa do estoque");
        return repository.findAll();
    }

    public Suplemento buscar(String nome) {
        return repository.findByNome(nome)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Suplemento não encontrado: " + nome));
    }

    public Suplemento adicionar(Suplemento s) {
        if (repository.existsByNome(s.getNome()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Suplemento já existe: " + s.getNome());
        repository.save(s);
        broker.publicar(Topico.SUPLEMENTO_ADICIONADO, "SuplementoService",
                "Suplemento adicionado: " + s.getNome() + " [" + s.getTipo() + "]");
        return s;
    }

    public void remover(String nome) {
        if (!repository.deleteByNome(nome))
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Suplemento não encontrado: " + nome);
        broker.publicar(Topico.SUPLEMENTO_REMOVIDO, "SuplementoService",
                "Suplemento removido: " + nome);
    }
}
