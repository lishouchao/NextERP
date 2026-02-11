package com.nexterp.platform.workflow.controller;

import com.nexterp.platform.workflow.domain.model.ProcessDefinition;
import com.nexterp.platform.workflow.service.ProcessDefinitionService;
import com.nexterp.shared.core.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 流程定义控制器
 *
 * @author NextERP
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/definitions")
@RequiredArgsConstructor
public class ProcessDefinitionController {

    private final ProcessDefinitionService processDefinitionService;

    /**
     * 创建流程定义
     *
     * @param definition 流程定义
     * @return 定义ID
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:workflow:definition:add')")
    public Result<Long> createDefinition(@RequestBody ProcessDefinition definition) {
        Long id = processDefinitionService.createDefinition(definition);
        return Result.success(id);
    }

    /**
     * 部署流程定义
     *
     * @param id 定义ID
     * @return 部署ID
     */
    @PostMapping("/{id}/deploy")
    @PreAuthorize("hasAuthority('system:workflow:definition:deploy')")
    public Result<String> deployDefinition(@PathVariable Long id) {
        String deploymentId = processDefinitionService.deployDefinition(id);
        return Result.success(deploymentId);
    }

    /**
     * 导入BPMN文件
     *
     * @param file BPMN文件
     * @param processKey 流程Key
     * @param processName 流程名称
     * @param tenantId 租户ID
     * @return 定义ID
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('system:workflow:definition:import')")
    public Result<Long> importBpmnFile(
            @RequestParam MultipartFile file,
            @RequestParam String processKey,
            @RequestParam String processName,
            @RequestParam Long tenantId) {
        Long id = processDefinitionService.importBpmnFile(file, processKey, processName, tenantId);
        return Result.success(id);
    }

    /**
     * 更新流程定义
     *
     * @param id 定义ID
     * @param definition 流程定义
     * @return 更新后的定义
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:workflow:definition:edit')")
    public Result<ProcessDefinition> updateDefinition(
            @PathVariable Long id,
            @RequestBody ProcessDefinition definition) {
        ProcessDefinition updated = processDefinitionService.updateDefinition(id, definition);
        return Result.success(updated);
    }

    /**
     * 删除流程定义
     *
     * @param id 定义ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:workflow:definition:delete')")
    public Result<Void> deleteDefinition(@PathVariable Long id) {
        processDefinitionService.deleteDefinition(id);
        return Result.success();
    }

    /**
     * 获取流程定义详情
     *
     * @param id 定义ID
     * @return 流程定义
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:workflow:definition:view')")
    public Result<ProcessDefinition> getDefinitionById(@PathVariable Long id) {
        ProcessDefinition definition = processDefinitionService.getDefinitionById(id);
        return Result.success(definition);
    }

    /**
     * 根据流程Key获取最新版本
     *
     * @param processKey 流程Key
     * @param tenantId 租户ID
     * @return 流程定义
     */
    @GetMapping("/latest")
    @PreAuthorize("hasAuthority('system:workflow:definition:view')")
    public Result<ProcessDefinition> getLatestByKey(
            @RequestParam String processKey,
            @RequestParam Long tenantId) {
        ProcessDefinition definition = processDefinitionService.getLatestByKey(processKey, tenantId);
        return Result.success(definition);
    }

    /**
     * 查询租户所有已发布的流程定义
     *
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    @GetMapping("/published")
    @PreAuthorize("hasAuthority('system:workflow:definition:view')")
    public Result<List<ProcessDefinition>> getPublishedDefinitions(@RequestParam Long tenantId) {
        List<ProcessDefinition> definitions = processDefinitionService.getPublishedDefinitions(tenantId);
        return Result.success(definitions);
    }

    /**
     * 根据分类查询流程定义
     *
     * @param category 分类
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    @GetMapping("/category")
    @PreAuthorize("hasAuthority('system:workflow:definition:view')")
    public Result<List<ProcessDefinition>> getDefinitionsByCategory(
            @RequestParam String category,
            @RequestParam Long tenantId) {
        List<ProcessDefinition> definitions = processDefinitionService.getDefinitionsByCategory(category, tenantId);
        return Result.success(definitions);
    }

    /**
     * 启用/禁用流程定义
     *
     * @param id 定义ID
     * @param enabled 是否启用
     * @return 成功响应
     */
    @PutMapping("/{id}/enabled")
    @PreAuthorize("hasAuthority('system:workflow:definition:edit')")
    public Result<Void> setDefinitionEnabled(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        processDefinitionService.setDefinitionEnabled(id, enabled);
        return Result.success();
    }
}
