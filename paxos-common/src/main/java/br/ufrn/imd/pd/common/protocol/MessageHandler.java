package br.ufrn.imd.pd.common.protocol;

public interface MessageHandler {

    MessageEnvelope handle(MessageEnvelope request);
}
