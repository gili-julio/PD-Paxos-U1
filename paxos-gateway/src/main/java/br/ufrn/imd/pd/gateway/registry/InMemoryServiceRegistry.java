package br.ufrn.imd.pd.gateway.registry;

import br.ufrn.imd.pd.common.model.ServiceInfo;
import br.ufrn.imd.pd.gateway.observer.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryServiceRegistry.class);

    private final Map<String, ServiceInfo> services = new ConcurrentHashMap<>();
    private final List<ServiceListener> listeners = new CopyOnWriteArrayList<>();

    public void register(ServiceInfo info) {
        services.put(info.getId(), info);
        logger.info("Servico registrado: {}", info.getId());
        listeners.forEach(l -> l.onServiceRegistered(info));
    }

    public void updateHeartbeat(String host, int port) {
        services.values().stream()
                .filter(s -> s.getHost().equals(host) && s.getPort() == port)
                .forEach(s -> {
                    s.setLastHeartbeat(System.currentTimeMillis());
                    if (!s.isActive()) {
                        s.setActive(true);
                        logger.info("Servico reativado: {}", s.getId());
                        listeners.forEach(l -> l.onServiceRestored(s));
                    }
                });
    }

    public void markInactive(ServiceInfo info) {
        info.setActive(false);
        logger.warn("Servico inativo: {}", info.getId());
        listeners.forEach(l -> l.onServiceFailed(info));
    }

    public List<ServiceInfo> getActive(String serviceName) {
        List<ServiceInfo> result = new ArrayList<>();
        for (ServiceInfo s : services.values()) {
            if (s.isActive() && s.getServiceName().equalsIgnoreCase(serviceName)) {
                result.add(s);
            }
        }
        return result;
    }

    public List<ServiceInfo> getAll() {
        return new ArrayList<>(services.values());
    }

    public void addListener(ServiceListener listener) {
        listeners.add(listener);
    }
}
