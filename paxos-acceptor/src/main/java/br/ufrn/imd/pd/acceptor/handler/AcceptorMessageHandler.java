package br.ufrn.imd.pd.acceptor.handler;

import br.ufrn.imd.pd.acceptor.paxos.PaxosAcceptorLogic;
import br.ufrn.imd.pd.acceptor.store.KeyValueStore;
import br.ufrn.imd.pd.common.model.paxos.PaxosAcceptRequest;
import br.ufrn.imd.pd.common.model.paxos.PaxosAcceptedResponse;
import br.ufrn.imd.pd.common.model.paxos.PaxosPrepareRequest;
import br.ufrn.imd.pd.common.model.paxos.PaxosPromiseResponse;
import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AcceptorMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AcceptorMessageHandler.class);

    private final PaxosAcceptorLogic paxosLogic;
    private final KeyValueStore kvStore;
    private final Gson gson;

    public AcceptorMessageHandler(PaxosAcceptorLogic paxosLogic, KeyValueStore kvStore) {
        this.paxosLogic = paxosLogic;
        this.kvStore = kvStore;
        this.gson = new Gson();
    }

    @Override
    public MessageEnvelope handle(MessageEnvelope request) {
        String method = request.getMethod();
        String path   = request.getPath() != null ? request.getPath() : "";

        logger.debug("Acceptor recebeu: {} {}", method, path);

        if ("POST".equalsIgnoreCase(method) && path.equals("/paxos/prepare")) {
            return handlePrepare(request);
        }
        if ("POST".equalsIgnoreCase(method) && path.equals("/paxos/accept")) {
            return handleAccept(request);
        }
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/kv/")) {
            return handleGet(extractKey(path));
        }
        if ("POST".equalsIgnoreCase(method) && path.equals("/heartbeat")) {
            return MessageEnvelope.response(200, toJson("status", "UP"));
        }

        logger.warn("Rota nao encontrada: {} {}", method, path);
        return MessageEnvelope.response(404, toJson("error", "Rota nao encontrada"));
    }

    private MessageEnvelope handlePrepare(MessageEnvelope request) {
        if (request.getBody() == null || request.getBody().isBlank()) {
            return MessageEnvelope.response(400, toJson("error", "Corpo vazio"));
        }
        try {
            PaxosPrepareRequest req = gson.fromJson(request.getBody(), PaxosPrepareRequest.class);
            if (req.getKey() == null || req.getKey().isBlank()) {
                return MessageEnvelope.response(400, toJson("error", "Campo 'key' ausente"));
            }
            PaxosPromiseResponse promise = paxosLogic.handlePrepare(req);
            return MessageEnvelope.response(200, gson.toJson(promise));
        } catch (Exception e) {
            return MessageEnvelope.response(400, toJson("error", "JSON invalido: " + e.getMessage()));
        }
    }

    private MessageEnvelope handleAccept(MessageEnvelope request) {
        if (request.getBody() == null || request.getBody().isBlank()) {
            return MessageEnvelope.response(400, toJson("error", "Corpo vazio"));
        }
        try {
            PaxosAcceptRequest req = gson.fromJson(request.getBody(), PaxosAcceptRequest.class);
            if (req.getKey() == null || req.getKey().isBlank()) {
                return MessageEnvelope.response(400, toJson("error", "Campo 'key' ausente"));
            }
            if (req.getValue() == null) {
                return MessageEnvelope.response(400, toJson("error", "Campo 'value' ausente"));
            }
            PaxosAcceptedResponse accepted = paxosLogic.handleAccept(req);
            return MessageEnvelope.response(200, gson.toJson(accepted));
        } catch (Exception e) {
            return MessageEnvelope.response(400, toJson("error", "JSON invalido: " + e.getMessage()));
        }
    }

    private MessageEnvelope handleGet(String key) {
        if (key == null || key.isBlank()) {
            return MessageEnvelope.response(400, toJson("error", "Chave invalida"));
        }
        Optional<String> value = kvStore.get(key);
        if (value.isPresent()) {
            JsonObject json = new JsonObject();
            json.addProperty("key", key);
            json.addProperty("value", value.get());
            return MessageEnvelope.response(200, gson.toJson(json));
        }
        return MessageEnvelope.response(404, toJson("error", "Chave nao encontrada: " + key));
    }

    private String extractKey(String path) {
        int idx = path.indexOf("/kv/");
        if (idx < 0) return null;
        String key = path.substring(idx + 4);
        return key.isBlank() ? null : key;
    }

    private String toJson(String key, String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty(key, value);
        return gson.toJson(obj);
    }
}
