package br.ufrn.imd.pd.acceptor.paxos;

import java.util.concurrent.ConcurrentHashMap;

public class AcceptorState {

    private final ConcurrentHashMap<String, KeyState> stateByKey = new ConcurrentHashMap<>();

    public KeyState getOrCreate(String key) {
        return stateByKey.computeIfAbsent(key, k -> new KeyState());
    }

    public static class KeyState {
        long highestPromisedProposal = 0;
        long highestAcceptedProposal = 0;
        String acceptedValue = null;
    }
}
