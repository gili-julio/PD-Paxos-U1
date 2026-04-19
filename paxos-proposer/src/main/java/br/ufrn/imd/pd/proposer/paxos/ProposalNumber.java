package br.ufrn.imd.pd.proposer.paxos;

public class ProposalNumber {

    // Garante unicidade entre proposers: proposalNumber = seq * MAX_PROPOSERS + proposerId
    private static final int MAX_PROPOSERS = 1000;

    private final long sequence;
    private final int proposerId;
    private final long value;

    public ProposalNumber(long sequence, int proposerId) {
        this.sequence = sequence;
        this.proposerId = proposerId;
        this.value = sequence * MAX_PROPOSERS + proposerId;
    }

    public long getValue() { return value; }
    public long getSequence() { return sequence; }
    public int getProposerId() { return proposerId; }
}
