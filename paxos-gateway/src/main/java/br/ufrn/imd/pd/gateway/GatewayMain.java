package br.ufrn.imd.pd.gateway;

import br.ufrn.imd.pd.common.config.AppConfig;
import br.ufrn.imd.pd.common.factory.CommunicationFactory;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.gateway.handler.GatewayMessageHandler;
import br.ufrn.imd.pd.gateway.observer.HeartbeatMonitor;
import br.ufrn.imd.pd.gateway.registry.InMemoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayMain {

    private static final Logger logger = LoggerFactory.getLogger(GatewayMain.class);

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);

        logger.info("Iniciando API Gateway na porta {} com protocolo {}",
                config.getPort(), config.getProtocolType());

        CommunicationStrategy strategy = CommunicationFactory.create(config.getProtocolType());

        InMemoryServiceRegistry registry = new InMemoryServiceRegistry();
        HeartbeatMonitor monitor = new HeartbeatMonitor(registry);
        GatewayMessageHandler handler = new GatewayMessageHandler(registry, strategy);

        try {
            CommunicationServer server = strategy.createServer(config.getPort(), handler);
            server.start();
            monitor.start();

            logger.info("API Gateway rodando na porta {} [{}]", config.getPort(), config.getProtocolType());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando API Gateway...");
                monitor.stop();
                server.stop();
            }));

            Thread.currentThread().join();
        } catch (Exception e) {
            logger.error("Erro ao iniciar API Gateway", e);
            System.exit(1);
        }
    }
}
