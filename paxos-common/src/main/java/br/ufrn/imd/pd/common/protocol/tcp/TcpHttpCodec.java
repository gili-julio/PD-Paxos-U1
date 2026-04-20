package br.ufrn.imd.pd.common.protocol.tcp;

import br.ufrn.imd.pd.common.protocol.MessageEnvelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TcpHttpCodec {

    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String CRLF = "\r\n";

    private TcpHttpCodec() {}

    public static void write(MessageEnvelope envelope, OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();

        if (envelope.getMethod() != null && !envelope.getMethod().isBlank()) {
            sb.append(envelope.getMethod())
              .append(" ").append(envelope.getPath() != null ? envelope.getPath() : "/")
              .append(" ").append(HTTP_VERSION).append(CRLF);
        } else {
            String reason = envelope.getStatusCode() >= 400 ? "ERROR" : "OK";
            sb.append(HTTP_VERSION).append(" ").append(envelope.getStatusCode()).append(" ").append(reason).append(CRLF);
        }

        String body = envelope.getBody();
        int bodyLength = (body != null && !body.isEmpty()) ? body.getBytes(StandardCharsets.UTF_8).length : 0;

        Map<String, String> headers = envelope.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(CRLF);
            }
        }

        sb.append("Content-Length: ").append(bodyLength).append(CRLF);
        sb.append(CRLF);

        if (bodyLength > 0) {
            sb.append(body);
        }

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static MessageEnvelope read(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String firstLine = reader.readLine();
        if (firstLine == null || firstLine.isBlank()) {
            return MessageEnvelope.response(400, "");
        }

        MessageEnvelope envelope = new MessageEnvelope();

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
        int contentLength = 0;
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int sep = line.indexOf(": ");
            if (sep >= 0) {
                String key = line.substring(0, sep).trim();
                String value = line.substring(sep + 2).trim();
                if (key.equalsIgnoreCase("Content-Length")) {
                    try { contentLength = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
                } else {
                    headers.put(key, value);
                }
            }
        }

        envelope.setHeaders(headers);

        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            envelope.setBody(new String(bodyChars, 0, totalRead));
        }

        return envelope;
    }
}
