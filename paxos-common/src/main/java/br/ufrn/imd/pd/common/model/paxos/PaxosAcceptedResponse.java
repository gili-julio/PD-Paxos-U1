package br.ufrn.imd.pd.common.model.paxos;

public class PaxosAcceptedResponse {

    public static final String TYPE_ACCEPTED = "ACCEPTED";
    public static final String TYPE_NACK = "NACK";

    private String type;
    private String acceptorId;
    private long proposalNumber;

    public PaxosAcceptedResponse() {}

    public static PaxosAcceptedResponse accepted(String acceptorId, long proposalNumber) {
        PaxosAcceptedResponse r = new PaxosAcceptedResponse();
        r.type = TYPE_ACCEPTED;
        r.acceptorId = acceptorId;
        r.proposalNumber = proposalNumber;
        return r;
    }

    public static PaxosAcceptedResponse nack(String acceptorId, long proposalNumber) {
        PaxosAcceptedResponse r = new PaxosAcceptedResponse();
        r.type = TYPE_NACK;
        r.acceptorId = acceptorId;
        r.proposalNumber = proposalNumber;
        return r;
    }

    public boolean isAccepted() { return TYPE_ACCEPTED.equals(type); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAcceptorId() { return acceptorId; }
    public void setAcceptorId(String acceptorId) { this.acceptorId = acceptorId; }

    public long getProposalNumber() { return proposalNumber; }
    public void setProposalNumber(long proposalNumber) { this.proposalNumber = proposalNumber; }
}
