package br.ufrn.imd.pd.common.model.paxos;

public class PaxosPromiseResponse {

    public static final String TYPE_PROMISE = "PROMISE";
    public static final String TYPE_NACK = "NACK";

    private String type;
    private String acceptorId;
    private long proposalNumber;
    private long acceptedProposalNumber;
    private String acceptedValue;

    public PaxosPromiseResponse() {}

    public static PaxosPromiseResponse promise(String acceptorId, long proposalNumber,
                                               long acceptedProposalNumber, String acceptedValue) {
        PaxosPromiseResponse p = new PaxosPromiseResponse();
        p.type = TYPE_PROMISE;
        p.acceptorId = acceptorId;
        p.proposalNumber = proposalNumber;
        p.acceptedProposalNumber = acceptedProposalNumber;
        p.acceptedValue = acceptedValue;
        return p;
    }

    public static PaxosPromiseResponse nack(String acceptorId, long proposalNumber) {
        PaxosPromiseResponse p = new PaxosPromiseResponse();
        p.type = TYPE_NACK;
        p.acceptorId = acceptorId;
        p.proposalNumber = proposalNumber;
        return p;
    }

    public boolean isPromise() { return TYPE_PROMISE.equals(type); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAcceptorId() { return acceptorId; }
    public void setAcceptorId(String acceptorId) { this.acceptorId = acceptorId; }

    public long getProposalNumber() { return proposalNumber; }
    public void setProposalNumber(long proposalNumber) { this.proposalNumber = proposalNumber; }

    public long getAcceptedProposalNumber() { return acceptedProposalNumber; }
    public void setAcceptedProposalNumber(long acceptedProposalNumber) { this.acceptedProposalNumber = acceptedProposalNumber; }

    public String getAcceptedValue() { return acceptedValue; }
    public void setAcceptedValue(String acceptedValue) { this.acceptedValue = acceptedValue; }
}
