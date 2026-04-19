package br.ufrn.imd.pd.common.protocol.udp;

import br.ufrn.imd.pd.common.protocol.MessageEnvelope;

import java.util.HashMap;
import java.util.Map;

public class HttpLikeCodec {

    private static final String HTTP_VERSION = "HTTP/1.0";
    private static final String CRLF = "\r\n";

    private HttpLikeCodec() {}

    public static String encode(MessageEnvelope envelope) {
        StringBuilder sb = new StringBuilder();

        if (envelope.getMethod() != null && !envelope.getMethod().isBlank()) {
            sb.append(envelope.getMethod())
              .append(" ").append(envelope.getPath() != null ? envelope.getPath() : "/")
              .append(" ").append(HTTP_VERSION).append(CRLF);
        } else {
            String reason = envelope.getStatusCode() >= 400 ? "ERROR" : "OK";
            sb.append(HTTP_VERSION).append(" ").append(envelope.getStatusCode()).append(" ").append(reason).append(CRLF);
        }

        Map<String, String> headers = envelope.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(CRLF);
            }
        }

        String body = envelope.getBody();
        if (body != null && !body.isEmpty()) {
            sb.append("Content-Length: ").append(body.length()).append(CRLF);
        }

        sb.append(CRLF);

        if (body != null && !body.isEmpty()) {
            sb.append(body);
        }

        return sb.toString();
    }

    public static MessageEnvelope decode(String raw) {
        if (raw == null || raw.isBlank()) {
            return MessageEnvelope.response(400, "");
        }

        MessageEnvelope envelope = new MessageEnvelope();

        int headerEnd = raw.indexOf(CRLF + CRLF);
        String headerSection;
        String body = null;

        if (headerEnd >= 0) {
            headerSection = raw.substring(0, headerEnd);
            String afterHeaders = raw.substring(headerEnd + 4);
            if (!afterHeaders.isBlank()) body = afterHeaders;
        } else {
            headerSection = raw;
        }

        String[] lines = headerSection.split(CRLF, -1);
        if (lines.length == 0) return MessageEnvelope.response(400, "");

        String firstLine = lines[0];
        if (firstLine.startsWith(HTTP_VERSION)) {
            String[] parts = firstLine.split(" ", 3);
            if (parts.length >= 2) {
                try { envelope.setStatusCode(Integer.parseInt(parts[1])); }
                catch (NumberFormatException e) { envelope.setStatusCode(500); }
            }
        } else {
            String[] parts = firstLine.split(" ", 3);
            if (parts.length >= 2) {
                envelope.setMethod(parts[0]);
                envelope.setPath(parts[1]);
            }
        }

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) break;
            int sep = line.indexOf(": ");
            if (sep >= 0) {
                String k = line.substring(0, sep).trim();
                String v = line.substring(sep + 2).trim();
                if (!k.equalsIgnoreCase("Content-Length")) headers.put(k, v);
            }
        }
        envelope.setHeaders(headers);
        envelope.setBody(body);

        return envelope;
    }
}
