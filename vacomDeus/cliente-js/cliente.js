#!/usr/bin/env node
/**
 * Cliente JavaScript (Node.js) — API REST + SSE Pub-Sub
 * Trabalho 3B – Sistemas Distribuídos | UFC Quixadá
 *
 * Requer: Node.js 18+  |  npm install eventsource
 * Uso:    node cliente.js [host] [id_cliente]
 */

import EventSource from "eventsource";
import * as readline from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";

const HOST      = process.argv[2] ?? "localhost";
const CLIENT_ID = process.argv[3] ?? "cliente-js";
const BASE      = `http://${HOST}:8080/api`;
const rl        = readline.createInterface({ input, output });
const ask       = (q) => rl.question(q);

const TOPICOS = [
    "SUPLEMENTO_ADICIONADO",
    "SUPLEMENTO_REMOVIDO",
    "LOTE_ADICIONADO",
    "ALERTA_VENCIMENTO",
    "ESTOQUE_CONSULTADO",
];

// ── SSE subscriber ──────────────────────────────────────────────────────────

function assinarTopico(topico) {
    const url = `${BASE}/eventos/subscribe/${topico}?id=${CLIENT_ID}`;
    const es  = new EventSource(url);

    es.addEventListener("evento", (e) => {
        try {
            let raw = e.data.trim();
            if (raw.startsWith("data:")) raw = raw.slice(5).trim();
            const evt = JSON.parse(raw);
            process.stdout.write(
                `\n\x1b[1;33m[EVENTO ${evt.topico}]\x1b[0m ${evt.payload}  (${evt.timestamp.slice(0,19)})\n> `
            );
        } catch (_) {}
    });

    es.onerror = () => {};  // reconecta automaticamente
    return es;
}

function iniciarSubscribers() {
    TOPICOS.forEach(t => assinarTopico(t));
}

// ── REST helpers ────────────────────────────────────────────────────────────

async function api(method, path, body, params) {
    let url = `${BASE}${path}`;
    if (params) url += "?" + new URLSearchParams(params).toString();
    const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: body ? JSON.stringify(body) : undefined,
    });
    const text = await res.text();
    if (!res.ok) throw new Error(`HTTP ${res.status}: ${text}`);
    return text ? JSON.parse(text) : null;
}

const line = (n = 52) => console.log("-".repeat(n));
const head = (t) => { console.log(); console.log("=".repeat(52)); console.log(`  ${t}`); console.log("=".repeat(52)); };

// ── operações ───────────────────────────────────────────────────────────────

async function resumoEstoque() {
    const d = await api("GET", "/estoque");
    head("RESUMO DO ESTOQUE");
    console.log(`  Nome:           ${d.nomeEstoque}`);
    console.log(`  Total de itens: ${d.totalItens}`);
    console.log(`  Total unidades: ${d.totalUnidades}`);
    console.log(`  Vencidos:       ${d.vencidos}`);
}

async function listarSuplementos() {
    const lista = await api("GET", "/suplementos");
    head("SUPLEMENTOS CADASTRADOS");
    if (!lista.length) { console.log("  (nenhum)"); return; }
    for (const s of lista) {
        console.log(`  [${s.tipo}] ${s.nome} - ${s.marca} | R$ ${s.preco.toFixed(2)}` +
                    ` | Qtd: ${s.quantidadeTotal} | Valido: ${s.estaValido ? "Sim" : "NAO"}`);
    }
}

async function adicionarSuplemento() {
    head("ADICIONAR SUPLEMENTO");
    const tipos = { "1": "Whey Protein", "2": "Creatina", "3": "Vitaminas", "4": "Pre-Treino" };
    Object.entries(tipos).forEach(([k, v]) => console.log(`  ${k}. ${v}`));
    const tipoKey = (await ask("Tipo: ")).trim();
    const tipo    = tipos[tipoKey];
    if (!tipo) { console.log("  Tipo invalido."); return; }

    const nome  = (await ask("Nome: ")).trim();
    const marca = (await ask("Marca: ")).trim();
    const preco = parseFloat(await ask("Preco: "));
    const body  = { tipo, nome, marca, preco, lotes: [] };

    if (tipo === "Whey Protein") {
        body.proteinas = parseFloat(await ask("Proteinas por porcao (g): "));
        body.sabor     = (await ask("Sabor: ")).trim();
    } else if (tipo === "Creatina") {
        body.tipoCreatina = (await ask("Tipo de creatina: ")).trim();
    } else if (tipo === "Vitaminas") {
        body.formulacao = (await ask("Formulacao: ")).trim();
    } else if (tipo === "Pre-Treino") {
        body.cafeina        = parseInt(await ask("Cafeina (mg): "));
        body.temBetaAlanina = (await ask("Tem Beta-Alanina? (s/n): ")).trim().toLowerCase() === "s";
    }

    const s = await api("POST", "/suplementos", body);
    console.log(`  OK Adicionado: ${s.nome}`);
}

