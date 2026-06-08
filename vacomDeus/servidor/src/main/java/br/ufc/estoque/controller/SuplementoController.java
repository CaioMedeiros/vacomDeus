package br.ufc.estoque.controller;

import br.ufc.estoque.model.Suplemento;
import br.ufc.estoque.service.SuplementoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Objeto distribuído 1 exposto via REST.
 * Gerencia suplementos: criação, busca, listagem e remoção.
 *
 * Endpoints:
 *   GET    /api/suplementos           → lista todos
 *   GET    /api/suplementos/{nome}    → busca por nome
 *   POST   /api/suplementos           → adiciona novo suplemento
 *   DELETE /api/suplementos/{nome}    → remove suplemento
 */
@RestController
@RequestMapping("/api/suplementos")
public class SuplementoController {

    private final SuplementoService service;

    public SuplementoController(SuplementoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Suplemento> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/{nome}")
    public Suplemento buscar(@PathVariable String nome) {
        return service.buscar(nome);
    }

    @PostMapping
    public ResponseEntity<Suplemento> adicionar(@RequestBody Suplemento suplemento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionar(suplemento));
    }

    @DeleteMapping("/{nome}")
    public ResponseEntity<Map<String, String>> remover(@PathVariable String nome) {
        service.remover(nome);
        return ResponseEntity.ok(Map.of("mensagem", "Removido: " + nome));
    }
}
