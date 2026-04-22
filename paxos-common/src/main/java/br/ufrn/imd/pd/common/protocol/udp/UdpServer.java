package br.ufrn.imd.pd.common.protocol.udp;

import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UdpServer implements CommunicationServer {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private static final int MAX_PACKET_SIZE = 65507;
    private static final int SOCKET_BUFFER_SIZE = 4 * 1024 * 1024; // 4MB

    private final int port;
    private final MessageHandler handler;

    private DatagramSocket socket;
    private ExecutorService threadPool;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger threadCount = new AtomicInteger(0);

    public UdpServer(int port, MessageHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void start() throws IOException {
        socket = new DatagramSocket(port);
        socket.setReceiveBufferSize(SOCKET_BUFFER_SIZE);
        socket.setSendBufferSize(SOCKET_BUFFER_SIZE);
        threadPool = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "udp-worker-" + port + "-" + threadCount.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
        running.set(true);

        Thread listenerThread = new Thread(this::receiveLoop, "udp-server-" + port);
        listenerThread.setDaemon(true);
        listenerThread.start();

        logger.info("UdpServer escutando na porta {}", port);
    }

    private void receiveLoop() {
        byte[] buffer = new byte[MAX_PACKET_SIZE];

        while (running.get()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                InetAddress sender = packet.getAddress();
                int senderPort = packet.getPort();
                String raw = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                threadPool.submit(() -> processPacket(raw, sender, senderPort));
            } catch (IOException e) {
                if (running.get()) logger.error("Erro ao receber pacote UDP", e);
            }
        }
    }

    private void processPacket(String raw, InetAddress sender, int senderPort) {
        MessageEnvelope request;
        try {
            request = UdpJsonCodec.decode(raw);
        } catch (Exception e) {
            logger.error("Erro ao decodificar pacote de {}:{}", sender, senderPort, e);
            return;
        }

        MessageEnvelope response;
        try {
            response = handler.handle(request);
        } catch (Exception e) {
            logger.error("Erro ao processar requisicao {} {}", request.getMethod(), request.getPath(), e);
            response = MessageEnvelope.response(500, "{\"error\":\"" + e.getMessage() + "\"}");
        }

        byte[] responseBytes = UdpJsonCodec.encode(response).getBytes(StandardCharsets.UTF_8);

        synchronized (socket) {
            try {
                socket.send(new DatagramPacket(responseBytes, responseBytes.length, sender, senderPort));
            } catch (IOException e) {
                logger.error("Erro ao enviar resposta UDP para {}:{}", sender, senderPort, e);
            }
        }
    }

    @Override
    public void stop() {
        running.set(false);
        if (socket != null && !socket.isClosed()) socket.close();
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) threadPool.shutdownNow();
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("UdpServer porta {} encerrado", port);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
