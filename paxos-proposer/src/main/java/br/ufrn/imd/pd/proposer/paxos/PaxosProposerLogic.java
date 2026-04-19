package br.ufrn.imd.pd.proposer.paxos;

import br.ufrn.imd.pd.common.model.paxos.PaxosAcceptRequest;
import br.ufrn.imd.pd.common.model.paxos.PaxosAcceptedResponse;
import br.ufrn.imd.pd.common.model.paxos.PaxosPrepareRequest;
import br.ufrn.imd.pd.common.model.paxos.PaxosPromiseResponse;
import br.ufrn.imd.pd.common.model.paxos.PaxosRelayBatch;
import br.ufrn.imd.pd.common.protocol.CommunicationClient;
import br.ufrn.imd.pd.common.protocol.CommunicationStrategy;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PaxosProposerLogic {

    private static final Logger logger = LoggerFactory.getLogger(PaxosProposerLogic.class);

    private static final int MAX_PAXOS_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 100;

    private final CommunicationStrategy strategy;
    private final String gatewayHost;
    private final int gatewayPort;
    private final ProposalNumberGenerator proposalGenerator;
    private final Gson gson;

    public PaxosProposerLogic(CommunicationStrategy strategy, String gatewayHost, int gatewayPort, int proposerPort) {
        this.strategy = strategy;
        this.gatewayHost = gatewayHost;
        this.gatewayPort = gatewayPort;
        this.proposalGenerator = new ProposalNumberGenerator(proposerPort);
        this.gson = new Gson();
    }

    public PaxosRoundResult propose(String key, String value) {
        for (int attempt = 1; attempt <= MAX_PAXOS_RETRIES; attempt++) {
            logger.info("[PAXOS] Tentativa {}/{} key='{}'", attempt, MAX_PAXOS_RETRIES, key);

            ProposalNumber n = proposalGenerator.next();

            // Fase 1: Prepare
            List<PaxosPromiseResponse> promises = sendPrepare(key, n);
            if (promises == null) {
                sleepBefore(attempt);
                continue;
            }

            long promised = countPromises(promises);
            long total = promises.size();
            long needed = (total / 2) + 1;

            logger.info("[PAXOS] Fase1 {}/{} promises (quorum={})", promised, total, needed);

            if (promised < needed) {
                updateGeneratorFromNacks(promises);
                sleepBefore(attempt);
                continue;
            }

            // Regra de seguranca: usar o valor do maior acceptedProposalNumber entre os promises
            String valueToPropose = chooseSafeValue(promises, value);
            if (!valueToPropose.equals(value)) {
                logger.info("[PAXOS] Valor substituido pelo previamente aceito: '{}'", valueToPropose);
            }

            // Fase 2: Accept
            List<PaxosAcceptedResponse> accepteds = sendAccept(key, n.getValue(), valueToPropose);
            if (accepteds == null) {
                sleepBefore(attempt);
                continue;
            }

            long accepted = accepteds.stream().filter(PaxosAcceptedResponse::isAccepted).count();
            long neededAcc = (accepteds.size() / 2) + 1;

            logger.info("[PAXOS] Fase2 {}/{} accepted (quorum={})", accepted, accepteds.size(), neededAcc);

            if (accepted >= neededAcc) {
                logger.info("[PAXOS] Consenso atingido! key='{}' value='{}'", key, valueToPropose);
                return PaxosRoundResult.success(valueToPropose);
            }

            sleepBefore(attempt);
        }

        return PaxosRoundResult.failure("Consenso nao atingido apos " + MAX_PAXOS_RETRIES + " tentativas para key='" + key + "'");
    }

    private List<PaxosPromiseResponse> sendPrepare(String key, ProposalNumber n) {
        MessageEnvelope msg = new MessageEnvelope("POST", "/paxos/relay");
        msg.addHeader("X-Paxos-Action", "PREPARE");
        msg.addHeader("X-Target", "acceptors");
        msg.setBody(gson.toJson(new PaxosPrepareRequest(key, n.getValue())));

        try {
            CommunicationClient client = strategy.createClient(gatewayHost, gatewayPort);
            MessageEnvelope response = client.send(msg).get(5, TimeUnit.SECONDS);
            client.close();

            if (response.getStatusCode() != 200 || response.getBody() == null) {
                logger.error("[PAXOS] Gateway respondeu {} ao PREPARE relay", response.getStatusCode());
                return null;
            }

            return parsePromises(gson.fromJson(response.getBody(), PaxosRelayBatch.class));
        } catch (Exception e) {
            logger.error("[PAXOS] Erro ao enviar PREPARE", e);
            return null;
        }
    }

    private List<PaxosPromiseResponse> parsePromises(PaxosRelayBatch batch) {
        List<PaxosPromiseResponse> result = new ArrayList<>();
        if (batch == null || batch.getResponses() == null) return result;
        for (String json : batch.getResponses()) {
            try {
                result.add(gson.fromJson(json, PaxosPromiseResponse.class));
            } catch (Exception e) {
                logger.warn("[PAXOS] Nao foi possivel deserializar Promise: {}", json);
            }
        }
        return result;
    }

    private long countPromises(List<PaxosPromiseResponse> responses) {
        return responses.stream().filter(PaxosPromiseResponse::isPromise).count();
    }

    private void updateGeneratorFromNacks(List<PaxosPromiseResponse> responses) {
        responses.stream()
                 .filter(r -> !r.isPromise())
                 .mapToLong(PaxosPromiseResponse::getProposalNumber)
                 .max()
                 .ifPresent(proposalGenerator::updateFrom);
    }

    private String chooseSafeValue(List<PaxosPromiseResponse> promises, String clientValue) {
        return promises.stream()
                       .filter(PaxosPromiseResponse::isPromise)
                       .filter(p -> p.getAcceptedValue() != null && !p.getAcceptedValue().isBlank())
                       .max((a, b) -> Long.compare(a.getAcceptedProposalNumber(), b.getAcceptedProposalNumber()))
                       .map(PaxosPromiseResponse::getAcceptedValue)
                       .orElse(clientValue);
    }

    private List<PaxosAcceptedResponse> sendAccept(String key, long proposalNumber, String value) {
        MessageEnvelope msg = new MessageEnvelope("POST", "/paxos/relay");
        msg.addHeader("X-Paxos-Action", "ACCEPT");
        msg.addHeader("X-Target", "acceptors");
        msg.setBody(gson.toJson(new PaxosAcceptRequest(key, proposalNumber, value)));

        try {
            CommunicationClient client = strategy.createClient(gatewayHost, gatewayPort);
            MessageEnvelope response = client.send(msg).get(5, TimeUnit.SECONDS);
            client.close();

            if (response.getStatusCode() != 200 || response.getBody() == null) {
                logger.error("[PAXOS] Gateway respondeu {} ao ACCEPT relay", response.getStatusCode());
                return null;
            }

            return parseAccepteds(gson.fromJson(response.getBody(), PaxosRelayBatch.class));
        } catch (Exception e) {
            logger.error("[PAXOS] Erro ao enviar ACCEPT", e);
            return null;
        }
    }

    private List<PaxosAcceptedResponse> parseAccepteds(PaxosRelayBatch batch) {
        List<PaxosAcceptedResponse> result = new ArrayList<>();
        if (batch == null || batch.getResponses() == null) return result;
        for (String json : batch.getResponses()) {
            try {
                result.add(gson.fromJson(json, PaxosAcceptedResponse.class));
            } catch (Exception e) {
                logger.warn("[PAXOS] Nao foi possivel deserializar Accepted: {}", json);
            }
        }
        return result;
    }

    private void sleepBefore(int attempt) {
        if (attempt < MAX_PAXOS_RETRIES) {
            try { Thread.sleep(RETRY_DELAY_MS * attempt); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
