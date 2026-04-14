package br.ufrn.imd.pd.common.protocol.grpc;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class GrpcCommunicationStrategy implements CommunicationStrategy {

    @Override
    public CommunicationServer createServer(int port, MessageHandler handler) {
        // TODO: implementar GrpcServer
        throw new UnsupportedOperationException("gRPC server ainda nao implementado");
    }

    @Override
    public CommunicationClient createClient(String host, int port) {
        // TODO: implementar GrpcClient
        throw new UnsupportedOperationException("gRPC client ainda nao implementado");
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.GRPC;
    }
}
