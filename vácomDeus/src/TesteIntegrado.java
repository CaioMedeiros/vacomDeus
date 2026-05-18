import estoque.Lote;
import modelo.*;
import rmi.client.EstoqueProxy;
import rmi.server.Dispatcher;
import rmi.server.RequestReplyProtocol;
import rmi.server.EstoqueSkeletonImpl;
import estoque.Estoque;
import rmi.common.ReplyMessage;
import rmi.common.RequestMessage;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.List;

public class TesteIntegrado {

    public static void main(String[] args) throws Exception {
        Thread serverThread = new Thread(() -> {
            try {
                Estoque estoque = new Estoque("Estoque Central");
                Dispatcher dispatcher = new Dispatcher(estoque);
                RequestReplyProtocol rrp = new RequestReplyProtocol(5001);

                System.out.println("[SERVIDOR] Aguardando requisições na porta 5001...");
                while (true) {
                    RequestMessage req    = rrp.getRequest();
                    InetAddress addr      = rrp.getClientAddress();
                    int port              = rrp.getClientPort();
                    System.out.printf("[SERVIDOR] REQ #%d método=%s%n",
                            req.getRequestId(), req.getMethodId());
                    ReplyMessage reply = dispatcher.dispatch(req);
                    rrp.sendReply(reply, addr, port);
                    System.out.printf("[SERVIDOR] REP #%d status=%s%n",
                            reply.getRequestId(), reply.getStatus());
                }
            } catch (Exception e) {
                System.out.println("[SERVIDOR] Encerrado: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500);

        EstoqueProxy proxy = new EstoqueProxy("localhost", 5001);

        System.out.println("\n========== TESTE 1: Adicionar Whey Protein ==========");
        WheyProtein whey = new WheyProtein("Whey Gold Standard", "Optimum Nutrition", 299.90, 25.0, "Chocolate");
        System.out.println(proxy.adicionarSuplemento(whey));

        System.out.println("\n========== TESTE 2: Adicionar Creatina ==========");
        Creatina creatina = new Creatina("Creatina Monohidratada", "Integralmedica", 89.90, "Monohidratada");
        System.out.println(proxy.adicionarSuplemento(creatina));

        System.out.println("\n========== TESTE 3: Adicionar Vitamina ==========");
        Vitaminas vitamina = new Vitaminas("Vitamina C", "Now Foods", 45.00, "1000mg");
        System.out.println(proxy.adicionarSuplemento(vitamina));

        System.out.println("\n========== TESTE 4: Adicionar Pré-Treino ==========");
        PreTreino pre = new PreTreino("C4 Original", "Cellucor", 199.90, 150, true);
        System.out.println(proxy.adicionarSuplemento(pre));

        System.out.println("\n========== TESTE 5: Adicionar Lote ao Whey ==========");
        Lote lote1 = new Lote("L001", 50, LocalDate.of(2025, 1, 1), LocalDate.of(2026, 12, 1));
        System.out.println(proxy.adicionarLote("Whey Gold Standard", lote1));

        System.out.println("\n========== TESTE 6: Adicionar Lote vencido (para teste de relatório) ==========");
        Lote loteVencido = new Lote("L002", 10, LocalDate.of(2023, 1, 1), LocalDate.of(2024, 1, 1));
        System.out.println(proxy.adicionarLote("Creatina Monohidratada", loteVencido));

        System.out.println("\n========== TESTE 7: Listar Estoque Completo ==========");
        List<Suplemento> estoque = proxy.listarEstoque();
        estoque.forEach(s -> System.out.println("  • " + s));

        System.out.println("\n========== TESTE 8: Buscar Suplemento ==========");
        Suplemento encontrado = proxy.buscarSuplemento("Whey Gold Standard");
        System.out.println(encontrado != null ? "  ✔ " + encontrado : "  ✘ Não encontrado");

        System.out.println("\n========== TESTE 9: Listar Vencidos ==========");
        List<Suplemento> vencidos = proxy.listarVencidos();
        if (vencidos.isEmpty()) System.out.println("  Nenhum vencido.");
        else vencidos.forEach(s -> System.out.println("  ✘ " + s.getNome()));

        System.out.println("\n========== TESTE 10: Listar Próximos ao Vencer (500 dias) ==========");
        List<Suplemento> proximos = proxy.listarProximosAoVencer(500);
        if (proximos.isEmpty()) System.out.println("  Nenhum.");
        else proximos.forEach(s -> System.out.printf("  ! %s — %d dias%n", s.getNome(), s.getDiasParaVencer()));

        System.out.println("\n========== TESTE 11: Remover Suplemento ==========");
        System.out.println(proxy.removerSuplemento("Vitamina C"));

        System.out.println("\n========== TESTE 12: Estoque após remoção ==========");
        proxy.listarEstoque().forEach(s -> System.out.println("  • " + s.getNome()));

        System.out.println("\n\n✔ Todos os testes concluídos com sucesso!");
    }
}
