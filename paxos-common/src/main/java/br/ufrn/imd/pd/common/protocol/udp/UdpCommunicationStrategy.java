package br.ufrn.imd.pd.common.protocol.udp;

import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationServer;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class UdpCommunicationStrategy implements CommunicationStrategy {

    @Override
    public CommunicationServer createServer(int port, MessageHandler handler) {
        // TODO: implementar UdpServer
        throw new UnsupportedOperationException("UDP server ainda nao implementado");
    }

    @Override
    public CommunicationClient createClient(String host, int port) {
        // TODO: implementar UdpClient
        throw new UnsupportedOperationException("UDP client ainda nao implementado");
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.UDP;
    }
}
