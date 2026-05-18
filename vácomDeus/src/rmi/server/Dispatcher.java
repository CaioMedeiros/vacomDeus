package rmi.server;

import estoque.Estoque;
import estoque.Lote;
import modelo.Suplemento;
import rmi.common.ReplyMessage;
import rmi.common.RequestMessage;
import util.JsonSerializer;

import java.util.List;

public class Dispatcher {

    private final EstoqueSkeletonImpl skeleton;

    public Dispatcher(Estoque estoque) {
        this.skeleton = new EstoqueSkeletonImpl(estoque);
    }

    public ReplyMessage dispatch(RequestMessage req) {
        try {
            String methodId = req.getMethodId();
            String argsJson = new String(req.getArguments(), "UTF-8");
            byte[] result;

            switch (methodId) {

                case "adicionarSuplemento" -> {
                    Suplemento s = JsonSerializer.jsonToSuplemento(argsJson);
                    String resp  = skeleton.adicionarSuplemento(s);
                    result = resp.getBytes("UTF-8");
                }

                case "adicionarLote" -> {
                    String nomeS = JsonSerializer.getString(argsJson, "nomeSuplemento");
                    String loteJ = getLoteJson(argsJson);
                    Lote lote    = JsonSerializer.jsonToLote(loteJ);
                    String resp  = skeleton.adicionarLote(nomeS, lote);
                    result = resp.getBytes("UTF-8");
                }

                case "buscarSuplemento" -> {
                    String nome  = JsonSerializer.extractStringArg(argsJson);
                    Suplemento s = skeleton.buscarSuplemento(nome);
                    result = JsonSerializer.suplementoToJson(s).getBytes("UTF-8");
                }

                case "listarEstoque" -> {
                    List<Suplemento> lista = skeleton.listarEstoque();
                    result = JsonSerializer.listaToJson(lista).getBytes("UTF-8");
                }

                case "listarVencidos" -> {
                    List<Suplemento> lista = skeleton.listarVencidos();
                    result = JsonSerializer.listaToJson(lista).getBytes("UTF-8");
                }

                case "listarProximosAoVencer" -> {
                    int dias             = JsonSerializer.extractIntArg(argsJson);
                    List<Suplemento> lista = skeleton.listarProximosAoVencer(dias);
                    result = JsonSerializer.listaToJson(lista).getBytes("UTF-8");
                }

                case "removerSuplemento" -> {
                    String nome = JsonSerializer.extractStringArg(argsJson);
                    String resp = skeleton.removerSuplemento(nome);
                    result = resp.getBytes("UTF-8");
                }

                default -> {
                    result = ("ERRO: método desconhecido '" + methodId + "'").getBytes("UTF-8");
                    return new ReplyMessage(req.getRequestId(), "ERROR", result);
                }
            }

            return new ReplyMessage(req.getRequestId(), "OK", result);

        } catch (Exception e) {
            try {
                return new ReplyMessage(req.getRequestId(), "ERROR",
                        ("ERRO: " + e.getMessage()).getBytes("UTF-8"));
            } catch (Exception ex) {
                return new ReplyMessage(req.getRequestId(), "ERROR", new byte[0]);
            }
        }
    }

    private String getLoteJson(String argsJson) {
        String pattern = "\"lote\":";
        int start = argsJson.indexOf(pattern);
        if (start < 0) return "{}";
        start += pattern.length();
        return extractObject(argsJson, start);
    }

    private String extractObject(String json, int from) {
        int depth = 0;
        for (int i = from; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') { depth--; if (depth == 0) return json.substring(from, i + 1); }
        }
        return "{}";
    }
}
