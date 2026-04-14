package br.ufrn.imd.pd.common.protocol;

import java.util.concurrent.CompletableFuture;

public interface CommunicationClient {

    CompletableFuture<MessageEnvelope> send(MessageEnvelope request);

    void close();
}
