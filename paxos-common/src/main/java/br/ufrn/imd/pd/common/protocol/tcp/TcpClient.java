package br.ufrn.imd.pd.common.protocol.tcp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpClient implements CommunicationClient {

    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    private static final int TIMEOUT_MS = 5000;

    private final String host;
    private final int port;
    private final ExecutorService executor;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "tcp-client-" + host + ":" + port);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public CompletableFuture<MessageEnvelope> send(MessageEnvelope request) {
        return CompletableFuture.supplyAsync(() -> doSend(request), executor);
    }

    private MessageEnvelope doSend(MessageEnvelope request) {
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(TIMEOUT_MS);
            TcpHttpCodec.write(request, socket.getOutputStream());
            return TcpHttpCodec.read(socket.getInputStream());
        } catch (Exception e) {
            logger.error("Erro ao enviar para {}:{}: {}", host, port, e.getMessage());
            return MessageEnvelope.response(503, "{\"error\":\"Servico indisponivel: " + host + ":" + port + "\"}");
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
