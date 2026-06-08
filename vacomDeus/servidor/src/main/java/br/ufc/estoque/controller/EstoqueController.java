package br.ufc.estoque.controller;

import br.ufc.estoque.model.Suplemento;
import br.ufc.estoque.service.LoteService;
import br.ufc.estoque.service.SuplementoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Objeto distribuído 3 exposto via REST.
 * Fornece relatórios e visão geral do estoque.
 *
 * Endpoints:
 *   GET /api/estoque                         → resumo do estoque (total de itens e quantidade)
 *   GET /api/estoque/vencidos                → suplementos vencidos
 *   GET /api/estoque/vencendo?dias={n}       → suplementos vencendo em até n dias
 */
@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final SuplementoService suplementoService;
    private final LoteService       loteService;

    public EstoqueController(SuplementoService suplementoService, LoteService loteService) {
        this.suplementoService = suplementoService;
        this.loteService       = loteService;
    }

    @GetMapping
    public Map<String, Object> resumo() {
        List<Suplemento> todos = suplementoService.listarTodos();
        int totalItens      = todos.size();
        int totalUnidades   = todos.stream().mapToInt(Suplemento::getQuantidadeTotal).sum();
        int vencidos        = (int) todos.stream().filter(s -> !s.estaValido()).count();
        return Map.of(
                "nomeEstoque",   "Estoque Central",
                "totalItens",    totalItens,
                "totalUnidades", totalUnidades,
                "vencidos",      vencidos
        );
    }

    @GetMapping("/vencidos")
    public List<Suplemento> vencidos() {
        return loteService.listarVencidos();
    }

    @GetMapping("/vencendo")
    public List<Suplemento> vencendo(@RequestParam(defaultValue = "30") int dias) {
        return loteService.listarProximosAoVencer(dias);
    }
}