async function adicionarLote() {
    head("ADICIONAR LOTE");
    const nome = (await ask("Nome do suplemento: ")).trim();
    const cod  = (await ask("Codigo do lote: ")).trim();
    const qtd  = parseInt(await ask("Quantidade: "));
    const fab  = (await ask("Data fabricacao (AAAA-MM-DD): ")).trim();
    const venc = (await ask("Data vencimento (AAAA-MM-DD): ")).trim();
    const s = await api("POST", `/suplementos/${nome}/lotes`,
            { codigo: cod, quantidade: qtd, dataFabricacao: fab, dataVencimento: venc });
    console.log(`  OK Lote ${cod} adicionado a ${s.nome}`);
}

async function buscarSuplemento() {
    head("BUSCAR SUPLEMENTO");
    const nome = (await ask("Nome: ")).trim();
    const s = await api("GET", `/suplementos/${nome}`);
    console.log(`  OK [${s.tipo}] ${s.nome} - ${s.marca}`);
    console.log(`     Preco: R$ ${s.preco.toFixed(2)} | Qtd: ${s.quantidadeTotal}`);
    console.log(`     Valido: ${s.estaValido ? "Sim" : "NAO"} | Vence em: ${s.diasParaVencer} dias`);
    for (const l of s.lotes ?? [])
        console.log(`       * ${l.codigo} | Qtd: ${l.quantidade} | Venc: ${l.dataVencimento}`);
}

async function vencidos() {
    const lista = await api("GET", "/estoque/vencidos");
    head("SUPLEMENTOS VENCIDOS");
    if (!lista.length) { console.log("  Nenhum vencido."); return; }
    for (const s of lista) console.log(`  X ${s.nome} (${s.tipo})`);
}

async function vencendo() {
    head("VENCENDO EM X DIAS");
    const dias = (await ask("Vencendo em ate quantos dias? ")).trim() || "30";
    const lista = await api("GET", `/estoque/vencendo`, null, { dias });
    if (!lista.length) { console.log(`  Nenhum vencendo em ate ${dias} dias.`); return; }
    for (const s of lista) console.log(`  ! ${s.nome} - ${s.diasParaVencer} dias`);
}

async function remover() {
    head("REMOVER SUPLEMENTO");
    const nome = (await ask("Nome: ")).trim();
    const res  = await api("DELETE", `/suplementos/${nome}`);
    console.log(`  OK ${res.mensagem}`);
}

async function historicoEventos() {
    head("HISTORICO DE EVENTOS (Pub-Sub)");
    const topicos = ["SUPLEMENTO_ADICIONADO","SUPLEMENTO_REMOVIDO","LOTE_ADICIONADO","ALERTA_VENCIMENTO"];
    for (const t of topicos) {
        const evts = await api("GET", `/eventos/historico/${t}`);
        if (evts?.length) {
            console.log(`\n  [${t}] - ${evts.length} evento(s):`);
            evts.slice(-3).forEach(e =>
                console.log(`    * ${e.payload}  (${e.timestamp.slice(0,19)})`));
        }
    }
}

// ── menu ────────────────────────────────────────────────────────────────────

const OPCOES = [
    ["Resumo do estoque",             resumoEstoque],
    ["Listar suplementos",            listarSuplementos],
    ["Adicionar suplemento",          adicionarSuplemento],
    ["Adicionar lote",                adicionarLote],
    ["Buscar suplemento",             buscarSuplemento],
    ["Relatorio: vencidos",           vencidos],
    ["Relatorio: vencendo em X dias", vencendo],
    ["Remover suplemento",            remover],
    ["Ver historico de eventos",      historicoEventos],
];

async function main() {
    console.log("\n==========================================");
    console.log("  CLIENTE JS - ESTOQUE API + PUB-SUB");
    console.log(`  ID: ${CLIENT_ID}`);
    console.log(`  Servidor: ${BASE}`);
    console.log("==========================================");

    iniciarSubscribers();
    console.log(`  Assinado em ${TOPICOS.length} topicos SSE\n`);

    while (true) {
        console.log();
        line();
        OPCOES.forEach(([label], i) => console.log(`  ${i + 1}. ${label}`));
        console.log("  0. Sair");
        line();
        const op = (await ask("> ")).trim();

        if (op === "0") { console.log("Encerrando..."); break; }
        const idx = parseInt(op) - 1;
        if (isNaN(idx) || idx < 0 || idx >= OPCOES.length) {
            console.log("  Opcao invalida."); continue;
        }
        try {
            await OPCOES[idx][1]();
        } catch (e) {
            console.log(`  Erro: ${e.message}`);
        }
    }
    rl.close();
    process.exit(0);
}

main();
