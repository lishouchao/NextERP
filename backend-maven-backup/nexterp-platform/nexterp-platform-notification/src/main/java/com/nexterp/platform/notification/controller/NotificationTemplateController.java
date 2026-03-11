package com.nexterp.platform.notification.controller;

import com.nexterp.platform.notification.domain.model.SysNotificationTemplate;
import com.nexterp.platform.notification.service.NotificationTemplateService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知模板控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    /**
     * 创建通知模板
     *
     * @param template 模板
     * @return 模板ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:notification:template:add')")
    public Result<Long> createTemplate(@RequestBody SysNotificationTemplate template) {
        Long id = templateService.createTemplate(template);
        return Result.success(id);
    }

    /**
     * 更新通知模板
     *
     * @param id 模板ID
     * @param template 模板
     * @return 模板
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notification:template:edit')")
    public Result<SysNotificationTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody SysNotificationTemplate template) {
        SysNotificationTemplate updated = templateService.updateTemplate(id, template);
        return Result.success(updated);
    }

    /**
     * 删除通知模板
     *
     * @param id 模板ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:notification:template:delete')")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }

    /**
     * 获取所有启用状态的模板
     *
     * @param tenantId 租户ID
     * @return 模板列表
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('system:notification:template:view')")
    public Result<List<SysNotificationTemplate>> getAllActiveTemplates(@RequestParam Long tenantId) {
        List<SysNotificationTemplate> templates = templateService.getAllActiveTemplates(tenantId);
        return Result.success(templates);
    }

    /**
     * 渲染模板内容
     *
     * @param templateCode 模板编码
     * @param tenantId 租户ID
     * @param variables 变量
     * @return 渲染后的内容
     */
    @PostMapping("/render")
    @PreAuthorize("hasAuthority('system:notification:template:view')")
    public Result<Map<String, String>> renderTemplate(
            @RequestParam String templateCode,
            @RequestParam Long tenantId,
            @RequestBody Map<String, Object> variables) {
        NotificationTemplateService.RenderedContent rendered =
                templateService.renderTemplate(templateCode, tenantId, variables);

        return Result.success(Map.of(
                "title", rendered.getTitle(),
                "content", rendered.getContent()
        ));
    }
}
