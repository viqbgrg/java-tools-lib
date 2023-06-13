package com.github.viqbgrg.tools.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlistDownload {
    private static final String URL = "https://al.chirmyram.com/api/public/path";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String LOCAL_PATH = "C:\\Users\\hhj\\Desktop\\1111";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    private static final ArrayBlockingQueue<String> QUEUE = new ArrayBlockingQueue<>(7);

    public static void main(String[] args) throws JsonProcessingException {
        url("/rep/Doc/欧路词典库");
        EXECUTOR_SERVICE.shutdown();
    }

    private static void url(String path) throws JsonProcessingException {
        JsonNode jsonNode = requestPath(path);
        JsonNode jsonNode1 = jsonNode.at("/data/files");
        if (jsonNode1 instanceof ArrayNode arrayNode) {
            for (JsonNode node : arrayNode) {
                int anInt = node.get("type").asInt();
                if (anInt == 1) {
                    url(path + "/" + node.get("name").asText());
                } else {
                    downloadThread(LOCAL_PATH + "/" + path + "/" + node.get("name").asText(), node.get("url").asText());
                }
            }
        }
    }

    public static void downloadThread(String path, String url) {
        EXECUTOR_SERVICE.submit(() -> {
            try {
                DownloadHelper.download(url, path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static JsonNode requestPath(String path) throws JsonProcessingException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpPost httpPost = new HttpPost(URL);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        Path pathObject = new Path("", path);
        String pathJson = objectMapper.writeValueAsString(pathObject);
        // 解决中文乱码问题
        StringEntity stringEntity = new StringEntity(pathJson, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        JsonNode jsonNode = null;
        try {
            jsonNode = httpclient.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    requestPath(path);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("----------------------------------------");
                System.out.println(responseBody);
                return objectMapper.readTree(responseBody);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonNode;
    }

    record Path(String password, String path) {
    }

    ;

}
