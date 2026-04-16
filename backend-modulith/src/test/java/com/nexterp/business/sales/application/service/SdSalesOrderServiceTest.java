package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdSalesOrderHdr;
import com.nexterp.business.sales.domain.model.SdSalesOrderItm;
import com.nexterp.business.sales.domain.repository.SdSalesOrderHdrRepository;
import com.nexterp.business.sales.domain.repository.SdSalesOrderItmRepository;
import com.nexterp.business.sales.dto.CreateSalesOrderRequest;
import com.nexterp.business.sales.dto.SalesOrderDTO;
import com.nexterp.business.sales.event.SalesOrderApprovedEvent;
import com.nexterp.business.sales.event.SalesOrderCreatedEvent;
import com.nexterp.business.sales.event.SalesOrderRejectedEvent;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 销售订单服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class SdSalesOrderServiceTest {

    @Mock
    private SdSalesOrderHdrRepository orderHdrRepository;

    @Mock
    private SdSalesOrderItmRepository orderItmRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SdCreditService creditService;

    @InjectMocks
    private SdSalesOrderService salesOrderService;

    @Test
    @DisplayName("创建销售订单 - 成功")
    void testCreateOrder_Success() {
        // Given
        CreateSalesOrderRequest.CreateSalesOrderItemRequest itemRequest = CreateSalesOrderRequest.CreateSalesOrderItemRequest.builder()
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .orderedQty(new BigDecimal("10.000"))
                .salesUnit("EA")
                .netPrice(new BigDecimal("100.00"))
                .plantId(1000L)
                .slocId(1001L)
                .itemCategory("TAN")
                .build();

        CreateSalesOrderRequest request = CreateSalesOrderRequest.builder()
                .tenantId(1L)
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .billToParty(10002L)
                .payerParty(10003L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .requestedDeliveryDate(LocalDate.of(2026, 4, 15))
                .purchaseOrder("PO-2026-001")
                .remark("测试订单")
                .items(List.of(itemRequest))
                .build();

        // Mock: count() for order number generation
        when(orderHdrRepository.count()).thenReturn(0L);

        // Mock: save() returns the entity with ID set
        SdSalesOrderHdr savedHdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .billToParty(10002L)
                .payerParty(10003L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .requestedDeliveryDate(LocalDate.of(2026, 4, 15))
                .purchaseOrder("PO-2026-001")
                .remark("测试订单")
                .orderStatus("01")
                .deliveryStatus("A")
                .billingStatus("A")
                .netValue(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .tenantId(1L)
                .build();

        when(orderHdrRepository.save(any(SdSalesOrderHdr.class))).thenReturn(savedHdr);

        // When
        Long orderId = salesOrderService.createOrder(request);

        // Then
        assertNotNull(orderId);
        assertEquals(1L, orderId);

        // Verify saved entity fields via ArgumentCaptor
        ArgumentCaptor<SdSalesOrderHdr> hdrCaptor = ArgumentCaptor.forClass(SdSalesOrderHdr.class);
        verify(orderHdrRepository).save(hdrCaptor.capture());

        SdSalesOrderHdr captured = hdrCaptor.getValue();
        // Verify order number format: starts with "6" and is 8 characters total
        assertNotNull(captured.getOrderNumber());
        assertTrue(captured.getOrderNumber().startsWith("6"));
        assertEquals(8, captured.getOrderNumber().length());
        // Verify status is "01" (draft)
        assertEquals("01", captured.getOrderStatus());

        // Verify event was published
        verify(eventPublisher).publishEvent(any(SalesOrderCreatedEvent.class));
    }

    @Test
    @DisplayName("根据ID获取销售订单 - 成功")
    void testGetOrderById_Success() {
        // Given
        SdSalesOrderItm item = SdSalesOrderItm.builder()
                .id(1L)
                .orderHdrId(1L)
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .orderedQty(new BigDecimal("10.000"))
                .deliveredQty(BigDecimal.ZERO)
                .invoicedQty(BigDecimal.ZERO)
                .salesUnit("EA")
                .netPrice(new BigDecimal("100.00"))
                .netValue(new BigDecimal("1000.00"))
                .plantId(1000L)
                .slocId(1001L)
                .itemCategory("TAN")
                .build();

        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .billToParty(10002L)
                .payerParty(10003L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .requestedDeliveryDate(LocalDate.of(2026, 4, 15))
                .netValue(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .orderStatus("01")
                .deliveryStatus("A")
                .billingStatus("A")
                .purchaseOrder("PO-2026-001")
                .remark("测试订单")
                .items(new ArrayList<>(List.of(item)))
                .build();

        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));

        // When
        SalesOrderDTO dto = salesOrderService.getOrderById(1L);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("60000001", dto.getOrderNumber());
        assertEquals("OR", dto.getOrderType());
        assertEquals("01", dto.getOrderStatus());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(10, dto.getItems().get(0).getItemNumber());
        assertEquals("MAT001", dto.getItems().get(0).getMaterialCode());
        assertEquals(new BigDecimal("1000.00"), dto.getItems().get(0).getNetValue());

        verify(orderHdrRepository).findById(1L);
    }

    @Test
    @DisplayName("提交销售订单 - 状态从01变为02")
    void testSubmitOrder_Success() {
        // Given
        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .orderStatus("01")
                .deliveryStatus("A")
                .billingStatus("A")
                .items(new ArrayList<>())
                .build();

        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(orderHdrRepository.save(any(SdSalesOrderHdr.class))).thenReturn(hdr);

        // When
        salesOrderService.submitOrder(1L);

        // Then
        ArgumentCaptor<SdSalesOrderHdr> captor = ArgumentCaptor.forClass(SdSalesOrderHdr.class);
        verify(orderHdrRepository).save(captor.capture());

        SdSalesOrderHdr saved = captor.getValue();
        assertEquals("02", saved.getOrderStatus());

        verify(orderHdrRepository).findById(1L);
    }

    @Test
    @DisplayName("审批通过销售订单 - 状态从02变为03, 发布事件")
    void testApproveOrder_Success() {
        // Given
        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .orderStatus("02")
                .deliveryStatus("A")
                .billingStatus("A")
                .tenantId(1L)
                .items(new ArrayList<>())
                .build();

        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(orderHdrRepository.save(any(SdSalesOrderHdr.class))).thenReturn(hdr);

        // When
        salesOrderService.approveOrder(1L, "admin");

        // Then
        ArgumentCaptor<SdSalesOrderHdr> captor = ArgumentCaptor.forClass(SdSalesOrderHdr.class);
        verify(orderHdrRepository).save(captor.capture());

        SdSalesOrderHdr saved = captor.getValue();
        assertEquals("03", saved.getOrderStatus());

        // Verify event was published
        ArgumentCaptor<SalesOrderApprovedEvent> eventCaptor = ArgumentCaptor.forClass(SalesOrderApprovedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        SalesOrderApprovedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.orderId());
        assertEquals("60000001", event.orderNumber());
        assertEquals(1L, event.tenantId());
        assertEquals("admin", event.approvedBy());
    }

    @Test
    @DisplayName("审批拒绝销售订单 - 状态回到01, 发布事件")
    void testRejectOrder_Success() {
        // Given
        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .orderStatus("02")
                .deliveryStatus("A")
                .billingStatus("A")
                .tenantId(1L)
                .items(new ArrayList<>())
                .build();

        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(orderHdrRepository.save(any(SdSalesOrderHdr.class))).thenReturn(hdr);

        // When
        salesOrderService.rejectOrder(1L, "admin", "金额超出预算");

        // Then
        ArgumentCaptor<SdSalesOrderHdr> captor = ArgumentCaptor.forClass(SdSalesOrderHdr.class);
        verify(orderHdrRepository).save(captor.capture());

        SdSalesOrderHdr saved = captor.getValue();
        assertEquals("01", saved.getOrderStatus());

        // Verify event was published
        ArgumentCaptor<SalesOrderRejectedEvent> eventCaptor = ArgumentCaptor.forClass(SalesOrderRejectedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        SalesOrderRejectedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.orderId());
        assertEquals("60000001", event.orderNumber());
        assertEquals(1L, event.tenantId());
        assertEquals("admin", event.rejectedBy());
        assertEquals("金额超出预算", event.rejectReason());
    }

    @Test
    @DisplayName("删除销售订单 - 仅草稿状态(01)允许删除")
    void testDeleteOrder_OnlyInDraftStatus() {
        // Given
        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .orderStatus("01")
                .deliveryStatus("A")
                .billingStatus("A")
                .items(new ArrayList<>())
                .build();

        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(orderHdrRepository.save(any(SdSalesOrderHdr.class))).thenReturn(hdr);

        // When
        salesOrderService.deleteOrder(1L);

        // Then
        ArgumentCaptor<SdSalesOrderHdr> captor = ArgumentCaptor.forClass(SdSalesOrderHdr.class);
        verify(orderHdrRepository).save(captor.capture());

        SdSalesOrderHdr saved = captor.getValue();
        assertTrue(saved.getIsDeleted());

        verify(orderHdrRepository).findById(1L);
        verify(orderHdrRepository).save(any(SdSalesOrderHdr.class));
    }

    @Test
    @DisplayName("删除销售订单 - 非草稿状态抛出BusinessException")
    void testDeleteOrder_NotInDraftStatus() {
        // Given
        SdSalesOrderHdr hdr = SdSalesOrderHdr.builder()
                .id(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .orderStatus("02")  // 已提交状态, 非草稿
                .deliveryStatus("A")
                .billingStatus("A")
                .items(new ArrayList<>())
                .build();

        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            salesOrderService.deleteOrder(1L);
        });

        // Verify no save was performed
        verify(orderHdrRepository, never()).save(any(SdSalesOrderHdr.class));
    }
}
