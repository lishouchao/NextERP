package com.nexterp.platform.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.notification.config.SmsProperties;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 短信发送服务
 * 支持阿里云短信、腾讯云短信等主流短信服务商
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sms", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SmsService {

    private final SmsProperties smsProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号
     * @param templateCode 短信模板编码
     * @param params      模板参数
     * @return 是否发送成功
     */
    public boolean sendSms(String phoneNumber, String templateCode, Map<String, String> params) {
        String provider = smsProperties.getProvider();

        return switch (provider.toLowerCase()) {
            case "aliyun" -> sendAliyunSms(phoneNumber, templateCode, params);
            case "tencent" -> sendTencentSms(phoneNumber, templateCode, params);
            case "custom" -> sendCustomSms(phoneNumber, templateCode, params);
            default -> {
                log.error("不支持的短信服务商: {}", provider);
                yield false;
            }
        };
    }

    /**
     * 发送验证码短信
     *
     * @param phoneNumber 手机号
     * @param code        验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String phoneNumber, String code) {
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        return sendSms(phoneNumber, smsProperties.getVerificationTemplate(), params);
    }

    /**
     * 发送通知短信
     *
     * @param phoneNumber 手机号
     * @param message     消息内容
     * @return 是否发送成功
     */
    public boolean sendNotification(String phoneNumber, String message) {
        Map<String, String> params = new HashMap<>();
        params.put("message", message);
        return sendSms(phoneNumber, smsProperties.getNotificationTemplate(), params);
    }

    /**
     * 阿里云短信发送
     */
    private boolean sendAliyunSms(String phoneNumber, String templateCode, Map<String, String> params) {
        try {
            String url = "https://dysmsapi.aliyuncs.com/";

            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("PhoneNumbers", phoneNumber);
            requestParams.put("SignName", smsProperties.getAliyun().getSignName());
            requestParams.put("TemplateCode", templateCode);
            requestParams.put("TemplateParam", objectMapper.writeValueAsString(params));

            // 计算签名
            String timestamp = String.valueOf(System.currentTimeMillis());
            requestParams.put("Timestamp", timestamp);
            requestParams.put("AccessKeyId", smsProperties.getAliyun().getAccessKeyId());
            requestParams.put("SignatureMethod", "HMAC-SHA1");
            requestParams.put("SignatureVersion", "1.0");
            requestParams.put("SignatureNonce", java.util.UUID.randomUUID().toString());
            requestParams.put("Action", "SendSms");
            requestParams.put("Version", "2017-05-25");

            // TODO: 实际签名计算需要使用阿里云SDK或自行实现签名算法
            // String signature = calculateAliyunSignature(requestParams, smsProperties.getAliyun().getAccessKeySecret());
            // requestParams.put("Signature", signature);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestParams, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                String code = responseBody.path("Code").asText();
                if ("OK".equals(code)) {
                    log.info("阿里云短信发送成功: {}", phoneNumber);
                    return true;
                } else {
                    log.error("阿里云短信发送失败: {}, code={}", phoneNumber, code);
                    return false;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("阿里云短信发送异常: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 腾讯云短信发送
     */
    private boolean sendTencentSms(String phoneNumber, String templateCode, Map<String, String> params) {
        try {
            String url = "https://sms.tencentcloudapi.com/";

            // 构建请求参数
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("PhoneNumberSet", new String[]{"+86" + phoneNumber});
            requestParams.put("TemplateID", templateCode);
            requestParams.put("TemplateParamSet", params.values().toArray(new String[0]));
            requestParams.put("SmsSdkAppId", smsProperties.getTencent().getSdkAppId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TC-Action", "SendSms");
            headers.set("X-TC-Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            headers.set("X-TC-Version", "2021-01-11");
            headers.set("X-TC-Region", smsProperties.getTencent().getRegion());

            // TODO: 实际签名计算需要使用腾讯云SDK或自行实现签名算法
            // String signature = calculateTencentSignature(headers, requestParams, smsProperties.getTencent().getSecretId(), smsProperties.getTencent().getSecretKey());
            // headers.set("Authorization", signature);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestParams, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                JsonNode responseStatus = responseBody.path("Response").path("Error");
                if (responseStatus.isMissingNode() || responseStatus.isEmpty()) {
                    log.info("腾讯云短信发送成功: {}", phoneNumber);
                    return true;
                } else {
                    log.error("腾讯云短信发送失败: {}, error={}", phoneNumber, responseStatus.path("Message").asText());
                    return false;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("腾讯云短信发送异常: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 自定义短信发送
     */
    private boolean sendCustomSms(String phoneNumber, String templateCode, Map<String, String> params) {
        try {
            String url = smsProperties.getCustom().getUrl();

            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("phone", phoneNumber);
            requestParams.put("template", templateCode);
            requestParams.put("params", params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsProperties.getCustom().getApiKey() != null) {
                headers.set("X-API-Key", smsProperties.getCustom().getApiKey());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestParams, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("自定义短信发送成功: {}", phoneNumber);
                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("自定义短信发送异常: {}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 批量发送短信
     *
     * @param phoneNumbers 手机号列表
     * @param templateCode 短信模板编码
     * @param params       模板参数
     * @return 发送结果映射
     */
    public Map<String, Boolean> sendBatchSms(java.util.List<String> phoneNumbers, String templateCode, Map<String, String> params) {
        Map<String, Boolean> results = new HashMap<>();

        for (String phoneNumber : phoneNumbers) {
            boolean success = sendSms(phoneNumber, templateCode, params);
            results.put(phoneNumber, success);

            // 避免触发限流
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return results;
    }
}
