package br.ufrn.imd.pd.common.protocol.tcp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TcpClient implements CommunicationClient {

    private static final Logger logger = LoggerFactory.getLogger(TcpClient.class);
    private static final int TIMEOUT_MS = 5000;
    private static final int POOL_SIZE = 16;

    private final String host;
    private final int port;
    private final ExecutorService executor;
    private final LinkedBlockingQueue<PooledConnection> pool;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.pool = new LinkedBlockingQueue<>(POOL_SIZE);
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
        // Tenta reutilizar conexão do pool
        PooledConnection conn = pool.poll();
        if (conn != null) {
            if (!conn.socket.isClosed()) {
                try {
                    TcpHttpCodec.write(request, conn.socket.getOutputStream());
                    MessageEnvelope response = TcpHttpCodec.read(conn.reader);
                    returnToPool(conn);
                    return response;
                } catch (IOException e) {
                    closeQuietly(conn); // conexão estava morta
                }
            } else {
                closeQuietly(conn);
            }
        }

        // Conexão nova
        try {
            Socket socket = new Socket(host, port);
            socket.setSoTimeout(TIMEOUT_MS);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            TcpHttpCodec.write(request, socket.getOutputStream());
            MessageEnvelope response = TcpHttpCodec.read(reader);
            returnToPool(new PooledConnection(socket, reader));
            return response;
        } catch (Exception e) {
            logger.error("Erro ao enviar para {}:{}: {}", host, port, e.getMessage());
            return MessageEnvelope.response(503, "{\"error\":\"Servico indisponivel: " + host + ":" + port + "\"}");
        }
    }

    private void returnToPool(PooledConnection conn) {
        if (!pool.offer(conn)) {
            closeQuietly(conn);
        }
    }

    private void closeQuietly(PooledConnection conn) {
        try { conn.socket.close(); } catch (IOException ignored) {}
    }

    @Override
    public void close() {
        executor.shutdown();
        PooledConnection conn;
        while ((conn = pool.poll()) != null) {
            closeQuietly(conn);
        }
    }

    private record PooledConnection(Socket socket, BufferedReader reader) {}
}
