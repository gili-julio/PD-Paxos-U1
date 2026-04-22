package br.ufrn.imd.pd.gateway.observer;

import br.ufrn.imd.pd.common.model.ServiceInfo;

public interface ServiceListener {
    void onServiceRegistered(ServiceInfo info);
    void onServiceFailed(ServiceInfo info);
    void onServiceRestored(ServiceInfo info);
}
