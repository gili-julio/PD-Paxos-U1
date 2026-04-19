package br.ufrn.imd.pd.common.model.paxos;

import java.util.List;

public class PaxosRelayBatch {

    private int totalAcceptors;
    private List<String> responses;

    public PaxosRelayBatch() {}

    public PaxosRelayBatch(int totalAcceptors, List<String> responses) {
        this.totalAcceptors = totalAcceptors;
        this.responses = responses;
    }

    public int getTotalAcceptors() { return totalAcceptors; }
    public void setTotalAcceptors(int totalAcceptors) { this.totalAcceptors = totalAcceptors; }

    public List<String> getResponses() { return responses; }
    public void setResponses(List<String> responses) { this.responses = responses; }
}
