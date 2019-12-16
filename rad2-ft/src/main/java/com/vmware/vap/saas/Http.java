package com.vmware.vap.saas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Http {

    private static final int TIMEOUT = 300_000;
    private static final Logger logger = Logger.getLogger("http");

    public static Response exchange(String url, Map<String, String> headers, Method method, byte[] data) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method.name());
        if (headers != null) {
            headers.forEach(connection::addRequestProperty);
        }
        if (data != null) {
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(data);
                outputStream.flush();
            }
        }
        int code = connection.getResponseCode();
        String message = connection.getResponseMessage();
        Map<String, List<String>> responseHeaders = connection.getHeaderFields();
        byte[] response;
        try (InputStream inputStream = connection.getInputStream()) {
            response = toBytes(inputStream);
        } catch (IOException e) {
            try (InputStream errorStream = connection.getErrorStream()) {
                response = toBytes(errorStream);
            }
        }
        return new Response(code, message, responseHeaders, response);
    }

    public static Http.Response retry(String url, Map<String, String> headers, Http.Method method, byte[] data) {
        long timeout = System.currentTimeMillis() + TIMEOUT;
        while (true) {
            try {
                Http.Response response = exchange(url, headers, method, data);
                if (response.code == 200) {
                    return response;
                }
                logger.info(url + " Response: " + response);
            } catch (IOException e) {
                logger.info(url + " - " + e);
            }
            if (System.currentTimeMillis() > timeout) {
                logger.warning(url + " Timed out...");
                throw new RuntimeException(url + " Timed out");
            }
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new RuntimeException(url + " - " + e);
            }
        }
    }

    private static byte[] toBytes(InputStream inputStream) throws IOException {
        int read;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] bytes = new byte[16384];
        while ((read = inputStream.read(bytes, 0, bytes.length)) != -1) {
            buffer.write(bytes, 0, read);
        }
        return buffer.toByteArray();
    }

    public enum Method { GET, POST, PUT, HEAD, DELETE, PATCH }

    public static class Response {
        public final int code;
        public final String message;
        public final Map<String, List<String>> headers;
        public final byte[] data;

        private Response(int code, String message, Map<String, List<String>> headers, byte[] data) {
            this.code = code;
            this.message = message;
            this.headers = headers;
            this.data = data;
        }

        @Override
        public String toString() {
            return "HTTP " + code + ": " + message + " - " + new String(data);
        }
    }
}
