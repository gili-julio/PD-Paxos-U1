package br.ufrn.imd.pd.gateway;

import br.ufrn.imd.pd.common.config.AppConfig;
import br.ufrn.imd.pd.common.factory.CommunicationFactory;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayMain {

    private static final Logger logger = LoggerFactory.getLogger(GatewayMain.class);

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);

        logger.info("Iniciando API Gateway na porta {} com protocolo {}",
                config.getPort(), config.getProtocolType());

        CommunicationStrategy strategy = CommunicationFactory.create(config.getProtocolType());

        // TODO: criar InMemoryServiceRegistry, HeartbeatMonitor, RequestRouter
        MessageHandler handler = request -> {
            logger.info("Requisicao recebida: {} {}", request.getMethod(), request.getPath());
            return MessageEnvelope.response(200, "{\"status\":\"ok\"}");
        };

        try {
            CommunicationServer server = strategy.createServer(config.getPort(), handler);
            server.start();

            logger.info("API Gateway rodando na porta {} [{}]",
                    config.getPort(), config.getProtocolType());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando API Gateway...");
                server.stop();
            }));
        } catch (Exception e) {
            logger.error("Erro ao iniciar API Gateway", e);
            System.exit(1);
        }
    }
}
