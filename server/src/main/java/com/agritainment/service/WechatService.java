package com.agritainment.service;

import com.agritainment.common.AppException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WechatService {

    @Value("${app.wechat.mini-program.appid:}")
    private String appid;

    @Value("${app.wechat.mini-program.secret:}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile String accessToken;
    private volatile long accessTokenExpireAt = 0;

    public String code2Session(String code) {
        if (isPlaceholder()) {
            log.warn("WeChat config is placeholder, skipping code2Session");
            return null;
        }
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code);
        try {
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(resp);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                log.error("code2Session failed: {}", resp);
                return null;
            }
            return node.get("openid").asText();
        } catch (Exception e) {
            log.error("code2Session error", e);
            return null;
        }
    }

    public synchronized String getAccessToken() {
        if (isPlaceholder()) {
            log.warn("WeChat config is placeholder, skipping getAccessToken");
            return null;
        }
        if (accessToken != null && System.currentTimeMillis() < accessTokenExpireAt) {
            return accessToken;
        }
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appid, secret);
        try {
            String resp = restTemplate.getForObject(url, String.class);
            JsonNode node = objectMapper.readTree(resp);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                log.error("getAccessToken failed: {}", resp);
                return null;
            }
            accessToken = node.get("access_token").asText();
            int expiresIn = node.get("expires_in").asInt(7200);
            accessTokenExpireAt = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
            return accessToken;
        } catch (Exception e) {
            log.error("getAccessToken error", e);
            return null;
        }
    }

    public boolean sendSubscribeMessage(String openid, String templateId, String page, Map<String, Object> data) {
        if (openid == null || openid.isEmpty()) return false;
        String token = getAccessToken();
        if (token == null) {
            log.warn("No access_token, skip sending subscribe message to {}", openid);
            return false;
        }
        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s", token);
        Map<String, Object> body = new ConcurrentHashMap<>();
        body.put("touser", openid);
        body.put("template_id", templateId);
        body.put("page", page != null ? page : "");
        body.put("data", data);
        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            String resp = restTemplate.postForObject(url, jsonBody, String.class);
            JsonNode node = objectMapper.readTree(resp);
            int errcode = node.get("errcode").asInt();
            if (errcode == 0) {
                log.info("Subscribe message sent to {}", openid);
                return true;
            }
            log.error("Send subscribe message failed: {}", resp);
            return false;
        } catch (Exception e) {
            log.error("Send subscribe message error", e);
            return false;
        }
    }

    private boolean isPlaceholder() {
        return appid == null || appid.isEmpty() || appid.contains("your-");
    }
}
