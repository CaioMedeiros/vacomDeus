# Trabalho 3B – API REST + Publish-Subscribe via SSE
## Sistemas Distribuídos – QXD0043 | UFC Quixadá

TL;DR
Continuação do Trabalho 3. O sistema de estoque REST ganhou um
Broker Pub-Sub embutido no spring boot usando server-sent events (SSE).
Nenhum socket manual foi criado — toda comunicação é HTTP.

1. Justificativa da Escolha 

O sistema de gerenciamento de estoque de suplementos foi implementado como uma API REST em Spring Boot, com clientes em Python e JavaScript. Nesse modelo, toda interação é síncrona e iniciada pelo cliente: para saber que um produto foi adicionado ou que um lote está próximo do vencimento, o cliente precisaria fazer polling contínuo — consultando repetidamente os mesmos endpoints. Isso é ineficiente, eleva desnecessariamente a carga do servidor e não reflete a natureza reativa de um sistema de estoque real. 

A abordagem Publish-Subscribe foi escolhida para resolver exatamente esse problema. Em vez de o cliente perguntar continuamente ao servidor "o que mudou?", o servidor notifica proativamente todos os interessados cada vez que algo relevante acontece. O padrão é natural para o domínio: múltiplos atores — sistemas de alerta, painéis de controle, módulos de auditoria — precisam reagir a eventos do estoque sem que o servidor conheça ou gerencie individualmente cada um deles. 

As demais opções foram descartadas por razões objetivas. Comunicação em Grupo (Opção A) pressupõe participantes iguais que precisam de consenso ou replicação, o que não se aplica aqui. Filas de Mensagens (Opção C) são adequadas para pipelines de processamento assíncrono com persistência durável em disco, adicionando complexidade desnecessária para notificações de eventos leves. Espaço de Tuplas (Opção D) exige que o consumidor conheça a estrutura exata dos dados para realizar buscas, criando acoplamento pelo conteúdo — incompatível com o desacoplamento que se busca. 

A escolha específica de Server-Sent Events (SSE) como mecanismo de entrega foi determinada pelo contexto do Trabalho 3A: o sistema já usa HTTP como único protocolo de comunicação, e SSE é um padrão HTTP nativo (especificado no HTML5), sem necessidade de bibliotecas adicionais no servidor. Clientes Python e JavaScript suportam SSE de forma nativa ou com pacotes mínimos, mantendo a consistência com as linguagens já adotadas e sem violar a restrição de não usar sockets manuais. 

Propriedade 

Como é atendida no sistema 

Desacoplamento Espacial 

SuplementoService e LoteService chamam apenas broker.publicar(topico, payload). Não conhecem IP, porta, quantidade ou identidade de nenhum subscriber. Novos clientes podem assinar sem qualquer mudança no servidor. 

Desacoplamento Temporal 

O EventBroker persiste todos os eventos em um histórico por tópico. Ao conectar em GET /api/eventos/subscribe/{topico}, o cliente recebe automaticamente os eventos gerados durante sua ausência (replay). 

Integração com REST 

SSE é um endpoint GET padrão. Clientes Python e JavaScript usam as mesmas bibliotecas HTTP já empregadas para as chamadas REST, sem abrir conexões de protocolo diferente. 

 

2. Análise: Overhead, Flexibilidade e Mitigações 

2.1 Flexibilidade introduzida 

A integração do EventBroker transformou o sistema de um modelo puramente requisição-resposta para um modelo orientado a eventos, sem substituir o ReST existente — os dois coexistem. As melhorias arquiteturais concretas foram: 

 

Notificação em tempo real: clientes recebem eventos como SUPLEMENTO_ADICIONADO, LOTE_ADICIONADO e ALERTA_VENCIMENTO imediatamente, sem polling. 

Extensibilidade sem modificação: novos subscribers (painel web, sistema de alertas, auditoria) se conectam ao endpoint SSE sem qualquer alteração no servidor. 

