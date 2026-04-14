package br.ufrn.imd.pd.common.factory;

import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.ProtocolType;
import br.ufrn.imd.pd.common.protocol.udp.UdpCommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.tcp.TcpCommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.grpc.GrpcCommunicationStrategy;

public class CommunicationFactory {

    private CommunicationFactory() {
    }

    public static CommunicationStrategy create(ProtocolType type) {
        return switch (type) {
            case UDP -> new UdpCommunicationStrategy();
            case TCP -> new TcpCommunicationStrategy();
            case GRPC -> new GrpcCommunicationStrategy();
        };
    }
}
