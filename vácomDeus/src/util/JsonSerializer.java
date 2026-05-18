package util;

import estoque.Lote;
import modelo.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JsonSerializer {

    public static String suplementoToJson(Suplemento s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"tipo\":\"").append(s.getTipo()).append("\",");
        sb.append("\"nome\":\"").append(esc(s.getNome())).append("\",");
        sb.append("\"marca\":\"").append(esc(s.getMarca())).append("\",");
        sb.append("\"preco\":").append(s.getPreco()).append(",");
        sb.append("\"quantidadeTotal\":").append(s.getQuantidadeTotal()).append(",");
        sb.append("\"estaValido\":").append(s.estaValido()).append(",");
        sb.append("\"diasParaVencer\":").append(s.getDiasParaVencer()).append(",");

        if (s instanceof WheyProtein w) {
            sb.append("\"proteinas\":").append(w.getProteinas()).append(",");
            sb.append("\"sabor\":\"").append(esc(w.getSabor())).append("\",");
        } else if (s instanceof Creatina c) {
            sb.append("\"tipoCreatina\":\"").append(esc(c.getTipoCreatina())).append("\",");
        } else if (s instanceof Vitaminas v) {
            sb.append("\"formulacao\":\"").append(esc(v.getFormulacao())).append("\",");
        } else if (s instanceof PreTreino p) {
            sb.append("\"cafeina\":").append(p.getCafeina()).append(",");
            sb.append("\"temBeta\":").append(p.isTemBeta()).append(",");
        }

        sb.append("\"lotes\":[");
        List<Lote> lotes = s.getLotes();
        for (int i = 0; i < lotes.size(); i++) {
            sb.append(loteToJson(lotes.get(i)));
            if (i < lotes.size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    public static String listaToJson(List<Suplemento> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(suplementoToJson(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String loteToJson(Lote l) {
        return String.format("{\"codigo\":\"%s\",\"quantidade\":%d,\"dataFabricacao\":\"%s\",\"dataVencimento\":\"%s\"}",
                l.getCodigo(), l.getQuantidade(), l.getDataFabricacao(), l.getDataVencimento());
    }

    public static Suplemento jsonToSuplemento(String json) {
        if (json == null || json.equals("null")) return null;
        String tipo  = getString(json, "tipo");
        String nome  = getString(json, "nome");
        String marca = getString(json, "marca");
        double preco = getDouble(json, "preco");

        Suplemento s = switch (tipo) {
            case "Whey Protein" -> new WheyProtein(nome, marca, preco,
                    getDouble(json, "proteinas"), getString(json, "sabor"));
            case "Creatina"     -> new Creatina(nome, marca, preco,
                    getString(json, "tipoCreatina"));
            case "Vitaminas"    -> new Vitaminas(nome, marca, preco,
                    getString(json, "formulacao"));
            case "Pré-Treino"   -> new PreTreino(nome, marca, preco,
                    (int) getDouble(json, "cafeina"), getBool(json, "temBeta"));
            default -> throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
        };

        String lotesJson = getArray(json, "lotes");
        for (String loteJson : splitObjects(lotesJson)) {
            s.adicionarLote(jsonToLote(loteJson));
        }
        return s;
    }

    public static List<Suplemento> jsonToLista(String json) {
        List<Suplemento> lista = new ArrayList<>();
        if (json == null || json.equals("[]")) return lista;
        for (String obj : splitObjects(json.trim().substring(1, json.trim().length() - 1))) {
            lista.add(jsonToSuplemento(obj));
        }
        return lista;
    }

    public static Lote jsonToLote(String json) {
        String    cod  = getString(json, "codigo");
        int       qtd  = (int) getDouble(json, "quantidade");
        LocalDate fab  = LocalDate.parse(getString(json, "dataFabricacao"));
        LocalDate venc = LocalDate.parse(getString(json, "dataVencimento"));
        return new Lote(cod, qtd, fab, venc);
    }

    public static String stringArg(String value)      { return "{\"value\":\"" + esc(value) + "\"}"; }
    public static String intArg(int value)            { return "{\"value\":" + value + "}"; }
    public static String extractStringArg(String json) { return getString(json, "value"); }
    public static int    extractIntArg(String json)    { return (int) getDouble(json, "value"); }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static String getString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) return "";
        start += pattern.length();
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '\\') { end += 2; continue; }
            if (json.charAt(end) == '"')  break;
            end++;
        }
        return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static double getDouble(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) return 0;
        start += pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end))
                || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        try { return Double.parseDouble(json.substring(start, end)); }
        catch (NumberFormatException e) { return 0; }
    }

    public static boolean getBool(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start < 0) return false;
        start += pattern.length();
        return json.startsWith("true", start);
    }

    private static String getArray(String json, String key) {
        String pattern = "\"" + key + "\":[";
        int start = json.indexOf(pattern);
        if (start < 0) return "[]";
        start += pattern.length() - 1;
        return extractBracket(json, start, '[', ']');
    }

    private static String extractBracket(String json, int from, char open, char close) {
        int depth = 0, i = from;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == open)  depth++;
            else if (c == close) { depth--; if (depth == 0) return json.substring(from, i + 1); }
            i++;
        }
        return "[]";
    }

    public static List<String> splitObjects(String json) {
        List<String> result = new ArrayList<>();
        String s = json.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]"))   s = s.substring(0, s.length() - 1);
        s = s.trim();
        if (s.isEmpty()) return result;

        int depth = 0, start = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{' || c == '[') depth++;
            else if (c == '}' || c == ']') {
                depth--;
                if (depth == 0) {
                    result.add(s.substring(start, i + 1).trim());
                    start = i + 2;
                }
            }
        }
        return result;
    }
}
