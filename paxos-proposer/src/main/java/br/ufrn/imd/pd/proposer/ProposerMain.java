package br.ufrn.imd.pd.proposer;

import br.ufrn.imd.pd.common.config.AppConfig;
import br.ufrn.imd.pd.common.factory.CommunicationFactory;
import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.proposer.handler.ProposerMessageHandler;
import br.ufrn.imd.pd.proposer.paxos.PaxosProposerLogic;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProposerMain {

    private static final Logger logger = LoggerFactory.getLogger(ProposerMain.class);

    private static final int HEARTBEAT_INTERVAL_SECONDS = 5;
    private static final int REGISTER_RETRY_ATTEMPTS = 5;
    private static final int REGISTER_RETRY_DELAY_MS = 2000;

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);

        logger.info("=== Iniciando Proposer [protocolo={}, porta={}] ===", config.getProtocolType(), config.getPort());

        CommunicationStrategy strategy = CommunicationFactory.create(config.getProtocolType());

        PaxosProposerLogic paxosLogic = new PaxosProposerLogic(
                strategy, config.getGatewayHost(), config.getGatewayPort(), config.getPort());

        ProposerMessageHandler handler = new ProposerMessageHandler(paxosLogic);

        CommunicationServer server = strategy.createServer(config.getPort(), handler);
        try {
            server.start();
            logger.info("Proposer rodando na porta {}", config.getPort());
        } catch (IOException e) {
            logger.error("Falha ao iniciar Proposer na porta {}", config.getPort(), e);
            System.exit(1);
        }

        if (config.getGatewayHost() != null) {
            registrarNoGateway(strategy, config);

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "proposer-heartbeat");
                t.setDaemon(true);
                return t;
            });

            scheduler.scheduleAtFixedRate(
                    () -> enviarHeartbeat(strategy, config),
                    HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando Proposer porta {}...", config.getPort());
                scheduler.shutdown();
                server.stop();
            }));
        } else {
            logger.warn("Gateway nao configurado.");
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        }
    }

    private static void registrarNoGateway(CommunicationStrategy strategy, AppConfig config) {
        JsonObject body = new JsonObject();
        body.addProperty("serviceName", "proposer");
        body.addProperty("host", "localhost");
        body.addProperty("port", config.getPort());
        body.addProperty("protocol", config.getProtocolType().name());

        MessageEnvelope request = MessageEnvelope.request("POST", "/registry", body.toString());

        for (int attempt = 1; attempt <= REGISTER_RETRY_ATTEMPTS; attempt++) {
            try {
                CommunicationClient client = strategy.createClient(config.getGatewayHost(), config.getGatewayPort());
                MessageEnvelope response = client.send(request).get(5, TimeUnit.SECONDS);
                client.close();

                if (response.getStatusCode() == 200) {
                    logger.info("Proposer (porta {}) registrado no Gateway {}:{}", config.getPort(), config.getGatewayHost(), config.getGatewayPort());
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

    private static void enviarHeartbeat(CommunicationStrategy strategy, AppConfig config) {
        JsonObject body = new JsonObject();
        body.addProperty("serviceName", "proposer");
        body.addProperty("port", config.getPort());

        try {
            CommunicationClient client = strategy.createClient(config.getGatewayHost(), config.getGatewayPort());
            MessageEnvelope response = client.send(MessageEnvelope.request("POST", "/heartbeat", body.toString())).get(3, TimeUnit.SECONDS);
            client.close();

            if (response.getStatusCode() != 200) {
                logger.warn("Heartbeat nao confirmado (status {})", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.warn("Falha ao enviar heartbeat: {}", e.getMessage());
        }
    }
}
