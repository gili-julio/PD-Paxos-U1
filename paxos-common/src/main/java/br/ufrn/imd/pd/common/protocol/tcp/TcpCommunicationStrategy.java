package br.ufrn.imd.pd.common.protocol.tcp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class TcpCommunicationStrategy implements CommunicationStrategy {

    @Override
    public CommunicationServer createServer(int port, MessageHandler handler) {
        return new TcpServer(port, handler);
    }

    @Override
    public CommunicationClient createClient(String host, int port) {
        return new TcpClient(host, port);
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.TCP;
    }
}
