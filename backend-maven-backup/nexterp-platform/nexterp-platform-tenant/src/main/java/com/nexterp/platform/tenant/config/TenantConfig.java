package com.nexterp.platform.tenant.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 租户配置
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantConfig {

    /**
     * 功能开关
     */
    private FeatureConfig features;

    /**
     * 样式配置
     */
    private StyleConfig style;

    /**
     * 通知配置
     */
    private NotificationConfig notification;

    /**
     * 自定义配置
     */
    private Map<String, Object> custom;

    /**
     * 功能开关配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureConfig {
        private Boolean workflowEnabled;
        private Boolean reportEnabled;
        private Boolean notificationEnabled;
        private Boolean fileUploadEnabled;
        private Boolean apiAccessEnabled;
    }

    /**
     * 样式配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StyleConfig {
        private String primaryColor;
        private String logoUrl;
        private String faviconUrl;
        private String loginBackground;
        private String theme;
    }

    /**
     * 通知配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationConfig {
        private Boolean emailEnabled;
        private Boolean smsEnabled;
        private Boolean pushEnabled;
        private String emailFrom;
        private String smsSignature;
    }

    /**
     * 获取默认配置
     *
     * @return 默认配置
     */
    public static TenantConfig getDefault() {
        return TenantConfig.builder()
                .features(FeatureConfig.builder()
                        .workflowEnabled(true)
                        .reportEnabled(true)
                        .notificationEnabled(true)
                        .fileUploadEnabled(true)
                        .apiAccessEnabled(true)
                        .build())
                .style(StyleConfig.builder()
                        .primaryColor("#1890ff")
                        .theme("light")
                        .build())
                .notification(NotificationConfig.builder()
                        .emailEnabled(true)
                        .smsEnabled(false)
                        .pushEnabled(true)
                        .build())
                .build();
    }
}
