import estoque.Estoque;
import estoque.Lote;
import modelo.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    static Estoque estoque = new Estoque("Estoque Central");
    static Scanner sc = new Scanner(System.in);
    static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        int opcao;
        do {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║     SISTEMA DE ESTOQUE           ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║ 1. Adicionar suplemento          ║");
            System.out.println("║ 2. Adicionar lote a suplemento   ║");
            System.out.println("║ 3. Consultar lotes de um item    ║");
            System.out.println("║ 4. Listar estoque completo       ║");
            System.out.println("║ 5. Relatório: vencidos           ║");
            System.out.println("║ 6. Relatório: vencendo em X dias ║");
            System.out.println("║ 7. Buscar suplemento             ║");
            System.out.println("║ 8. Remover suplemento            ║");
            System.out.println("║ 0. Sair                          ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Opção: ");
            opcao = Integer.parseInt(sc.nextLine().trim());

            switch (opcao) {
                case 1 -> adicionarSuplemento();
                case 2 -> adicionarLote();
                case 3 -> consultarLotes();
                case 4 -> estoque.exibirEstoque();
                case 5 -> listarVencidos();
                case 6 -> listarProximosVencer();
                case 7 -> buscar();
                case 8 -> remover();
                case 0 -> System.out.println("Encerrando...");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    // add suplemento
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
                String tipoC = sc.nextLine();
                yield new Creatina(nome, marca, preco, tipoC);
            }
            case 3 -> {
                System.out.print("Formulação (ex: Vitamina C 1000mg): ");
                String form = sc.nextLine();
                yield new Vitaminas(nome, marca, preco, form);
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

        if (s != null) estoque.adicionarItem(s);
    }

    //  add lote um suplemento existente
    static void adicionarLote() {
        System.out.print("\nNome do suplemento: ");
        Suplemento s = estoque.buscarItem(sc.nextLine());

        if (s == null) { System.out.println("✘ Suplemento não encontrado."); return; }

        System.out.print("Código do lote (ex: L010): ");
        String cod = sc.nextLine();
        System.out.print("Quantidade: ");
        int qtd = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Data de fabricação (DD-MM-AAAA): ");
        LocalDate fab  = LocalDate.parse(sc.nextLine().trim(), fmt);
        System.out.print("Data de vencimento (DD-MM-AAAA): ");
        LocalDate venc = LocalDate.parse(sc.nextLine().trim(), fmt);


        s.adicionarLote(new Lote(cod, qtd, fab, venc));
        System.out.println("✔ Lote " + cod + " adicionado a " + s.getNome());
    }

    // consultar todos os lotes de um item
    static void consultarLotes() {
        System.out.print("\nNome do suplemento: ");
        Suplemento s = estoque.buscarItem(sc.nextLine());

        if (s == null) { System.out.println("✘ Não encontrado."); return; }

        System.out.println("\nLotes de " + s.getNome() + ":");
        if (s.getLotes().isEmpty()) {
            System.out.println("  Nenhum lote cadastrado.");
        } else {
            s.getLotes().forEach(l -> System.out.println("  • " + l));
        }
    }

    // vencidos
    static void listarVencidos() {
        var lista = estoque.listarVencidos();
        System.out.println("\n⚠ Suplementos VENCIDOS:");
        if (lista.isEmpty()) System.out.println("  Nenhum.");
        else lista.forEach(s -> System.out.println("  ✘ " + s.getNome()));
    }

    // perto de vencer
    static void listarProximosVencer() {
        System.out.print("Vencendo em até quantos dias? ");
        int dias = Integer.parseInt(sc.nextLine().trim());
        var lista = estoque.listarProximosAoVencer(dias);
        System.out.println("\n⏳ Vencendo em até " + dias + " dias:");
        if (lista.isEmpty()) System.out.println("  Nenhum.");
        else lista.forEach(s -> System.out.printf("  ! %s — %d dias%n",
                s.getNome(), s.getDiasParaVencer()));
    }

    // buscar
    static void buscar() {
        System.out.print("\nNome: ");
        Suplemento s = estoque.buscarItem(sc.nextLine());
        if (s != null) System.out.println("  ✔ " + s);
        else System.out.println("  ✘ Não encontrado.");
    }

    // deletar
    static void remover() {
        System.out.print("\nNome a remover: ");
        estoque.removerItem(sc.nextLine());
    }
}