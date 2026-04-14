package br.ufrn.imd.pd.common.protocol.tcp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class TcpCommunicationStrategy implements CommunicationStrategy {

    @Override
    public CommunicationServer createServer(int port, MessageHandler handler) {
        // TODO: implementar TcpServer
        throw new UnsupportedOperationException("TCP server ainda nao implementado");
    }

    @Override
    public CommunicationClient createClient(String host, int port) {
        // TODO: implementar TcpClient
        throw new UnsupportedOperationException("TCP client ainda nao implementado");
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }
}
