package br.ufrn.imd.pd.common.protocol;

import java.util.HashMap;
import java.util.Map;

public class MessageEnvelope {

    private String method;
    private String path;
    private Map<String, String> headers;
    private String body;
    private int statusCode;

    public MessageEnvelope() {
        this.headers = new HashMap<>();
        this.statusCode = 200;
    }

    public MessageEnvelope(String method, String path) {
        this();
        this.method = method;
        this.path = path;
    }

    public static MessageEnvelope request(String method, String path, String body) {
        MessageEnvelope envelope = new MessageEnvelope(method, path);
        envelope.setBody(body);
        return envelope;
    }

    public static MessageEnvelope response(int statusCode, String body) {
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.setStatusCode(statusCode);
        envelope.setBody(body);
        return envelope;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