Resiliência a desconexões: o histórico em memória por tópico garante que eventos gerados durante a ausência de um subscriber sejam entregues na reconexão. 

Simplicidade de implantação: o broker é um bean Spring (@Component) no mesmo processo do servidor REST — não há processo separado para gerenciar. 

 

2.2 Overhead introduzido 

A presença do EventBroker adiciona custo em três dimensões principais: 

 

Dimensão 

Impacto no sistema 

Magnitude 

Conexões HTTP persistentes 

Cada subscriber mantém uma conexão HTTP aberta por tópico assinado. Com 5 tópicos e N clientes, o servidor mantém 5×N conexões simultâneas. 

5×N threads/fds por cliente 

Latência por operação 

A cada operação REST que gera evento, o SseEmitter.send() é chamado de forma síncrona para todos os subscribers ativos antes de retornar ao cliente. 

Proporcional a N subscribers 

Memória do histórico 

Todos os eventos são acumulados em filas em memória por tópico sem TTL. Em operação contínua por dias, o consumo cresce indefinidamente. 

Proporcional ao volume total de eventos 

Complexidade do fluxo 

Uma operação como adicionarSuplemento agora percorre: Controller → Service → Repository → EventBroker → N×SseEmitter, em vez de apenas Controller → Service → Repository. 

Código mais difícil de rastrear em caso de falha 

 

Para o domínio do estoque de suplementos — operações pontuais por um operador humano, frequência baixa, poucos clientes simultâneos — todos esses impactos são desprezíveis na prática. O custo de envio SSE síncrono é medido em microssegundos para conexões locais, e o histórico em memória de um sistema com dezenas de operações diárias não representa pressão relevante para a JVM. 

2.3 Estratégias de mitigação 

Para cenários de maior escala, as seguintes estratégias eliminam ou reduzem os impactos descritos: 

 

Publicação assíncrona: substituir broker.publicar() síncrono por envio em thread separada (ex.: @Async do Spring ou um ExecutorService). A operação REST retorna imediatamente ao cliente, e o broadcast SSE ocorre em paralelo, eliminando o acoplamento de latência. 

TTL no histórico: implementar expiração por tempo (ex.: descartar eventos com mais de 24 horas) ou por volume (manter apenas os N eventos mais recentes por tópico), controlando o crescimento de memória. 

Multiplexação de tópicos: em vez de uma conexão SSE por tópico, expor um único endpoint /api/eventos/subscribe que entrega eventos de todos os tópicos assinados numa só conexão, reduzindo de 5×N para N conexões. 

Heartbeat periódico: enviar um comentário SSE (: keep-alive) a cada 15–30 segundos para manter conexões ativas em ambientes com proxies reversos que fecham conexões ociosas, evitando reconexões desnecessárias. 

Persistência seletiva: para tópicos críticos como ALERTA_VENCIMENTO, gravar eventos em arquivo de log ou banco de dados. Isso garante que não sejam perdidos mesmo em caso de reinicialização do servidor. 

 

2.4 Conclusão 

A adição do Pub-Sub via SSE ao sistema REST representa uma evolução arquitetural precisa e bem ajustada ao problema. O mecanismo escolhido — SSE sobre HTTP — mantém a homogeneidade de protocolo do projeto (tudo HTTP, sem sockets manuais), é suportado nativamente por Python e JavaScript, e entrega as propriedades de desacoplamento espacial e temporal exigidas. O custo em complexidade e uso de recursos é baixo para o domínio em questão e controlável por técnicas bem estabelecidas caso o sistema precise escalar. 

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

## Objetos Distribuídos (mínimo 3)

| # | Classe | Papel |
|---|--------|-------|
| 1 | `EstoqueRepository` | Repositório central em memória |
| 2 | `SuplementoService` | CRUD + publica eventos de suplemento |
| 3 | `LoteService`       | Gestão de lotes + publica alertas de vencimento |
