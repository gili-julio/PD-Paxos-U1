package br.ufrn.imd.pd.common.protocol.grpc;

import br.ufrn.imd.pd.common.proto.GenericRequest;
import br.ufrn.imd.pd.common.proto.GenericResponse;
import br.ufrn.imd.pd.common.proto.PaxosNodeServiceGrpc;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GrpcServer implements CommunicationServer {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServer.class);

    private final int port;
    private final MessageHandler handler;
    private Server server;

    public GrpcServer(int port, MessageHandler handler) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new PaxosServiceImpl(handler))
                .build()
                .start();

        logger.info("GrpcServer escutando na porta {}", port);
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        logger.info("GrpcServer porta {} encerrado", port);
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    private static class PaxosServiceImpl extends PaxosNodeServiceGrpc.PaxosNodeServiceImplBase {

        private final MessageHandler handler;

        PaxosServiceImpl(MessageHandler handler) {
            this.handler = handler;
        }

        @Override
        public void exchange(GenericRequest request, StreamObserver<GenericResponse> responseObserver) {
            MessageEnvelope envelope = toEnvelope(request);

            MessageEnvelope result;
            try {
                result = handler.handle(envelope);
            } catch (Exception e) {
                result = MessageEnvelope.response(500, "{\"error\":\"" + e.getMessage() + "\"}");
            }

            responseObserver.onNext(toGrpcResponse(result));
            responseObserver.onCompleted();
        }

        private MessageEnvelope toEnvelope(GenericRequest request) {
            MessageEnvelope envelope = new MessageEnvelope();
            envelope.setMethod(request.getMethod());
            envelope.setPath(request.getPath());
            envelope.setBody(request.getBody().isEmpty() ? null : request.getBody());
            envelope.setHeaders(new HashMap<>(request.getHeadersMap()));
            return envelope;
        }

        private GenericResponse toGrpcResponse(MessageEnvelope envelope) {
            GenericResponse.Builder builder = GenericResponse.newBuilder()
                    .setStatusCode(envelope.getStatusCode());

            if (envelope.getBody() != null) builder.setBody(envelope.getBody());
            if (envelope.getHeaders() != null) {
                for (Map.Entry<String, String> entry : envelope.getHeaders().entrySet()) {
                    builder.putHeaders(entry.getKey(), entry.getValue());
                }
            }

            return builder.build();
        }
    }
}
