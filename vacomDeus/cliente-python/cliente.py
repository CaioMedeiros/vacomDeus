#!/usr/bin/env python3
"""
Cliente Python — API REST + SSE Pub-Sub de Estoque de Suplementos
Trabalho 3B – Sistemas Distribuídos | UFC Quixadá

Requer: pip install requests sseclient-py
Uso:    python cliente.py [host] [id_cliente]
"""

import sys
import json
import threading
import requests
import sseclient

HOST      = sys.argv[1] if len(sys.argv) > 1 else "localhost"
CLIENT_ID = sys.argv[2] if len(sys.argv) > 2 else "cliente-python"
BASE      = f"http://{HOST}:8080/api"

TOPICOS = [
    "SUPLEMENTO_ADICIONADO",
    "SUPLEMENTO_REMOVIDO",
    "LOTE_ADICIONADO",
    "ALERTA_VENCIMENTO",
    "ESTOQUE_CONSULTADO",
]


# ── SSE subscriber ──────────────────────────────────────────────────────────

def assinar_topico(topico):
    url = f"{BASE}/eventos/subscribe/{topico}?id={CLIENT_ID}"
    try:
        response = requests.get(url, stream=True, timeout=None)
        client   = sseclient.SSEClient(response)
        for msg in client.events():
            if msg.data:
                try:
                    raw = msg.data.strip()
                    if raw.startswith("data:"):
                        raw = raw[5:].strip()
                    evt = json.loads(raw)
                    print(f"\n\033[1;33m[EVENTO {evt['topico']}]\033[0m "
                          f"{evt['payload']}  ({evt['timestamp'][:19]})")
                    print("> ", end="", flush=True)
                except Exception:
                    pass
    except Exception:
        pass


def iniciar_subscribers():
    for topico in TOPICOS:
        t = threading.Thread(target=assinar_topico, args=(topico,), daemon=True)
        t.start()


# ── REST helpers ────────────────────────────────────────────────────────────

def api(method, path, body=None, params=None):
    r = requests.request(method, f"{BASE}{path}", json=body, params=params)
    r.raise_for_status()
    return r.json() if r.text else None


def cabecalho(titulo):
    print(f"\n{'='*52}\n  {titulo}\n{'='*52}")


# ── operações ───────────────────────────────────────────────────────────────

def resumo_estoque():
    d = api("GET", "/estoque")
    cabecalho("RESUMO DO ESTOQUE")
    print(f"  Nome:           {d['nomeEstoque']}")
    print(f"  Total de itens: {d['totalItens']}")
    print(f"  Total unidades: {d['totalUnidades']}")
    print(f"  Vencidos:       {d['vencidos']}")


def listar_suplementos():
    lista = api("GET", "/suplementos")
    cabecalho("SUPLEMENTOS CADASTRADOS")
    if not lista:
        print("  (nenhum)")
        return
    for s in lista:
        valido = "Sim" if s["estaValido"] else "NAO"
        print(f"  [{s['tipo']}] {s['nome']} - {s['marca']}"
              f" | R$ {s['preco']:.2f} | Qtd: {s['quantidadeTotal']} | Valido: {valido}")


def adicionar_suplemento():
    cabecalho("ADICIONAR SUPLEMENTO")
    tipos = {"1": "Whey Protein", "2": "Creatina", "3": "Vitaminas", "4": "Pre-Treino"}
    for k, v in tipos.items():
        print(f"  {k}. {v}")
    tipo_key = input("Tipo: ").strip()
    tipo = tipos.get(tipo_key)
    if not tipo:
        print("Tipo invalido."); return

    nome  = input("Nome: ").strip()
    marca = input("Marca: ").strip()
    preco = float(input("Preco: ").strip())
    body  = {"tipo": tipo, "nome": nome, "marca": marca, "preco": preco, "lotes": []}

    if tipo == "Whey Protein":
        body["proteinas"] = float(input("Proteinas por porcao (g): ").strip())
        body["sabor"]     = input("Sabor: ").strip()
    elif tipo == "Creatina":
        body["tipoCreatina"] = input("Tipo de creatina: ").strip()
    elif tipo == "Vitaminas":
        body["formulacao"] = input("Formulacao: ").strip()
    elif tipo == "Pre-Treino":
        body["tipo"] = "Pre-Treino"
        body["cafeina"]        = int(input("Cafeina (mg): ").strip())
        body["temBetaAlanina"] = input("Tem Beta-Alanina? (s/n): ").strip().lower() == "s"

    s = api("POST", "/suplementos", body)
    print(f"  OK Adicionado: {s['nome']}")


