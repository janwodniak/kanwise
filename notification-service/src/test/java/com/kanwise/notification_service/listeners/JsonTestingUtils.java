package com.kanwise.notification_service.listeners;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public interface JsonTestingUtils {

    default String generateJsonStringFromPostParams(Map<String, List<String>> postParams) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<String, List<String>> entry : postParams.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append("\"").append(entry.getValue().get(0)).append("\",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    default String readJsonFileAsString(String path, ClassLoader classLoader) {
        try {
            try (InputStream jsonStream = classLoader.getResourceAsStream(path)) {
                assert jsonStream != null;
                return new String(jsonStream.readAllBytes());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
