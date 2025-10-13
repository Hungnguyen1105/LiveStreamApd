package com.example.livestream_apd.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;

@Service
@Slf4j
public class TRTCService {

    @Value("${trtc.app-id:}")
    private String appId;

    @Value("${trtc.app-secret:}")
    private String appSecret;

    @Value("${trtc.expire-time:86400}") // Default 24 hours
    private Long expireTime;

    /**
     * Generate UserSig for TRTC authentication
     * @param userId User ID
     * @return UserSig string
     */
    public String generateUserSig(String userId) {
        return generateUserSig(userId, expireTime);
    }

    /**
     * Generate UserSig for TRTC authentication with custom expire time
     * @param userId User ID
     * @param expireSeconds Expire time in seconds
     * @return UserSig string
     */
    public String generateUserSig(String userId, Long expireSeconds) {
        if (appId == null || appId.isEmpty()) {
            throw new IllegalStateException("TRTC App ID is not configured");
        }
        if (appSecret == null || appSecret.isEmpty()) {
            throw new IllegalStateException("TRTC App Secret is not configured");
        }

        try {
            long current = System.currentTimeMillis() / 1000;
            long expire = current + expireSeconds;

            // Create JSON payload
            String jsonPayload = String.format(
                "{\"TLS.ver\":\"2.0\"," +
                "\"TLS.identifier\":\"%s\"," +
                "\"TLS.appid\":%s," +
                "\"TLS.expire\":%d," +
                "\"TLS.time\":%d}",
                userId, appId, expire, current
            );

            // Compress the payload
            byte[] compressedPayload = compress(jsonPayload.getBytes(StandardCharsets.UTF_8));

            // Base64 encode
            String base64Payload = Base64.getEncoder().encodeToString(compressedPayload);

            // Create signature
            String signature = hmacSha256(base64Payload, appSecret);

            // Final UserSig
            String userSig = base64Payload + "." + signature;

            log.debug("Generated UserSig for user: {}, expire: {}", userId, expire);
            return userSig;

        } catch (Exception e) {
            log.error("Failed to generate UserSig for user: {}", userId, e);
            throw new RuntimeException("Failed to generate UserSig", e);
        }
    }

    /**
     * Verify if UserSig is valid (basic validation)
     * @param userId User ID
     * @param userSig UserSig to verify
     * @return true if valid
     */
    public boolean verifyUserSig(String userId, String userSig) {
        try {
            if (userSig == null || !userSig.contains(".")) {
                return false;
            }

            String[] parts = userSig.split("\\.");
            if (parts.length != 2) {
                return false;
            }

            String payload = parts[0];
            String signature = parts[1];

            // Verify signature
            String expectedSignature = hmacSha256(payload, appSecret);
            if (!signature.equals(expectedSignature)) {
                return false;
            }

            // Decode and decompress payload to check expiration
            byte[] decodedPayload = Base64.getDecoder().decode(payload);
            byte[] decompressedPayload = decompress(decodedPayload);
            String jsonPayload = new String(decompressedPayload, StandardCharsets.UTF_8);

            // Extract expire time (simple string parsing for this example)
            // In production, you might want to use a JSON library
            if (jsonPayload.contains("\"TLS.expire\":")) {
                String expireStr = jsonPayload.substring(
                    jsonPayload.indexOf("\"TLS.expire\":") + 13
                );
                expireStr = expireStr.substring(0, expireStr.indexOf(","));
                long expireTime = Long.parseLong(expireStr);
                long currentTime = System.currentTimeMillis() / 1000;

                return currentTime < expireTime;
            }

            return false;

        } catch (Exception e) {
            log.error("Failed to verify UserSig for user: {}", userId, e);
            return false;
        }
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private byte[] compress(byte[] data) throws Exception {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        byte[] buffer = new byte[1024];
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            baos.write(buffer, 0, count);
        }

        deflater.end();
        return baos.toByteArray();
    }

    private byte[] decompress(byte[] data) throws Exception {
        java.util.zip.Inflater inflater = new java.util.zip.Inflater();
        inflater.setInput(data);

        byte[] buffer = new byte[1024];
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            baos.write(buffer, 0, count);
        }

        inflater.end();
        return baos.toByteArray();
    }

    public String getAppId() {
        return appId;
    }

    public Long getExpireTime() {
        return expireTime;
    }
}