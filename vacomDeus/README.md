# Trabalho 3B – API REST + Publish-Subscribe via SSE
## Sistemas Distribuídos – QXD0043 | UFC Quixadá

Continuação do Trabalho 3A. O sistema de estoque REST ganhou um
Broker Pub-Sub embutido no Spring Boot usando Server-Sent Events (SSE).
Nenhum socket manual foi criado — toda comunicação é HTTP.

---

## Estrutura do Projeto

```
trabalho3b/
├── servidor/                        # Projeto Java Spring Boot
│   └── src/main/java/br/ufc/estoque/
│       ├── config/
│       │   └── WebConfig.java           # CORS
│       ├── controller/
│       │   ├── SuplementoController.java
│       │   ├── LoteController.java
│       │   ├── EstoqueController.java
│       │   └── EventoController.java    # NOVO: endpoints SSE do broker
│       ├── pubsub/
│       │   ├── Topico.java              # NOVO: enum de tópicos
│       │   ├── Evento.java              # NOVO: record de evento
│       │   └── EventBroker.java         # NOVO: broker SSE + histórico
│       ├── model/      (inalterado)
│       ├── repository/ (inalterado)
│       └── service/
│           ├── SuplementoService.java   # ATUALIZADO: publica eventos
│           └── LoteService.java         # ATUALIZADO: publica eventos
├── cliente-python/
│   ├── cliente.py        # REST + assinatura SSE em threads daemon
│   └── requirements.txt  # requests, sseclient-py
└── cliente-js/
    ├── cliente.js        # REST + assinatura SSE via EventSource
    └── package.json      # eventsource
```

---

## Novos Endpoints Pub-Sub

| Método | Endpoint                                   | Descrição                              |
|--------|--------------------------------------------|----------------------------------------|
| GET    | /api/eventos/topicos                       | Lista tópicos disponíveis              |
| GET    | /api/eventos/subscribe/{topico}?id={id}    | Abre stream SSE (fica aberto)          |
| GET    | /api/eventos/historico/{topico}            | Histórico JSON de um tópico            |
| POST   | /api/eventos/publicar/{topico}             | Publica evento manualmente             |

### Tópicos disponíveis
- `SUPLEMENTO_ADICIONADO`
- `SUPLEMENTO_REMOVIDO`
- `LOTE_ADICIONADO`
- `ALERTA_VENCIMENTO`
- `ESTOQUE_CONSULTADO`

---

## Desacoplamento demonstrado

**Espacial:** `SuplementoService` e `LoteService` chamam apenas
`broker.publicar(topico, origem, payload)` — sem conhecer IP, porta
ou identidade de qualquer subscriber.

**Temporal:** o `EventBroker` persiste todos os eventos em um histórico
por tópico. Ao fazer `GET /api/eventos/subscribe/{topico}`, o broker
entrega automaticamente os eventos acumulados antes de abrir o stream.

---

## Como Executar

### 1. Servidor (Java 21 + Maven)
```bash
cd servidor
mvn spring-boot:run
# sobe em http://localhost:8080
```

### 2. Cliente Python
```bash
cd cliente-python
pip install -r requirements.txt
python cliente.py [host] [id]
# Exemplos:
python cliente.py localhost estudante-1
python cliente.py 192.168.0.10 estudante-2
```

### 3. Cliente JavaScript (Node.js 18+)
```bash
cd cliente-js
npm install
node cliente.js [host] [id]
# Exemplos:
node cliente.js localhost estudante-1
node cliente.js 192.168.0.10 estudante-2
```

---

## Demonstração do Desacoplamento Temporal

Abra dois terminais com clientes diferentes e IDs diferentes:

```bash
# Terminal A (Python)
python cliente.py localhost estudante-1

# Terminal B (JavaScript) — conecta depois
node cliente.js localhost estudante-2
```

No Terminal A, adicione suplementos. Os eventos são publicados e
ficam no histórico. Ao iniciar o Terminal B (JavaScript), ele
conecta ao SSE e recebe automaticamente os eventos anteriores —
mesmo que os tenha perdido por não estar online no momento.

---

## Objetos Distribuídos (mínimo 3)

| # | Classe | Papel |
|---|--------|-------|
| 1 | `EstoqueRepository` | Repositório central em memória |
| 2 | `SuplementoService` | CRUD + publica eventos de suplemento |
| 3 | `LoteService`       | Gestão de lotes + publica alertas de vencimento |

---

## Requisitos do Trabalho 2 Mantidos

- Mínimo 4 classes entidade (Suplemento, WheyProtein, Creatina, Vitaminas, PreTreino, Lote)
- Mínimo 2 composições "tem-um" (Suplemento tem List<Lote>; Repository tem List<Suplemento>)
- Mínimo 2 composições "é-um" (subclasses de Suplemento)
- Mínimo 4 métodos remotos (7 endpoints REST + 4 endpoints SSE)
- Sem sockets ou RMI — apenas HTTP/SSE
- Clientes em 2 linguagens: Python e JavaScript
