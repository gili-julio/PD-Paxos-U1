package br.ufrn.imd.pd.gateway.observer;

import br.ufrn.imd.pd.common.model.ServiceInfo;
import br.ufrn.imd.pd.gateway.registry.InMemoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatMonitor {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatMonitor.class);
    private static final long TIMEOUT_MS = 10_000;
    private static final long CHECK_INTERVAL_S = 5;

    private final InMemoryServiceRegistry registry;
    private final ScheduledExecutorService scheduler;

    public HeartbeatMonitor(InMemoryServiceRegistry registry) {
        this.registry = registry;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "heartbeat-monitor");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkHeartbeats, CHECK_INTERVAL_S, CHECK_INTERVAL_S, TimeUnit.SECONDS);
        logger.info("HeartbeatMonitor iniciado (timeout={}ms, intervalo={}s)", TIMEOUT_MS, CHECK_INTERVAL_S);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void checkHeartbeats() {
        long now = System.currentTimeMillis();
        for (ServiceInfo service : registry.getAll()) {
            if (service.isActive() && now - service.getLastHeartbeat() > TIMEOUT_MS) {
                registry.markInactive(service);
            }
        }
    }
}
