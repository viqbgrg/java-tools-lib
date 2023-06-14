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
import java.util.concurrent.*;

public class AlistDownload {
    private static final String URL = "https://al.chirmyram.com/api/public/path";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String LOCAL_PATH = "C:\\Users\\hhj\\Desktop\\1111";

    private static final ArrayBlockingQueue<Runnable> QUEUE = new ArrayBlockingQueue<>(5);
    private static final ThreadPoolExecutor EXECUTOR_SERVICE = new ThreadPoolExecutor(7, 7,
            0L, TimeUnit.MILLISECONDS,
            QUEUE);


    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        EXECUTOR_SERVICE.prestartAllCoreThreads();
        url("/rep/Doc/欧路词典库");
        EXECUTOR_SERVICE.shutdown();
    }

    private static void url(String path) throws JsonProcessingException, InterruptedException {
        JsonNode jsonNode = requestPath(path);
        JsonNode jsonNode1 = jsonNode.at("/data/files");
        if (jsonNode1 instanceof ArrayNode arrayNode) {
            for (JsonNode node : arrayNode) {
                int anInt = node.get("type").asInt();
                if (anInt == 1) {
                    url(path + "/" + node.get("name").asText());
                } else {
                    QUEUE.put(() -> {
                        try {
                            DownloadHelper.download(node.get("url").asText(), LOCAL_PATH + "/" + path + "/" + node.get("name").asText());
                        } catch (IOException e) {
                            System.out.println("失败:" + path);
                            throw new RuntimeException(e);
                        }
                    });
                    System.out.println("QUEUE: " + QUEUE.size());
                }
            }
        }
    }


    public static JsonNode requestPath(String path) throws JsonProcessingException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(URL);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        Path pathObject = new Path("", path);
        String pathJson = OBJECT_MAPPER.writeValueAsString(pathObject);
        // 解决中文乱码问题
        StringEntity stringEntity = new StringEntity(pathJson, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        JsonNode jsonNode = null;
        try {
            jsonNode = httpclient.execute(httpPost, response -> {
                if (response.getCode() != 200) {
                    return requestPath(path);
                }
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("----------------------------------------");
                System.out.println(responseBody);
                JsonNode jsonNode1 = OBJECT_MAPPER.readTree(responseBody);
                if (jsonNode1.get("code").asInt() != 200) {
                    return requestPath(path);
                }
                return jsonNode1;
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
