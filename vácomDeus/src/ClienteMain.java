import estoque.Lote;
import modelo.*;
import rmi.client.EstoqueProxy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Cliente RMI — menu interativo.
 * Toda operação é feita via proxy, que serializa os dados em JSON
 * e os envia ao servidor através do protocolo requisição-resposta.
 */
public class ClienteMain {

    static EstoqueProxy proxy;
    static Scanner sc  = new Scanner(System.in);
    static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int    port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;

        proxy = new EstoqueProxy(host, port);

        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║   CLIENTE RMI — ESTOQUE          ║");
        System.out.printf( "║   Conectando a %s:%d%n", host, port);
        System.out.println("╚══════════════════════════════════╝");

        int opcao;
        do {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║ 1. Adicionar suplemento          ║");
            System.out.println("║ 2. Adicionar lote a suplemento   ║");
            System.out.println("║ 3. Buscar suplemento             ║");
            System.out.println("║ 4. Listar estoque completo       ║");
            System.out.println("║ 5. Relatório: vencidos           ║");
            System.out.println("║ 6. Relatório: vencendo em X dias ║");
            System.out.println("║ 7. Remover suplemento            ║");
            System.out.println("║ 0. Sair                          ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Opção: ");
            opcao = Integer.parseInt(sc.nextLine().trim());

            switch (opcao) {
                case 1 -> adicionarSuplemento();
                case 2 -> adicionarLote();
                case 3 -> buscar();
                case 4 -> listarEstoque();
                case 5 -> listarVencidos();
                case 6 -> listarProximosVencer();
                case 7 -> remover();
                case 0 -> System.out.println("Encerrando cliente...");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    static void adicionarSuplemento() {
        System.out.println("\nTipo: 1-Whey  2-Creatina  3-Vitaminas  4-Pré-Treino");
        System.out.print("Tipo: ");
        int tipo = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Nome: ");   String nome  = sc.nextLine();
        System.out.print("Marca: ");  String marca = sc.nextLine();
        System.out.print("Preço: ");  double preco = Double.parseDouble(sc.nextLine().trim());

        Suplemento s = switch (tipo) {
            case 1 -> {
                System.out.print("Proteínas por porção (g): ");
                double prot = Double.parseDouble(sc.nextLine().trim());
                System.out.print("Sabor: ");
                String sabor = sc.nextLine();
                yield new WheyProtein(nome, marca, preco, prot, sabor);
            }
            case 2 -> {
                System.out.print("Tipo de creatina (ex: Monohidratada): ");
                yield new Creatina(nome, marca, preco, sc.nextLine());
            }
            case 3 -> {
                System.out.print("Formulação (ex: Vitamina C 1000mg): ");
                yield new Vitaminas(nome, marca, preco, sc.nextLine());
            }
            case 4 -> {
                System.out.print("Cafeína (mg): ");
                int caf = Integer.parseInt(sc.nextLine().trim());
                System.out.print("Tem Beta-Alanina? (s/n): ");
                boolean beta = sc.nextLine().trim().equalsIgnoreCase("s");
                yield new PreTreino(nome, marca, preco, caf, beta);
            }
            default -> { System.out.println("Tipo inválido."); yield null; }
        };

        if (s != null) System.out.println(proxy.adicionarSuplemento(s));
    }

    static void adicionarLote() {
        System.out.print("\nNome do suplemento: ");
        String nome = sc.nextLine();

        System.out.print("Código do lote (ex: L010): ");
        String cod = sc.nextLine();
        System.out.print("Quantidade: ");
        int qtd = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Data de fabricação (DD-MM-AAAA): ");
        LocalDate fab  = LocalDate.parse(sc.nextLine().trim(), fmt);
        System.out.print("Data de vencimento (DD-MM-AAAA): ");
        LocalDate venc = LocalDate.parse(sc.nextLine().trim(), fmt);

        System.out.println(proxy.adicionarLote(nome, new Lote(cod, qtd, fab, venc)));
    }

    static void buscar() {
        System.out.print("\nNome: ");
        Suplemento s = proxy.buscarSuplemento(sc.nextLine());
        if (s != null) System.out.println("  ✔ " + s);
        else           System.out.println("  ✘ Não encontrado.");
    }

    static void listarEstoque() {
        List<Suplemento> lista = proxy.listarEstoque();
        System.out.println("\n═══ Estoque Central ═══");
        if (lista.isEmpty()) System.out.println("  (vazio)");
        else lista.forEach(s -> System.out.println("  • " + s));
        System.out.println("Total: " + lista.size());
    }

    static void listarVencidos() {
        List<Suplemento> lista = proxy.listarVencidos();
        System.out.println("\n⚠ Suplementos VENCIDOS:");
        if (lista.isEmpty()) System.out.println("  Nenhum.");
        else lista.forEach(s -> System.out.println("  ✘ " + s.getNome()));
    }

    static void listarProximosVencer() {
        System.out.print("Vencendo em até quantos dias? ");
        int dias = Integer.parseInt(sc.nextLine().trim());
        List<Suplemento> lista = proxy.listarProximosAoVencer(dias);
        System.out.println("\n⏳ Vencendo em até " + dias + " dias:");
        if (lista.isEmpty()) System.out.println("  Nenhum.");
        else lista.forEach(s -> System.out.printf("  ! %s — %d dias%n",
                s.getNome(), s.getDiasParaVencer()));
    }

    static void remover() {
        System.out.print("\nNome a remover: ");
        System.out.println(proxy.removerSuplemento(sc.nextLine()));
    }
}
