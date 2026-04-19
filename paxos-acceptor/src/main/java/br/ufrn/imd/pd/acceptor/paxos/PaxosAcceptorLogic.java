package br.ufrn.imd.pd.acceptor.paxos;

import br.ufrn.imd.pd.acceptor.store.KeyValueStore;
import br.ufrn.imd.pd.common.model.paxos.PaxosAcceptRequest;
import br.ufrn.imd.pd.common.model.paxos.PaxosAcceptedResponse;
import br.ufrn.imd.pd.common.model.paxos.PaxosPrepareRequest;
import br.ufrn.imd.pd.common.model.paxos.PaxosPromiseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaxosAcceptorLogic {

    private static final Logger logger = LoggerFactory.getLogger(PaxosAcceptorLogic.class);

    private final String acceptorId;
    private final AcceptorState state;
    private final KeyValueStore kvStore;

    public PaxosAcceptorLogic(String acceptorId, AcceptorState state, KeyValueStore kvStore) {
        this.acceptorId = acceptorId;
        this.state = state;
        this.kvStore = kvStore;
    }

    public PaxosPromiseResponse handlePrepare(PaxosPrepareRequest request) {
        String key = request.getKey();
        long n = request.getProposalNumber();

        AcceptorState.KeyState keyState = state.getOrCreate(key);

        synchronized (keyState) {
            if (n > keyState.highestPromisedProposal) {
                keyState.highestPromisedProposal = n;
                logger.debug("[{}] PROMISE key='{}' n={}", acceptorId, key, n);
                return PaxosPromiseResponse.promise(
                        acceptorId, n,
                        keyState.highestAcceptedProposal,
                        keyState.acceptedValue);
            }

            logger.debug("[{}] NACK Fase1 key='{}' n={} < promised={}", acceptorId, key, n, keyState.highestPromisedProposal);
            return PaxosPromiseResponse.nack(acceptorId, keyState.highestPromisedProposal);
        }
    }

    public PaxosAcceptedResponse handleAccept(PaxosAcceptRequest request) {
        String key = request.getKey();
        long n = request.getProposalNumber();
        String value = request.getValue();

        AcceptorState.KeyState keyState = state.getOrCreate(key);

        synchronized (keyState) {
            if (n >= keyState.highestPromisedProposal) {
                keyState.highestPromisedProposal = n;
                keyState.highestAcceptedProposal = n;
                keyState.acceptedValue = value;
                kvStore.put(key, value);
                logger.info("[{}] ACCEPTED key='{}' n={} value='{}'", acceptorId, key, n, value);
                return PaxosAcceptedResponse.accepted(acceptorId, n);
            }

            logger.debug("[{}] NACK Fase2 key='{}' n={} < promised={}", acceptorId, key, n, keyState.highestPromisedProposal);
            return PaxosAcceptedResponse.nack(acceptorId, keyState.highestPromisedProposal);
        }
    }
}
