package com.nexterp.business.production.service;

import com.nexterp.business.production.application.dto.request.ProductionOrderCreateRequest;
import com.nexterp.business.production.application.dto.request.ProductionOrderUpdateRequest;
import com.nexterp.business.production.domain.model.ProProductionOrder;
import com.nexterp.business.production.domain.repository.ProProductionOrderRepository;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 生产工单服务单元测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生产工单服务单元测试")
class ProProductionOrderServiceTest {

    @Mock
    private ProProductionOrderRepository orderRepository;

    @InjectMocks
    private ProProductionOrderService orderService;

    private ProProductionOrder testOrder;
    private ProductionOrderCreateRequest createRequest;
    private ProductionOrderUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testOrder = ProProductionOrder.builder()
                .id(1L)
                .tenantId(0L)
                .orderNo("PO20250115001")
                .productId(1L)
                .productCode("PROD001")
                .productName("产品A")
                .productSpec("标准规格")
                .plannedQty(new BigDecimal("100.00"))
                .completedQty(BigDecimal.ZERO)
                .qualifiedQty(BigDecimal.ZERO)
                .unqualifiedQty(BigDecimal.ZERO)
                .plannedStartDate(LocalDate.now().plusDays(1))
                .plannedEndDate(LocalDate.now().plusDays(7))
                .orderStatus(1) // 待开工
                .priority(1)
                .workshopId(1L)
                .workshopName("车间A")
                .status(1)
                .build();

        createRequest = new ProductionOrderCreateRequest();
        createRequest.setProductId(2L);
        createRequest.setProductCode("PROD002");
        createRequest.setProductName("产品B");
        createRequest.setPlannedQty(new BigDecimal("200.00"));
        createRequest.setPlannedStartDate(LocalDate.now().plusDays(1));
        createRequest.setPlannedEndDate(LocalDate.now().plusDays(10));
        createRequest.setPriority(1);
        createRequest.setWorkshopId(1L);
        createRequest.setWorkshopName("车间A");

        updateRequest = new ProductionOrderUpdateRequest();
        updateRequest.setPlannedQty(new BigDecimal("150.00"));
        updateRequest.setPriority(2);
    }

    @Test
    @DisplayName("创建生产工单 - 成功")
    void createOrder_Success() {
        when(orderRepository.save(any(ProProductionOrder.class))).thenReturn(testOrder);

        Long orderId = orderService.createOrder(createRequest, 0L);

        assertThat(orderId).isEqualTo(1L);
        verify(orderRepository, times(1)).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("更新生产工单 - 成功")
    void updateOrder_Success() {
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(ProProductionOrder.class))).thenReturn(testOrder);

        orderService.updateOrder(1L, updateRequest, 0L);

        verify(orderRepository, times(1)).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("更新生产工单 - 工单不存在")
    void updateOrder_NotFound() {
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(1L, updateRequest, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产工单不存在");

        verify(orderRepository, never()).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("更新生产工单 - 工单已完工不能修改")
    void updateOrder_AlreadyCompleted() {
        testOrder.setOrderStatus(5); // 已完工
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.updateOrder(1L, updateRequest, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("工单已完工，不能修改");

        verify(orderRepository, never()).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("开工 - 成功")
    void startOrder_Success() {
        testOrder.setOrderStatus(1); // 待开工
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(ProProductionOrder.class))).thenReturn(testOrder);

        orderService.startOrder(1L, 0L);

        verify(orderRepository, times(1)).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("开工 - 工单状态不允许")
    void startOrder_InvalidStatus() {
        testOrder.setOrderStatus(3); // 生产中
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.startOrder(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("工单状态不允许开工");

        verify(orderRepository, never()).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("完工 - 成功")
    void completeOrder_Success() {
        testOrder.setOrderStatus(3); // 生产中
        testOrder.setCompletedQty(new BigDecimal("100.00"));
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(ProProductionOrder.class))).thenReturn(testOrder);

        orderService.completeOrder(1L, 0L);

        verify(orderRepository, times(1)).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("完工 - 完工数量不足")
    void completeOrder_InsufficientQty() {
        testOrder.setOrderStatus(3); // 生产中
        testOrder.setCompletedQty(new BigDecimal("50.00")); // 少于计划数量
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.completeOrder(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("完工数量不足");

        verify(orderRepository, never()).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("关闭工单 - 成功")
    void closeOrder_Success() {
        testOrder.setOrderStatus(5); // 已完工
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(ProProductionOrder.class))).thenReturn(testOrder);

        orderService.closeOrder(1L, 0L);

        verify(orderRepository, times(1)).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("删除生产工单 - 成功")
    void deleteOrder_Success() {
        testOrder.setOrderStatus(1); // 待开工
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L, 0L);

        verify(orderRepository, times(1)).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("删除生产工单 - 已开工不能删除")
    void deleteOrder_AlreadyStarted() {
        testOrder.setOrderStatus(3); // 生产中
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.deleteOrder(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("工单已开工，不能删除");

        verify(orderRepository, never()).save(any(ProProductionOrder.class));
    }

    @Test
    @DisplayName("获取工单详情 - 成功")
    void getOrderById_Success() {
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        ProProductionOrder order = orderService.getOrderById(1L, 0L);

        assertThat(order).isNotNull();
        assertThat(order.getOrderNo()).isEqualTo("PO20250115001");
        assertThat(order.getProductName()).isEqualTo("产品A");
    }

    @Test
    @DisplayName("获取工单详情 - 不存在")
    void getOrderById_NotFound() {
        when(orderRepository.findByIdAndTenantIdAndIsDeletedFalse(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(1L, 0L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("生产工单不存在");
    }
}
