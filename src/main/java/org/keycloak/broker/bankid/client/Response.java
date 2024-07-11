package org.keycloak.broker.bankid.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.keycloak.connections.httpclient.SafeInputStream;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Response {
    private static final ObjectMapper mapper = new ObjectMapper();

    private HttpResponse response;
    private long maxConsumedResponseSize;

    private int statusCode = -1;
    private ContentType contentType;
    private String responseString;
    
    public Response(HttpResponse response, long maxConsumedResponseSize) {
        this.response = response;
        this.maxConsumedResponseSize = maxConsumedResponseSize;
    }

    public int getStatus() throws IOException {
        if ( this.response != null ) {
            return this.response.getStatusLine().getStatusCode();
        } else {
            throw new IOException("Invalid response");
        }
    }

    public String asString() throws IOException {
        readResponse();
        return responseString;
    }

    public JsonNode asJson() throws IOException {
        return mapper.readTree(this.asString());
    }

    public <T> T asJson(Class<T> type) throws IOException {
        return JsonSerialization.readValue(asString(), type);
    }

    private void readResponse() throws IOException {
        if (statusCode == -1) {
            statusCode = response.getStatusLine().getStatusCode();

            InputStream is;
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                is = entity.getContent();
                contentType = ContentType.getOrDefault(entity);
                Charset charset = contentType.getCharset();
                try {
                    HeaderIterator it = response.headerIterator();
                    while (it.hasNext()) {
                        Header header = it.nextHeader();
                        if (header.getName().equals("Content-Encoding") && header.getValue().equals("gzip")) {
                            is = new GZIPInputStream(is);
                        }
                    }

                    is = new SafeInputStream(is, maxConsumedResponseSize);

                    try (InputStreamReader reader = charset == null ? new InputStreamReader(is, StandardCharsets.UTF_8) :
                            new InputStreamReader(is, charset)) {

                        StringWriter writer = new StringWriter();

                        char[] buffer = new char[1024 * 4];
                        for (int n = reader.read(buffer); n != -1; n = reader.read(buffer)) {
                            writer.write(buffer, 0, n);
                        }

                        responseString = writer.toString();
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }
        }
    }


}