def adicionar_lote():
    cabecalho("ADICIONAR LOTE")
    nome = input("Nome do suplemento: ").strip()
    cod  = input("Codigo do lote: ").strip()
    qtd  = int(input("Quantidade: ").strip())
    fab  = input("Data fabricacao (AAAA-MM-DD): ").strip()
    venc = input("Data vencimento (AAAA-MM-DD): ").strip()
    s = api("POST", f"/suplementos/{nome}/lotes",
            {"codigo": cod, "quantidade": qtd,
             "dataFabricacao": fab, "dataVencimento": venc})
    print(f"  OK Lote {cod} adicionado a {s['nome']}")


def buscar_suplemento():
    cabecalho("BUSCAR SUPLEMENTO")
    nome = input("Nome: ").strip()
    s = api("GET", f"/suplementos/{nome}")
    print(f"  OK [{s['tipo']}] {s['nome']} - {s['marca']}")
    print(f"     Preco: R$ {s['preco']:.2f} | Qtd: {s['quantidadeTotal']}")
    print(f"     Valido: {'Sim' if s['estaValido'] else 'NAO'} | Vence em: {s['diasParaVencer']} dias")
    for l in s.get("lotes", []):
        print(f"       * {l['codigo']} | Qtd: {l['quantidade']} | Venc: {l['dataVencimento']}")


def vencidos():
    lista = api("GET", "/estoque/vencidos")
    cabecalho("SUPLEMENTOS VENCIDOS")
    if not lista:
        print("  Nenhum vencido.")
    else:
        for s in lista:
            print(f"  X {s['nome']} ({s['tipo']})")


def vencendo():
    cabecalho("VENCENDO EM X DIAS")
    dias = input("Vencendo em ate quantos dias? ").strip() or "30"
    lista = api("GET", "/estoque/vencendo", params={"dias": dias})
    if not lista:
        print(f"  Nenhum vencendo em ate {dias} dias.")
    else:
        for s in lista:
            print(f"  ! {s['nome']} - {s['diasParaVencer']} dias")


def remover():
    cabecalho("REMOVER SUPLEMENTO")
    nome = input("Nome: ").strip()
    res = api("DELETE", f"/suplementos/{nome}")
    print(f"  OK {res['mensagem']}")


def historico_eventos():
    cabecalho("HISTORICO DE EVENTOS (Pub-Sub)")
    topicos = ["SUPLEMENTO_ADICIONADO", "SUPLEMENTO_REMOVIDO",
               "LOTE_ADICIONADO", "ALERTA_VENCIMENTO"]
    for t in topicos:
        evts = api("GET", f"/eventos/historico/{t}")
        if evts:
            print(f"\n  [{t}] - {len(evts)} evento(s):")
            for e in evts[-3:]:
                print(f"    * {e['payload']}  ({e['timestamp'][:19]})")


# ── menu ────────────────────────────────────────────────────────────────────

OPCOES = [
    ("Resumo do estoque",             resumo_estoque),
    ("Listar suplementos",            listar_suplementos),
    ("Adicionar suplemento",          adicionar_suplemento),
    ("Adicionar lote",                adicionar_lote),
    ("Buscar suplemento",             buscar_suplemento),
    ("Relatorio: vencidos",           vencidos),
    ("Relatorio: vencendo em X dias", vencendo),
    ("Remover suplemento",            remover),
    ("Ver historico de eventos",      historico_eventos),
]


def menu():
    print("\n==========================================")
    print("  CLIENTE PYTHON - ESTOQUE API + PUB-SUB")
    print(f"  ID: {CLIENT_ID}")
    print(f"  Servidor: {BASE}")
    print("==========================================")

    iniciar_subscribers()
    print(f"  Assinado em {len(TOPICOS)} topicos SSE\n")

    while True:
        print("\n" + "-"*52)
        for i, (label, _) in enumerate(OPCOES, 1):
            print(f"  {i}. {label}")
        print("  0. Sair")
        print("-"*52)
        op = input("> ").strip()

        if op == "0":
            print("Encerrando..."); break
        try:
            idx = int(op) - 1
            if 0 <= idx < len(OPCOES):
                OPCOES[idx][1]()
            else:
                print("  Opcao invalida.")
        except ValueError:
            print("  Opcao invalida.")
        except requests.exceptions.ConnectionError:
            print(f"  Servidor indisponivel em {BASE}")
        except requests.exceptions.HTTPError as e:
            print(f"  Erro HTTP: {e}")
        except Exception as e:
            print(f"  Erro: {e}")


if __name__ == "__main__":
    menu()
