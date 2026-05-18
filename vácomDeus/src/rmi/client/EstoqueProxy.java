package rmi.client;

import estoque.Lote;
import modelo.Suplemento;
import rmi.common.EstoqueService;
import rmi.common.RemoteObjectRef;
import util.JsonSerializer;

import java.util.List;

public class EstoqueProxy implements EstoqueService {

    private final RemoteObjectRef ref;
    private final CommunicationModule comm;

    public EstoqueProxy(String host, int port) {
        this.ref  = new RemoteObjectRef(host, port, "EstoqueService");
        this.comm = new CommunicationModule();
    }

    @Override
    public String adicionarSuplemento(Suplemento s) {
        try {
            byte[] args   = JsonSerializer.suplementoToJson(s).getBytes("UTF-8");
            byte[] result = comm.doOperation(ref, "adicionarSuplemento", args);
            return new String(result, "UTF-8");
        } catch (Exception e) { return "✘ Erro: " + e.getMessage(); }
    }

    @Override
    public String adicionarLote(String nomeSuplemento, Lote lote) {
        try {
            String json = "{\"nomeSuplemento\":\"" + nomeSuplemento + "\","
                    + "\"lote\":" + JsonSerializer.loteToJson(lote) + "}";
            byte[] result = comm.doOperation(ref, "adicionarLote", json.getBytes("UTF-8"));
            return new String(result, "UTF-8");
        } catch (Exception e) { return "✘ Erro: " + e.getMessage(); }
    }

    @Override
    public Suplemento buscarSuplemento(String nome) {
        try {
            byte[] args   = JsonSerializer.stringArg(nome).getBytes("UTF-8");
            byte[] result = comm.doOperation(ref, "buscarSuplemento", args);
            return JsonSerializer.jsonToSuplemento(new String(result, "UTF-8"));
        } catch (Exception e) { System.err.println("✘ Erro: " + e.getMessage()); return null; }
    }

    @Override
    public List<Suplemento> listarEstoque() {
        try {
            byte[] result = comm.doOperation(ref, "listarEstoque", "{}".getBytes("UTF-8"));
            return JsonSerializer.jsonToLista(new String(result, "UTF-8"));
        } catch (Exception e) { System.err.println("✘ Erro: " + e.getMessage()); return List.of(); }
    }

    @Override
    public List<Suplemento> listarVencidos() {
        try {
            byte[] result = comm.doOperation(ref, "listarVencidos", "{}".getBytes("UTF-8"));
            return JsonSerializer.jsonToLista(new String(result, "UTF-8"));
        } catch (Exception e) { System.err.println("✘ Erro: " + e.getMessage()); return List.of(); }
    }

    @Override
    public List<Suplemento> listarProximosAoVencer(int dias) {
        try {
            byte[] args   = JsonSerializer.intArg(dias).getBytes("UTF-8");
            byte[] result = comm.doOperation(ref, "listarProximosAoVencer", args);
            return JsonSerializer.jsonToLista(new String(result, "UTF-8"));
        } catch (Exception e) { System.err.println("✘ Erro: " + e.getMessage()); return List.of(); }
    }

    @Override
    public String removerSuplemento(String nome) {
        try {
            byte[] args   = JsonSerializer.stringArg(nome).getBytes("UTF-8");
            byte[] result = comm.doOperation(ref, "removerSuplemento", args);
            return new String(result, "UTF-8");
        } catch (Exception e) { return "✘ Erro: " + e.getMessage(); }
    }
}
