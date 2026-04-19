package br.ufrn.imd.pd.proposer.paxos;

import java.util.concurrent.atomic.AtomicLong;

public class ProposalNumberGenerator {

    private final int proposerId;
    private final AtomicLong sequence;

    public ProposalNumberGenerator(int proposerPort) {
        this.proposerId = proposerPort % 1000;
        this.sequence = new AtomicLong(0);
    }

    public ProposalNumber next() {
        return new ProposalNumber(sequence.incrementAndGet(), proposerId);
    }

    // Garante que o proximo numero gerado supere um valor externo observado (ex: NACK)
    public void updateFrom(long observedProposalNumber) {
        long observedSeq = observedProposalNumber / 1000;
        sequence.updateAndGet(current -> Math.max(current, observedSeq + 1));
    }
}
