package com.nexterp.platform.notification.service;

import com.nexterp.platform.notification.domain.model.SysNotificationTemplate;
import com.nexterp.platform.notification.domain.repository.SysNotificationTemplateRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 通知模板服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class NotificationTemplateServiceTest {

    @Mock
    private SysNotificationTemplateRepository templateRepository;

    @InjectMocks
    private NotificationTemplateService templateService;

    private SysNotificationTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = SysNotificationTemplate.builder()
                .id(1L)
                .tenantId(1L)
                .templateCode("ORDER_APPROVED")
                .templateName("订单审批通知")
                .notificationType("system")
                .titleTemplate("${processName}审批通知")
                .contentTemplate("${initiator}提交的${processName}需要您审批")
                .status(1)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("创建模板 - 成功")
    void testCreateTemplate_Success() {
        when(templateRepository.findByTemplateCode("ORDER_APPROVED", 1L)).thenReturn(Optional.empty());
        when(templateRepository.save(any(SysNotificationTemplate.class))).thenReturn(testTemplate);

        Long id = templateService.createTemplate(testTemplate);

        assertThat(id).isEqualTo(1L);
        verify(templateRepository).save(testTemplate);
    }

    @Test
    @DisplayName("创建模板 - 编码已存在")
    void testCreateTemplate_CodeExists() {
        when(templateRepository.findByTemplateCode("ORDER_APPROVED", 1L)).thenReturn(Optional.of(testTemplate));

        assertThatThrownBy(() -> templateService.createTemplate(testTemplate))
                .isInstanceOf(BusinessException.class)
                .hasMessage("模板编码已存在");
    }

    @Test
    @DisplayName("更新模板")
    void testUpdateTemplate() {
        SysNotificationTemplate updated = SysNotificationTemplate.builder()
                .templateName("更新后的模板")
                .titleTemplate("新标题")
                .contentTemplate("新内容")
                .build();

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(SysNotificationTemplate.class))).thenReturn(testTemplate);

        SysNotificationTemplate result = templateService.updateTemplate(1L, updated);

        assertThat(result).isNotNull();
        verify(templateRepository).save(any(SysNotificationTemplate.class));
    }

    @Test
    @DisplayName("删除模板")
    void testDeleteTemplate() {
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.save(any(SysNotificationTemplate.class))).thenReturn(testTemplate);

        templateService.deleteTemplate(1L);

        assertThat(testTemplate.getIsDeleted()).isTrue();
        verify(templateRepository).save(testTemplate);
    }

    @Test
    @DisplayName("渲染模板内容")
    void testRenderTemplate() {
        when(templateRepository.findByTemplateCode("ORDER_APPROVED", 1L))
                .thenReturn(Optional.of(testTemplate));

        Map<String, Object> variables = Map.of(
                "processName", "采购订单",
                "initiator", "张三"
        );

        var result = templateService.renderTemplate("ORDER_APPROVED", 1L, variables);

        assertThat(result.getTitle()).isEqualTo("采购订单审批通知");
        assertThat(result.getContent()).isEqualTo("张三提交的采购订单需要您审批");
    }

    @Test
    @DisplayName("渲染模板 - 模板不存在")
    void testRenderTemplate_NotFound() {
        when(templateRepository.findByTemplateCode("NOT_EXIST", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateService.renderTemplate("NOT_EXIST", 1L, Map.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("模板不存在");
    }
}
