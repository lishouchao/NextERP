package com.nexterp.platform.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.platform.workflow.domain.model.ProcessDefinition;
import com.nexterp.platform.workflow.domain.repository.ProcessDefinitionRepository;
import com.nexterp.shared.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程定义服务
 *
 * @author NextERP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessDefinitionService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final RepositoryService flowableRepositoryService;

    /**
     * 创建流程定义
     *
     * @param definition 流程定义
     * @return 定义ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDefinition(ProcessDefinition definition) {
        // 检查流程Key是否已存在
        processDefinitionRepository.findLatestByKey(definition.getProcessKey(), definition.getTenantId())
                .ifPresent(existing -> {
                    throw new BusinessException("流程Key已存在");
                });

        ProcessDefinition saved = processDefinitionRepository.save(definition);
        log.info("创建流程定义: processKey={}", definition.getProcessKey());
        return saved.getId();
    }

    /**
     * 部署流程定义
     *
     * @param id 定义ID
     * @return 部署ID
     */
    @Transactional(rollbackFor = Exception.class)
    public String deployDefinition(Long id) {
        ProcessDefinition definition = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        if (definition.getStatus() == 1) {
            throw new BusinessException("流程已发布");
        }

        if (definition.getBpmnXml() == null || definition.getBpmnXml().isEmpty()) {
            throw new BusinessException("BPMN定义不能为空");
        }

        // 部署到Flowable
        Deployment deployment = flowableRepositoryService.createDeployment()
                .name(definition.getProcessName())
                .addBytes(definition.getProcessKey() + ".bpmn20.xml",
                        definition.getBpmnXml().getBytes(StandardCharsets.UTF_8))
                .deploy();

        // 更新状态
        definition.setStatus(1);
        definition.setPublishTime(LocalDateTime.now());
        processDefinitionRepository.save(definition);

        log.info("部署流程定义: processKey={}, deploymentId={}",
                definition.getProcessKey(), deployment.getId());
        return deployment.getId();
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
    @Transactional(rollbackFor = Exception.class)
    public Long importBpmnFile(MultipartFile file, String processKey, String processName, Long tenantId) {
        try {
            String bpmnXml = new String(file.getBytes(), StandardCharsets.UTF_8);

            ProcessDefinition definition = ProcessDefinition.builder()
                    .tenantId(tenantId)
                    .processKey(processKey)
                    .processName(processName)
                    .bpmnXml(bpmnXml)
                    .status(0)
                    .build();

            return createDefinition(definition);
        } catch (IOException e) {
            log.error("读取BPMN文件失败", e);
            throw new BusinessException("读取BPMN文件失败");
        }
    }

    /**
     * 更新流程定义
     *
     * @param id 定义ID
     * @param definition 流程定义
     * @return 更新后的定义
     */
    @Transactional(rollbackFor = Exception.class)
    public ProcessDefinition updateDefinition(Long id, ProcessDefinition definition) {
        ProcessDefinition existing = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        if (existing.getStatus() == 1) {
            throw new BusinessException("已发布的流程不能修改");
        }

        existing.setProcessName(definition.getProcessName());
        existing.setDescription(definition.getDescription());
        existing.setCategory(definition.getCategory());
        if (definition.getBpmnXml() != null) {
            existing.setBpmnXml(definition.getBpmnXml());
        }

        return processDefinitionRepository.save(existing);
    }

    /**
     * 删除流程定义
     *
     * @param id 定义ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDefinition(Long id) {
        ProcessDefinition definition = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        if (definition.getStatus() == 1) {
            throw new BusinessException("已发布的流程不能删除");
        }

        definition.setIsDeleted(true);
        processDefinitionRepository.save(definition);
    }

    /**
     * 获取流程定义详情
     *
     * @param id 定义ID
     * @return 流程定义
     */
    public ProcessDefinition getDefinitionById(Long id) {
        return processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));
    }

    /**
     * 根据流程Key获取最新版本
     *
     * @param processKey 流程Key
     * @param tenantId 租户ID
     * @return 流程定义
     */
    public ProcessDefinition getLatestByKey(String processKey, Long tenantId) {
        return processDefinitionRepository.findLatestByKey(processKey, tenantId)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));
    }

    /**
     * 查询租户所有已发布的流程定义
     *
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    public List<ProcessDefinition> getPublishedDefinitions(Long tenantId) {
        return processDefinitionRepository.findPublished(tenantId);
    }

    /**
     * 根据分类查询流程定义
     *
     * @param category 分类
     * @param tenantId 租户ID
     * @return 流程定义列表
     */
    public List<ProcessDefinition> getDefinitionsByCategory(String category, Long tenantId) {
        return processDefinitionRepository.findByCategory(category, tenantId);
    }

    /**
     * 启用/禁用流程定义
     *
     * @param id 定义ID
     * @param enabled 是否启用
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefinitionEnabled(Long id, Boolean enabled) {
        ProcessDefinition definition = processDefinitionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("流程定义不存在"));

        definition.setEnabled(enabled);
        processDefinitionRepository.save(definition);
    }
}
