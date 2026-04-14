package br.ufrn.imd.pd.common.protocol;

public interface CommunicationStrategy {

    CommunicationServer createServer(int port, MessageHandler handler);

    CommunicationClient createClient(String host, int port);

    ProtocolType getProtocolType();
}
