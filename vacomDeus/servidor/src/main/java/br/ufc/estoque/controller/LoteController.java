package br.ufc.estoque.controller;

import br.ufc.estoque.model.Lote;
import br.ufc.estoque.model.Suplemento;
import br.ufc.estoque.service.LoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Objeto distribuído 2 exposto via REST.
 * Gerencia lotes de suplementos.
 *
 * Endpoints:
 *   GET  /api/suplementos/{nome}/lotes       → lista lotes do suplemento
 *   POST /api/suplementos/{nome}/lotes       → adiciona lote ao suplemento
 */
@RestController
@RequestMapping("/api/suplementos/{nome}/lotes")
public class LoteController {

    private final LoteService service;

    public LoteController(LoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Lote> listarLotes(@PathVariable String nome) {
        return service.listarLotes(nome);
    }

    @PostMapping
    public ResponseEntity<Suplemento> adicionarLote(
            @PathVariable String nome,
            @RequestBody Lote lote) {
        return ResponseEntity.ok(service.adicionarLote(nome, lote));
    }
}
