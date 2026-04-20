package br.ufrn.imd.pd.common.protocol.tcp;

import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpServer implements CommunicationServer {

    private static final Logger logger = LoggerFactory.getLogger(TcpServer.class);
    private static final int THREAD_POOL_SIZE = 32;

    private final int port;
    private final MessageHandler handler;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TcpServer(int port, MessageHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        running.set(true);

        Thread acceptThread = new Thread(this::acceptLoop, "tcp-server-" + port);
        acceptThread.setDaemon(true);
        acceptThread.start();

        logger.info("TcpServer escutando na porta {}", port);
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket client = serverSocket.accept();
                threadPool.submit(() -> handleConnection(client));
            } catch (IOException e) {
                if (running.get()) logger.error("Erro ao aceitar conexao TCP", e);
            }
        }
    }

    private void handleConnection(Socket client) {
        try (client) {
            MessageEnvelope request = TcpHttpCodec.read(client.getInputStream());

            MessageEnvelope response;
            try {
                response = handler.handle(request);
            } catch (Exception e) {
                logger.error("Erro ao processar requisicao {} {}", request.getMethod(), request.getPath(), e);
                response = MessageEnvelope.response(500, "{\"error\":\"" + e.getMessage() + "\"}");
            }

            TcpHttpCodec.write(response, client.getOutputStream());
        } catch (IOException e) {
            logger.error("Erro na conexao TCP de {}", client.getInetAddress(), e);
        }
    }

    @Override
    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            logger.warn("Erro ao fechar ServerSocket", e);
        }
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) threadPool.shutdownNow();
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("TcpServer porta {} encerrado", port);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
