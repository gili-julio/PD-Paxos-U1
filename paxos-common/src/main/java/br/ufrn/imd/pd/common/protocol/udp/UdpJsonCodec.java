package br.ufrn.imd.pd.common.protocol.udp;

import br.ufrn.imd.pd.common.protocol.MessageEnvelope;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class UdpJsonCodec {

    private static final Gson gson = new Gson();

    private UdpJsonCodec() {}

    public static String encode(MessageEnvelope envelope) {
        JsonObject obj = new JsonObject();

        if (envelope.getMethod() != null && !envelope.getMethod().isBlank()) {
            obj.addProperty("method", envelope.getMethod());
            obj.addProperty("path", envelope.getPath() != null ? envelope.getPath() : "/");

            if (envelope.getHeaders() != null && !envelope.getHeaders().isEmpty()) {
                JsonObject headers = new JsonObject();
                envelope.getHeaders().forEach(headers::addProperty);
                obj.add("headers", headers);
            }

            if (envelope.getBody() != null) {
                obj.addProperty("body", envelope.getBody());
            }
        } else {
            obj.addProperty("statusCode", envelope.getStatusCode());

            if (envelope.getBody() != null) {
                try {
                    obj.add("data", gson.fromJson(envelope.getBody(), JsonElement.class));
                } catch (Exception e) {
                    obj.addProperty("data", envelope.getBody());
                }
            }
        }

        return gson.toJson(obj);
    }

    public static MessageEnvelope decode(String json) {
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);

            if (obj.has("acao")) {
                return decodeExterno(obj);
            }
            if (obj.has("method")) {
                return decodeInterno(obj);
            }
            if (obj.has("statusCode")) {
                return decodeResposta(obj);
            }

            return MessageEnvelope.response(400, "{\"error\":\"Formato desconhecido\"}");
        } catch (Exception e) {
            return MessageEnvelope.response(400, "{\"error\":\"JSON invalido: " + e.getMessage() + "\"}");
        }
    }

    private static MessageEnvelope decodeExterno(JsonObject obj) {
        String acao = obj.get("acao").getAsString().toUpperCase();
        String key  = obj.has("key") ? obj.get("key").getAsString() : "";

        return switch (acao) {
            case "ESCREVER" -> {
                String value = obj.has("value") ? obj.get("value").getAsString() : "";
                yield MessageEnvelope.request("PUT", "/kv/" + key,
                        "{\"value\":\"" + value + "\"}");
            }
            case "LER" -> MessageEnvelope.request("GET", "/kv/" + key, null);
            default -> MessageEnvelope.response(400, "{\"error\":\"Acao desconhecida: " + acao + "\"}");
        };
    }

    private static MessageEnvelope decodeInterno(JsonObject obj) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setMethod(obj.get("method").getAsString());
        envelope.setPath(obj.has("path") ? obj.get("path").getAsString() : "/");

        if (obj.has("headers")) {
            Map<String, String> headers = new HashMap<>();
            obj.getAsJsonObject("headers").entrySet()
               .forEach(e -> headers.put(e.getKey(), e.getValue().getAsString()));
            envelope.setHeaders(headers);
        }

        if (obj.has("body")) {
            envelope.setBody(obj.get("body").getAsString());
        }

        return envelope;
    }

    private static MessageEnvelope decodeResposta(JsonObject obj) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setStatusCode(obj.get("statusCode").getAsInt());

        if (obj.has("data")) {
            JsonElement data = obj.get("data");
            envelope.setBody(data.isJsonPrimitive() ? data.getAsString() : gson.toJson(data));
        }

        return envelope;
    }
}
