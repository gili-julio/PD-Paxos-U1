package br.ufrn.imd.pd.common.protocol.grpc;

import br.ufrn.imd.pd.common.proto.GenericRequest;
import br.ufrn.imd.pd.common.proto.GenericResponse;
import br.ufrn.imd.pd.common.proto.PaxosNodeServiceGrpc;
import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GrpcClient implements CommunicationClient {

    private static final Logger logger = LoggerFactory.getLogger(GrpcClient.class);

    private final String host;
    private final int port;
    private final ManagedChannel channel;
    private final PaxosNodeServiceGrpc.PaxosNodeServiceBlockingStub stub;
    private final ExecutorService executor;

    public GrpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = PaxosNodeServiceGrpc.newBlockingStub(channel);
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "grpc-client-" + host + ":" + port);
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public CompletableFuture<MessageEnvelope> send(MessageEnvelope request) {
        return CompletableFuture.supplyAsync(() -> doSend(request), executor);
    }

    private MessageEnvelope doSend(MessageEnvelope request) {
        try {
            GenericRequest grpcRequest = toGrpcRequest(request);
            GenericResponse response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).exchange(grpcRequest);
            return toEnvelope(response);
        } catch (Exception e) {
            logger.error("Erro gRPC para {}:{}: {}", host, port, e.getMessage());
            return MessageEnvelope.response(503, "{\"error\":\"Servico indisponivel: " + host + ":" + port + "\"}");
        }
    }

    private GenericRequest toGrpcRequest(MessageEnvelope envelope) {
        GenericRequest.Builder builder = GenericRequest.newBuilder();

        if (envelope.getMethod() != null) builder.setMethod(envelope.getMethod());
        if (envelope.getPath() != null) builder.setPath(envelope.getPath());
        if (envelope.getBody() != null) builder.setBody(envelope.getBody());
        if (envelope.getHeaders() != null) {
            for (Map.Entry<String, String> entry : envelope.getHeaders().entrySet()) {
                builder.putHeaders(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }

    private MessageEnvelope toEnvelope(GenericResponse response) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setStatusCode(response.getStatusCode());
        envelope.setBody(response.getBody().isEmpty() ? null : response.getBody());
        envelope.setHeaders(new HashMap<>(response.getHeadersMap()));
        return envelope;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            channel.shutdown().awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
