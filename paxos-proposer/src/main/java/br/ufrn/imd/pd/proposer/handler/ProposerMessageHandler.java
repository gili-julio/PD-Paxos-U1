package br.ufrn.imd.pd.proposer.handler;

import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import br.ufrn.imd.pd.common.protocol.MessageHandler;
import br.ufrn.imd.pd.proposer.paxos.PaxosProposerLogic;
import br.ufrn.imd.pd.proposer.paxos.PaxosRoundResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProposerMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProposerMessageHandler.class);

    private final PaxosProposerLogic paxosLogic;
    private final Gson gson;

    public ProposerMessageHandler(PaxosProposerLogic paxosLogic) {
        this.paxosLogic = paxosLogic;
        this.gson = new Gson();
    }

    @Override
    public MessageEnvelope handle(MessageEnvelope request) {
        String method = request.getMethod();
        String path   = request.getPath() != null ? request.getPath() : "";

        logger.debug("Proposer recebeu: {} {}", method, path);

        if ("POST".equalsIgnoreCase(method) && path.equals("/heartbeat")) {
            return MessageEnvelope.response(200, toJson("status", "UP"));
        }
        if ("PUT".equalsIgnoreCase(method) && path.startsWith("/kv/")) {
            return handlePut(extractKey(path), request.getBody());
        }

        logger.warn("Rota nao encontrada: {} {}", method, path);
        return MessageEnvelope.response(404, toJson("error", "Rota nao encontrada"));
    }

    private MessageEnvelope handlePut(String key, String body) {
        if (key == null || key.isBlank()) {
            return MessageEnvelope.response(400, toJson("error", "Chave invalida"));
        }

        String value = extractValue(body);
        if (value == null || value.isBlank()) {
            return MessageEnvelope.response(400, toJson("error", "Valor ausente"));
        }

        PaxosRoundResult result = paxosLogic.propose(key, value);

        if (result.isSuccess()) {
            JsonObject json = new JsonObject();
            json.addProperty("status", "ok");
            json.addProperty("key", key);
            json.addProperty("value", result.getValue());
            return MessageEnvelope.response(200, gson.toJson(json));
        }

        logger.error("Falha no consenso: {}", result.getErrorMessage());
        return MessageEnvelope.response(503, toJson("error", result.getErrorMessage()));
    }

    private String extractKey(String path) {
        int idx = path.indexOf("/kv/");
        if (idx < 0) return null;
        String key = path.substring(idx + 4);
        return key.isBlank() ? null : key;
    }

    // Aceita tanto {"value":"..."} quanto texto puro
    private String extractValue(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json.has("value")) return json.get("value").getAsString();
        } catch (Exception ignored) {}
        return body.trim();
    }

    private String toJson(String key, String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty(key, value);
        return gson.toJson(obj);
    }
}
