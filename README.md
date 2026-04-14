# PD-Paxos-U1

Trabalho da disciplina **Programação Distribuída** (UFRN - IMD), Unidade 1, implementado por meio do padrão **Paxos** descrito no livro *"Patterns of Distributed Systems"* (Addison-Wesley, 2024) de Unmesh Joshi.

## Padrão Paxos

O **Paxos** é um algoritmo de consenso distribuído que permite que um conjunto de nós (nodes) concordem sobre um único valor, mesmo na presença de falhas. O algoritmo opera em duas fases:

- **Fase 1:** O Proposer envia uma mensagem `Prepare(n)` com um número de proposta `n` para todos os Acceptors. Cada Acceptor responde com `Promise(n)` se `n` for maior que qualquer proposta já prometida, incluindo o valor previamente aceito (se houver).
- **Fase 2:** Se o Proposer recebe promessas de uma maioria (quorum), ele envia `Accept(n, valor)` para todos os Acceptors. Cada Acceptor aceita a proposta se ainda não tiver prometido um número maior.

O consenso é alcançado quando uma maioria de Acceptors aceita a mesma proposta.

## Aplicação: Key-Value Store Distribuído

A aplicação implementa um **armazenamento chave-valor distribuído** onde operações de escrita (PUT) passam pelo consenso Paxos para garantir consistência entre os nós.

- **PUT /kv/{chave}** - Escreve um valor (passa por consenso Paxos)
- **GET /kv/{chave}** - Lê o valor mais recente de um Acceptor

## Arquitetura

O sistema possui **3 componentes**:

### 1. API Gateway (1 instância)
- Recebe requisições externas
- **Service Discovery**: componentes se registram ao iniciar, informando IP e porta
- **Heartbeat**: monitora disponibilidade dos componentes internos (padrão Observer)
- **Roteamento**: encaminha requisições para Proposers e relay de mensagens Paxos
- Toda comunicação interna passa pelo Gateway

### 2. Proposer (2+ instâncias, stateless)
- Recebe requisições de escrita do Gateway
- Inicia rodadas Paxos (Fase 1 e Fase 2)
- Gera números de proposta únicos (compostos: sequência + proposerId)
- Verifica quorum antes de confirmar escrita

### 3. Acceptor (3+ instâncias, stateful)
- Participa do consenso Paxos votando em propostas
- Armazena o estado: maior proposta prometida, valor aceito
- Mantém o key-value store com os valores commitados
- Replicação garantida pelo próprio algoritmo Paxos

## Protocolos de Comunicação

O sistema suporta **3 modos de protocolo**, selecionados no startup via parâmetro `--protocol`:

| Modo   | Transporte | Aplicação      |
|--------|-----------|----------------|
| `udp`  | UDP       | HTTP-like      |
| `tcp`  | TCP       | HTTP/1.1       |
| `grpc` | HTTP/2    | gRPC (puro)    |

Todos os modos existem no mesmo código-fonte, usando o padrão **Strategy** para seleção em runtime.

## Design Patterns (GoF) Utilizados

| Padrão     | Onde                          | Propósito                                    |
|------------|-------------------------------|----------------------------------------------|
| Strategy   | CommunicationStrategy         | Seleção de protocolo (UDP/TCP/gRPC) em runtime |
| Factory    | CommunicationFactory          | Instanciar a strategy correta                |
| Observer   | HeartbeatMonitor / ServiceListener | Gateway monitora saude dos servicos       |
| Singleton  | AppConfig                     | Configuração única por JVM                   |

## Estrutura do Projeto

```
PD-Paxos-U1/
  pom.xml                    # Parent POM
  paxos-common/              # Modelos, interfaces de protocolo, factory, proto
  paxos-gateway/             # API Gateway
  paxos-proposer/            # Proposer (lógica Paxos - proposição)
  paxos-acceptor/            # Acceptor (lógica Paxos - aceitação + KV store)
```

## Como Compilar

```bash
mvn clean package
```

## Como Executar

Todos os componentes recebem o protocolo como parâmetro. Exemplo com TCP:

```bash
# 1. Iniciar o API Gateway
java -jar paxos-gateway/target/paxos-gateway-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --protocol=tcp --port=8080

# 2. Iniciar Proposers (2 instâncias)
java -jar paxos-proposer/target/paxos-proposer-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --protocol=tcp --port=9001 --gateway=localhost:8080

java -jar paxos-proposer/target/paxos-proposer-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --protocol=tcp --port=9002 --gateway=localhost:8080

# 3. Iniciar Acceptors (3 instâncias para quorum)
java -jar paxos-acceptor/target/paxos-acceptor-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --protocol=tcp --port=10001 --gateway=localhost:8080

java -jar paxos-acceptor/target/paxos-acceptor-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --protocol=tcp --port=10002 --gateway=localhost:8080

java -jar paxos-acceptor/target/paxos-acceptor-1.0-SNAPSHOT-jar-with-dependencies.jar \
  --protocol=tcp --port=10003 --gateway=localhost:8080
```

Para usar outro protocolo, troque `--protocol=tcp` por `--protocol=udp` ou `--protocol=grpc`.
