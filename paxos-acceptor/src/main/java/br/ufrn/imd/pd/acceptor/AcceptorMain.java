package br.ufrn.imd.pd.acceptor;

import br.ufrn.imd.pd.acceptor.handler.AcceptorMessageHandler;
import br.ufrn.imd.pd.acceptor.paxos.AcceptorState;
import br.ufrn.imd.pd.acceptor.paxos.PaxosAcceptorLogic;
import br.ufrn.imd.pd.acceptor.store.InMemoryKeyValueStore;
import br.ufrn.imd.pd.acceptor.store.KeyValueStore;
import br.ufrn.imd.pd.common.config.AppConfig;
import br.ufrn.imd.pd.common.factory.CommunicationFactory;
import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AcceptorMain {

    private static final Logger logger = LoggerFactory.getLogger(AcceptorMain.class);

    private static final int HEARTBEAT_INTERVAL_SECONDS = 5;
    private static final int REGISTER_RETRY_ATTEMPTS = 5;
    private static final int REGISTER_RETRY_DELAY_MS = 2000;

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);
        String acceptorId = "acceptor@localhost:" + config.getPort();

        logger.info("=== Iniciando Acceptor [id={}, protocolo={}, porta={}] ===",
                acceptorId, config.getProtocolType(), config.getPort());

        CommunicationStrategy strategy = CommunicationFactory.create(config.getProtocolType());

        AcceptorState acceptorState = new AcceptorState();
        KeyValueStore kvStore = new InMemoryKeyValueStore();
        PaxosAcceptorLogic paxosLogic = new PaxosAcceptorLogic(acceptorId, acceptorState, kvStore);
        AcceptorMessageHandler handler = new AcceptorMessageHandler(paxosLogic, kvStore);

        CommunicationServer server = strategy.createServer(config.getPort(), handler);
        try {
            server.start();
            logger.info("Acceptor rodando na porta {}", config.getPort());
        } catch (IOException e) {
            logger.error("Falha ao iniciar Acceptor na porta {}", config.getPort(), e);
            System.exit(1);
        }

        if (config.getGatewayHost() != null) {
            registrarNoGateway(strategy, config, acceptorId);

            CommunicationClient heartbeatClient = strategy.createClient(config.getGatewayHost(), config.getGatewayPort());

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "acceptor-heartbeat-" + config.getPort());
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(
                    () -> enviarHeartbeat(heartbeatClient, config),
                    HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando Acceptor porta {}...", config.getPort());
                scheduler.shutdown();
                heartbeatClient.close();
                server.stop();
            }));
        } else {
            logger.warn("Gateway nao configurado.");
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        }

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void registrarNoGateway(CommunicationStrategy strategy, AppConfig config, String acceptorId) {
        JsonObject body = new JsonObject();
        body.addProperty("serviceName", "acceptor");
        body.addProperty("host", "localhost");
        body.addProperty("port", config.getPort());
        body.addProperty("protocol", config.getProtocolType().name());
        body.addProperty("id", acceptorId);

        MessageEnvelope request = MessageEnvelope.request("POST", "/registry", body.toString());

        for (int attempt = 1; attempt <= REGISTER_RETRY_ATTEMPTS; attempt++) {
            try {
                CommunicationClient client = strategy.createClient(config.getGatewayHost(), config.getGatewayPort());
                MessageEnvelope response = client.send(request).get(5, TimeUnit.SECONDS);
                client.close();

                if (response.getStatusCode() == 200) {
                    logger.info("Acceptor '{}' registrado no Gateway {}:{}", acceptorId, config.getGatewayHost(), config.getGatewayPort());
                    return;
                }
                logger.warn("Registro falhou (status {}), tentativa {}/{}", response.getStatusCode(), attempt, REGISTER_RETRY_ATTEMPTS);
            } catch (Exception e) {
                logger.warn("Erro ao registrar, tentativa {}/{}: {}", attempt, REGISTER_RETRY_ATTEMPTS, e.getMessage());
            }

            if (attempt < REGISTER_RETRY_ATTEMPTS) {
                try { Thread.sleep(REGISTER_RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }

        logger.error("Nao foi possivel registrar no Gateway apos {} tentativas.", REGISTER_RETRY_ATTEMPTS);
    }

    private static void enviarHeartbeat(CommunicationClient client, AppConfig config) {
        try {
            MessageEnvelope msg = MessageEnvelope.request("POST", "/heartbeat", null);
            msg.addHeader("X-Service-Host", "localhost");
            msg.addHeader("X-Service-Port", String.valueOf(config.getPort()));
            MessageEnvelope response = client.send(msg).get(8, TimeUnit.SECONDS);
            if (response.getStatusCode() != 200) {
                logger.warn("Heartbeat nao confirmado (status {})", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.warn("Falha ao enviar heartbeat: {}", e.getMessage());
        }
    }
}
