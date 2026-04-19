package br.ufrn.imd.pd.common.protocol.udp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class UdpCommunicationStrategy implements CommunicationStrategy {

    @Override
    public CommunicationServer createServer(int port, MessageHandler handler) {
        return new UdpServer(port, handler);
    }

    @Override
    public CommunicationClient createClient(String host, int port) {
        return new UdpClient(host, port);
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.UDP;
    }
}
