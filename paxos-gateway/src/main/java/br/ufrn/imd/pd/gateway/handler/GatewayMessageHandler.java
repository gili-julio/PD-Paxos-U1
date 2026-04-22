package br.ufrn.imd.pd.gateway.handler;

import br.ufrn.imd.pd.common.model.ServiceInfo;
import br.ufrn.imd.pd.common.model.paxos.PaxosRelayBatch;
import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.gateway.registry.InMemoryServiceRegistry;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GatewayMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayMessageHandler.class);
    private static final long RELAY_TIMEOUT_S = 5;

    private final InMemoryServiceRegistry registry;
    private final CommunicationStrategy strategy;
    private final Gson gson;
    private final AtomicInteger proposerRoundRobin = new AtomicInteger(0);
    private final ConcurrentHashMap<String, CommunicationClient> clientCache = new ConcurrentHashMap<>();

    public GatewayMessageHandler(InMemoryServiceRegistry registry, CommunicationStrategy strategy) {
        this.registry = registry;
        this.strategy = strategy;
        this.gson = new Gson();
    }

    private CommunicationClient clientFor(String host, int port) {
        return clientCache.computeIfAbsent(host + ":" + port, k -> strategy.createClient(host, port));
    }

    @Override
    public MessageEnvelope handle(MessageEnvelope request) {
        String method = request.getMethod();
        String path = request.getPath();

        if ("POST".equals(method) && "/registry".equals(path)) {
            return handleRegister(request);
        }
        if ("POST".equals(method) && "/heartbeat".equals(path)) {
            return handleHeartbeat(request);
        }
        if ("POST".equals(method) && "/paxos/relay".equals(path)) {
            return handlePaxosRelay(request);
        }
        if ("GET".equals(method) && path != null && path.startsWith("/kv/")) {
            return handleReadForward(request);
        }
        if ("PUT".equals(method) && path != null && path.startsWith("/kv/")) {
            return handleWriteForward(request);
        }
        if ("GET".equals(method) && "/services".equals(path)) {
            return handleListServices();
        }

        return MessageEnvelope.response(404, "{\"error\":\"Rota nao encontrada: " + method + " " + path + "\"}");
    }

    private MessageEnvelope handleRegister(MessageEnvelope request) {
        try {
            ServiceInfo info = gson.fromJson(request.getBody(), ServiceInfo.class);
            registry.register(info);
            return MessageEnvelope.response(200, "{\"status\":\"registered\",\"id\":\"" + info.getId() + "\"}");
        } catch (Exception e) {
            logger.error("Erro ao registrar servico", e);
            return MessageEnvelope.response(400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private MessageEnvelope handleHeartbeat(MessageEnvelope request) {
        try {
            String host = request.getHeader("X-Service-Host");
            String portStr = request.getHeader("X-Service-Port");
            if (host == null || portStr == null) {
                return MessageEnvelope.response(400, "{\"error\":\"Headers X-Service-Host e X-Service-Port obrigatorios\"}");
            }
            registry.updateHeartbeat(host, Integer.parseInt(portStr));
            return MessageEnvelope.response(200, "{\"status\":\"ok\"}");
        } catch (Exception e) {
            return MessageEnvelope.response(400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private MessageEnvelope handlePaxosRelay(MessageEnvelope request) {
        String action = request.getHeader("X-Paxos-Action");
        if (action == null) {
            return MessageEnvelope.response(400, "{\"error\":\"Header X-Paxos-Action obrigatorio\"}");
        }

        List<ServiceInfo> acceptors = registry.getActive("acceptor");
        if (acceptors.isEmpty()) {
            return MessageEnvelope.response(503, "{\"error\":\"Nenhum acceptor ativo\"}");
        }

        String targetPath = "PREPARE".equalsIgnoreCase(action) ? "/paxos/prepare" : "/paxos/accept";
        logger.info("[RELAY] {} -> {} acceptor(s)", action, acceptors.size());

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (ServiceInfo acceptor : acceptors) {
            futures.add(relayToAcceptor(acceptor, targetPath, request.getBody()));
        }

        List<String> responses = new ArrayList<>();
        for (CompletableFuture<String> future : futures) {
            try {
                String body = future.get(RELAY_TIMEOUT_S, TimeUnit.SECONDS);
                if (body != null) responses.add(body);
            } catch (Exception e) {
                logger.warn("[RELAY] Timeout/erro em acceptor: {}", e.getMessage());
            }
        }

        PaxosRelayBatch batch = new PaxosRelayBatch(acceptors.size(), responses);
        return MessageEnvelope.response(200, gson.toJson(batch));
    }

    private CompletableFuture<String> relayToAcceptor(ServiceInfo acceptor, String path, String body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CommunicationClient client = clientFor(acceptor.getHost(), acceptor.getPort());
                MessageEnvelope msg = MessageEnvelope.request("POST", path, body);
                MessageEnvelope response = client.send(msg).get(RELAY_TIMEOUT_S, TimeUnit.SECONDS);
                return response.getBody();
            } catch (Exception e) {
                logger.warn("[RELAY] Falha ao contatar acceptor {}:{} - {}", acceptor.getHost(), acceptor.getPort(), e.getMessage());
                return null;
            }
        });
    }

    private MessageEnvelope handleReadForward(MessageEnvelope request) {
        List<ServiceInfo> acceptors = registry.getActive("acceptor");
        if (acceptors.isEmpty()) {
            return MessageEnvelope.response(503, "{\"error\":\"Nenhum acceptor ativo\"}");
        }
        ServiceInfo target = acceptors.get(Math.abs(proposerRoundRobin.getAndIncrement() % acceptors.size()));
        return forward(target, request);
    }

    private MessageEnvelope handleWriteForward(MessageEnvelope request) {
        List<ServiceInfo> proposers = registry.getActive("proposer");
        if (proposers.isEmpty()) {
            return MessageEnvelope.response(503, "{\"error\":\"Nenhum proposer ativo\"}");
        }
        ServiceInfo target = proposers.get(Math.abs(proposerRoundRobin.getAndIncrement() % proposers.size()));
        return forward(target, request);
    }

    private MessageEnvelope forward(ServiceInfo target, MessageEnvelope request) {
        try {
            CommunicationClient client = clientFor(target.getHost(), target.getPort());
            MessageEnvelope response = client.send(request).get(10, TimeUnit.SECONDS);
            return response;
        } catch (Exception e) {
            logger.error("Erro ao encaminhar para {}:{}", target.getHost(), target.getPort(), e);
            return MessageEnvelope.response(503, "{\"error\":\"Servico indisponivel: " + target.getId() + "\"}");
        }
    }

    private MessageEnvelope handleListServices() {
        List<ServiceInfo> all = registry.getAll();
        return MessageEnvelope.response(200, gson.toJson(all));
    }
}
