package br.ufrn.imd.pd.common.protocol;

import java.io.IOException;

public interface CommunicationServer {

    void start() throws IOException;

    void stop();

    boolean isRunning();
}
