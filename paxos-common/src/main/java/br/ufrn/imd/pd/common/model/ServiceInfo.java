package br.ufrn.imd.pd.common.model;

import br.ufrn.imd.pd.common.protocol.ProtocolType;

public class ServiceInfo {

    private String serviceName;
    private String host;
    private int port;
    private ProtocolType protocol;
    private long lastHeartbeat;
    private boolean active;

    public ServiceInfo() {
    }

    public ServiceInfo(String serviceName, String host, int port, ProtocolType protocol) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.lastHeartbeat = System.currentTimeMillis();
        this.active = true;
    }

    public String getId() {
        return serviceName + "@" + host + ":" + port;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return getId() + " [" + (active ? "ACTIVE" : "INACTIVE") + "]";
    }
}
