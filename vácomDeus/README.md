# Trabalho 2 – RMI: Sistema de Estoque de Suplementos

## Estrutura do Projeto

```
src/
├── interfaces/
│   └── Validavel.java              # Interface com estaValido(), getDataVencimento(), getDiasParaVencer()
├── modelo/
│   ├── Suplemento.java             # Classe abstrata base (implements Validavel, Serializable)
│   ├── WheyProtein.java            # extends Suplemento — "é-um"
│   ├── Creatina.java               # extends Suplemento — "é-um"
│   ├── Vitaminas.java              # extends Suplemento — "é-um"
│   └── PreTreino.java              # extends Suplemento — "é-um"
├── estoque/
│   ├── Estoque.java                # "tem-um" List<Suplemento>
│   └── Lote.java                   # "tem-um" em Suplemento (List<Lote>)
├── rmi/
│   ├── common/
│   │   ├── RemoteObjectRef.java    # Referência ao objeto remoto (host, porta, nome)
│   │   ├── RequestMessage.java     # Mensagem de requisição (messageType, requestId, objectRef, methodId, arguments)
│   │   ├── ReplyMessage.java       # Mensagem de resposta (messageType, requestId, status, result)
│   │   └── EstoqueService.java     # Interface com os 7 métodos remotos
│   ├── server/
│   │   ├── EstoqueServer.java      # Ponto de entrada do servidor
│   │   ├── RequestReplyProtocol.java  # getRequest() e sendReply()
│   │   ├── Dispatcher.java         # Despacha requisição para o método correto
│   │   └── EstoqueSkeletonImpl.java   # Skeleton: implementação real dos métodos
│   └── client/
│       ├── CommunicationModule.java   # doOperation() — envia req e recebe reply
│       └── EstoqueProxy.java          # Proxy/stub do cliente
├── util/
│   └── JsonSerializer.java         # Representação externa de dados (JSON manual)
├── ClienteMain.java                # Menu interativo do cliente
└── TesteIntegrado.java             # Teste automatizado (servidor em thread)
```

## Requisitos atendidos

| Requisito | Como foi atendido |
|-----------|------------------|
| Mín. 4 classes entidade | Suplemento, WheyProtein, Creatina, Vitaminas, PreTreino, Lote, Estoque |
| Mín. 2 composições "tem-um" | Estoque tem List\<Suplemento\>; Suplemento tem List\<Lote\> |
| Mín. 2 composições "é-um" | WheyProtein, Creatina, Vitaminas, PreTreino estendem Suplemento |
| Mín. 4 métodos remotos | 7 métodos: adicionarSuplemento, adicionarLote, buscarSuplemento, listarEstoque, listarVencidos, listarProximosAoVencer, removerSuplemento |
| Passagem por referência | RemoteObjectRef identifica o objeto remoto no servidor |
| Passagem por valor | Todos os argumentos/resultados serializados em JSON (JsonSerializer) |
| Representação externa de dados | JSON construído manualmente em JsonSerializer.java |
| Protocolo requisição-resposta | RequestMessage + ReplyMessage + RequestReplyProtocol + CommunicationModule |
| doOperation() | CommunicationModule.doOperation(RemoteObjectRef, methodId, byte[]) |
| getRequest() | RequestReplyProtocol.getRequest() |
| sendReply() | RequestReplyProtocol.sendReply(reply, clientHost, clientPort) |
| Sem sockets diretos no cliente | Cliente usa apenas EstoqueProxy → CommunicationModule |