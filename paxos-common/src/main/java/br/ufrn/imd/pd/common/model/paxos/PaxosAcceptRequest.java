package br.ufrn.imd.pd.common.model.paxos;

public class PaxosAcceptRequest {

    private String key;
    private long proposalNumber;
    private String value;

    public PaxosAcceptRequest() {}

    public PaxosAcceptRequest(String key, long proposalNumber, String value) {
        this.key = key;
        this.proposalNumber = proposalNumber;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public long getProposalNumber() { return proposalNumber; }
    public void setProposalNumber(long proposalNumber) { this.proposalNumber = proposalNumber; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
