package br.ufrn.imd.pd.common.config;

import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class AppConfig {

    private static AppConfig instance;

    private ProtocolType protocolType;
    private int port;
    private String gatewayHost;
    private int gatewayPort;

    private AppConfig() {
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public static AppConfig fromArgs(String[] args) {
        AppConfig config = getInstance();

        for (String arg : args) {
            if (arg.startsWith("--protocol=")) {
                config.protocolType = ProtocolType.fromString(arg.substring("--protocol=".length()));
            } else if (arg.startsWith("--port=")) {
                config.port = Integer.parseInt(arg.substring("--port=".length()));
            } else if (arg.startsWith("--gateway=")) {
                String gateway = arg.substring("--gateway=".length());
                String[] parts = gateway.split(":");
                config.gatewayHost = parts[0];
                config.gatewayPort = Integer.parseInt(parts[1]);
            }
        }

        if (config.protocolType == null) {
            throw new IllegalArgumentException("Parametro --protocol e obrigatorio (udp, tcp ou grpc)");
        }
        if (config.port == 0) {
            throw new IllegalArgumentException("Parametro --port e obrigatorio");
        }

        return config;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public int getPort() {
        return port;
    }

    public String getGatewayHost() {
        return gatewayHost;
    }

    public int getGatewayPort() {
        return gatewayPort;
    }
}
