package com.nexterp.platform.notification.service;

import com.nexterp.platform.notification.domain.model.SysNotificationTemplate;
import com.nexterp.platform.notification.domain.repository.SysNotificationTemplateRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知模板服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final SysNotificationTemplateRepository templateRepository;

    /**
     * 创建通知模板
     *
     * @param template 模板
     * @return 模板ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(SysNotificationTemplate template) {
        // 检查模板编码是否存在
        if (templateRepository.findByTemplateCode(template.getTemplateCode(), template.getTenantId()).isPresent()) {
            throw new BusinessException("模板编码已存在");
        }

        SysNotificationTemplate saved = templateRepository.save(template);
        log.info("创建通知模板成功: templateCode={}", template.getTemplateCode());
        return saved.getId();
    }

    /**
     * 更新通知模板
     *
     * @param id 模板ID
     * @param template 模板
     * @return 模板
     */
    @Transactional(rollbackFor = Exception.class)
    public SysNotificationTemplate updateTemplate(Long id, SysNotificationTemplate template) {
        SysNotificationTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        existing.setTemplateName(template.getTemplateName());
        existing.setTitleTemplate(template.getTitleTemplate());
        existing.setContentTemplate(template.getContentTemplate());
        existing.setVariables(template.getVariables());
        existing.setStatus(template.getStatus());
        existing.setRemark(template.getRemark());

        return templateRepository.save(existing);
    }

    /**
     * 删除通知模板
     *
     * @param id 模板ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long id) {
        SysNotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        template.setIsDeleted(true);
        templateRepository.save(template);
    }

    /**
     * 根据模板编码渲染内容
     *
     * @param templateCode 模板编码
     * @param tenantId 租户ID
     * @param variables 变量
     * @return 渲染后的内容
     */
    public RenderedContent renderTemplate(String templateCode, Long tenantId, Map<String, Object> variables) {
        SysNotificationTemplate template = templateRepository.findByTemplateCode(templateCode, tenantId)
                .orElseThrow(() -> new BusinessException("模板不存在"));

        String title = renderString(template.getTitleTemplate(), variables);
        String content = renderString(template.getContentTemplate(), variables);

        return new RenderedContent(title, content);
    }

    /**
     * 渲染字符串
     *
     * @param template 模板字符串
     * @param variables 变量
     * @return 渲染后的字符串
     */
    private String renderString(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            if (value != null) {
                result = result.replace("${" + variableName + "}", String.valueOf(value));
            }
        }

        return result;
    }

    /**
     * 获取所有启用状态的模板
     *
     * @param tenantId 租户ID
     * @return 模板列表
     */
    public List<SysNotificationTemplate> getAllActiveTemplates(Long tenantId) {
        return templateRepository.findAllActive(tenantId);
    }

    /**
     * 渲染后的内容
     */
    public static class RenderedContent {
        private final String title;
        private final String content;

        public RenderedContent(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
