package com.example.livestream_apd.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayService {

    @Value("${vnpay.sandbox.merchant-id}")
    private String vnpTmnCode;

    @Value("${vnpay.sandbox.secret-key}")
    private String vnpHashSecret;

    @Value("${vnpay.sandbox.return-url}")
    private String vnpReturnUrl;

    @Value("${vnpay.sandbox.notify-url}")
    private String vnpNotifyUrl;

    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "pay";
    private static final String VNP_ORDER_TYPE = "other";
    private static final String VNP_CURRENCY_CODE = "VND";
    private static final String VNP_LOCALE = "vn";

    public String createPaymentUrl(String orderId, long amount, String orderInfo, String ipAddress, String returnUrl) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", VNP_VERSION);
            vnpParams.put("vnp_Command", VNP_COMMAND);
            vnpParams.put("vnp_TmnCode", vnpTmnCode);
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay requires amount in VND x 100
            vnpParams.put("vnp_CurrCode", VNP_CURRENCY_CODE);
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", VNP_ORDER_TYPE);
            vnpParams.put("vnp_Locale", VNP_LOCALE);
            // Use returnUrl from request if provided, otherwise use default config
            vnpParams.put("vnp_ReturnUrl", returnUrl != null && !returnUrl.isEmpty() ? returnUrl : vnpReturnUrl);
            vnpParams.put("vnp_IpAddr", ipAddress);

            // Create date - sử dụng timezone Việt Nam
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String vnpCreateDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Expire date (30 minutes from now - tăng thời gian để test dễ hơn)
            cld.add(Calendar.MINUTE, 30);
            String vnpExpireDate = formatter.format(cld.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            log.info("VNPay payment creation - OrderId: {}, CreateDate: {}, ExpireDate: {}", 
                    orderId, vnpCreateDate, vnpExpireDate);

            // Build hash data
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnpSecureHash = hmacSHA512(vnpHashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

            return VNP_URL + "?" + queryUrl;

        } catch (Exception e) {
            log.error("Error creating VNPay payment URL", e);
            throw new RuntimeException("Error creating payment URL", e);
        }
    }

    public boolean validateSignature(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(vnpHashSecret, hashData.toString());
            return signValue.equals(vnpSecureHash);

        } catch (Exception e) {
            log.error("Error validating VNPay signature", e);
            return false;
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            log.error("Error creating HMAC SHA512", ex);
            return "";
        }
    }

    public String generateTransactionId() {
        return String.valueOf(System.currentTimeMillis());
    }
}
