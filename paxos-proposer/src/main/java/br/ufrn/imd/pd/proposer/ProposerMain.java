package br.ufrn.imd.pd.proposer;

import br.ufrn.imd.pd.common.config.AppConfig;
import br.ufrn.imd.pd.common.factory.CommunicationFactory;
import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProposerMain {

    private static final Logger logger = LoggerFactory.getLogger(ProposerMain.class);

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);

        logger.info("Iniciando Proposer na porta {} com protocolo {}",
                config.getPort(), config.getProtocolType());

        CommunicationStrategy strategy = CommunicationFactory.create(config.getProtocolType());

        // TODO: criar PaxosProposerLogic, handlers
        MessageHandler handler = request -> {
            logger.info("Requisicao recebida no Proposer: {} {}", request.getMethod(), request.getPath());
            return MessageEnvelope.response(200, "{\"status\":\"ok\"}");
        };

        try {
            CommunicationServer server = strategy.createServer(config.getPort(), handler);
            server.start();

            // Registrar no Gateway
            if (config.getGatewayHost() != null) {
                CommunicationClient gatewayClient = strategy.createClient(
                        config.getGatewayHost(), config.getGatewayPort());

                MessageEnvelope registration = MessageEnvelope.request(
                        "REGISTER", "/registry",
                        "{\"serviceName\":\"proposer\",\"host\":\"localhost\",\"port\":" + config.getPort() + "}");
                gatewayClient.send(registration);

                logger.info("Registrado no Gateway {}:{}", config.getGatewayHost(), config.getGatewayPort());
            }

            logger.info("Proposer rodando na porta {} [{}]",
                    config.getPort(), config.getProtocolType());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando Proposer...");
                server.stop();
            }));
        } catch (Exception e) {
            logger.error("Erro ao iniciar Proposer", e);
            System.exit(1);
        }
    }
}
