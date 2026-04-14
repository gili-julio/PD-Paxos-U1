package br.ufrn.imd.pd.common.protocol;

public enum ProtocolType {
    UDP,
    TCP,
    GRPC;

    public static ProtocolType fromString(String value) {
        return switch (value.toLowerCase()) {
            case "udp" -> UDP;
            case "tcp" -> TCP;
            case "grpc" -> GRPC;
            default -> throw new IllegalArgumentException(
                "Protocolo invalido: " + value + ". Use: udp, tcp ou grpc");
        };
    }
}
