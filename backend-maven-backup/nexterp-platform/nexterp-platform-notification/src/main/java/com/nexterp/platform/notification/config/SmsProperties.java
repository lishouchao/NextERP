package com.nexterp.platform.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信配置属性
 *
 * @author NextERP
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /**
     * 是否启用短信服务
     */
    private boolean enabled = false;

    /**
     * 短信服务商 (aliyun, tencent, custom)
     */
    private String provider = "aliyun";

    /**
     * 验证码短信模板
     */
    private String verificationTemplate = "SMS_VERIFICATION";

    /**
     * 通知短信模板
     */
    private String notificationTemplate = "SMS_NOTIFICATION";

    /**
     * 阿里云配置
     */
    private AliyunConfig aliyun = new AliyunConfig();

    /**
     * 腾讯云配置
     */
    private TencentConfig tencent = new TencentConfig();

    /**
     * 自定义配置
     */
    private CustomConfig custom = new CustomConfig();

    /**
     * 阿里云短信配置
     */
    @Data
    public static class AliyunConfig {
        /**
         * AccessKey ID
         */
        private String accessKeyId;

        /**
         * AccessKey Secret
         */
        private String accessKeySecret;

        /**
         * 短信签名
         */
        private String signName;

        /**
         * 短信服务地域
         */
        private String region = "cn-hangzhou";
    }

    /**
     * 腾讯云短信配置
     */
    @Data
    public static class TencentConfig {
        /**
         * SecretId
         */
        private String secretId;

        /**
         * SecretKey
         */
        private String secretKey;

        /**
         * 短信应用ID
         */
        private String sdkAppId;

        /**
         * 服务地域
         */
        private String region = "ap-guangzhou";
    }

    /**
     * 自定义短信配置
     */
    @Data
    public static class CustomConfig {
        /**
         * 自定义API地址
         */
        private String url;

        /**
         * API密钥
         */
        private String apiKey;
    }
}
