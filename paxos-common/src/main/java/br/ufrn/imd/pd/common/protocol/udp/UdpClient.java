package br.ufrn.imd.pd.common.protocol.udp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpClient implements CommunicationClient {

    private static final Logger logger = LoggerFactory.getLogger(UdpClient.class);

    private static final int TIMEOUT_MS = 3000;
    private static final int MAX_RETRIES = 3;
    private static final int MAX_PACKET_SIZE = 65507;

    private final String host;
    private final int port;
    private final ExecutorService executor;

    public UdpClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "udp-client-" + host + ":" + port);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public CompletableFuture<MessageEnvelope> send(MessageEnvelope request) {
        return CompletableFuture.supplyAsync(() -> doSend(request), executor);
    }

    private MessageEnvelope doSend(MessageEnvelope request) {
        byte[] requestBytes = UdpJsonCodec.encode(request).getBytes(StandardCharsets.UTF_8);
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT_MS);

                InetAddress serverAddress = InetAddress.getByName(host);
                socket.send(new DatagramPacket(requestBytes, requestBytes.length, serverAddress, port));

                byte[] buffer = new byte[MAX_PACKET_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);

                String raw = new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.UTF_8);
                return UdpJsonCodec.decode(raw);

            } catch (SocketTimeoutException e) {
                logger.warn("Timeout tentativa {}/{} para {}:{}", attempt, MAX_RETRIES, host, port);
                lastException = e;
            } catch (Exception e) {
                logger.error("Erro tentativa {}/{} para {}:{}: {}", attempt, MAX_RETRIES, host, port, e.getMessage());
                lastException = e;
            }
        }

        logger.error("Falha apos {} tentativas para {}:{}", MAX_RETRIES, host, port, lastException);
        return MessageEnvelope.response(503, "{\"error\":\"Servico indisponivel: " + host + ":" + port + "\"}");
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
