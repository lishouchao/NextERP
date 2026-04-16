package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdBillingHdr;
import com.nexterp.business.sales.domain.model.SdBillingItm;
import com.nexterp.business.sales.domain.model.SdDeliveryHdr;
import com.nexterp.business.sales.domain.repository.SdBillingHdrRepository;
import com.nexterp.business.sales.domain.repository.SdDeliveryHdrRepository;
import com.nexterp.business.sales.dto.BillingDTO;
import com.nexterp.business.sales.dto.CreateBillingRequest;
import com.nexterp.business.sales.event.BillingPostedEvent;
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
import static org.mockito.Mockito.*;

/**
 * 开票服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class SdBillingServiceTest {

    @Mock
    private SdBillingHdrRepository billingHdrRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SdDeliveryHdrRepository deliveryHdrRepository;

    @InjectMocks
    private SdBillingService billingService;

    @Test
    @DisplayName("创建开票凭证 - 成功")
    void testCreateBilling_Success() {
        // Given
        CreateBillingRequest.CreateBillingItemRequest itemRequest = CreateBillingRequest.CreateBillingItemRequest.builder()
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .billedQty(new BigDecimal("10.000"))
                .salesUnit("EA")
                .netPrice(new BigDecimal("100.00"))
                .deliveryId(1L)
                .deliveryItemId(1L)
                .build();

        CreateBillingRequest request = CreateBillingRequest.builder()
                .tenantId(1L)
                .billingType("F2")
                .billingDate(LocalDate.of(2026, 4, 10))
                .deliveryId(1L)
                .orderId(1L)
                .remark("测试开票")
                .items(List.of(itemRequest))
                .build();

        // Mock: delivery lookup for header info
        SdDeliveryHdr deliveryHdr = SdDeliveryHdr.builder()
                .id(1L)
                .deliveryNumber("80000001")
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .orderId(1L)
                .items(new ArrayList<>())
                .build();

        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(deliveryHdr));

        // Mock: count() for billing number generation
        when(billingHdrRepository.count()).thenReturn(0L);

        // Mock: save() returns entity with ID
        SdBillingHdr savedHdr = SdBillingHdr.builder()
                .id(1L)
                .billingNumber("90000001")
                .billingType("F2")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .billingDate(LocalDate.of(2026, 4, 10))
                .billingStatus("01")
                .deliveryId(1L)
                .orderId(1L)
                .netValue(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .tenantId(1L)
                .items(new ArrayList<>())
                .build();

        when(billingHdrRepository.save(any(SdBillingHdr.class))).thenReturn(savedHdr);

        // When
        Long billingId = billingService.createBilling(request);

        // Then
        assertNotNull(billingId);
        assertEquals(1L, billingId);

        // Verify saved entity
        ArgumentCaptor<SdBillingHdr> captor = ArgumentCaptor.forClass(SdBillingHdr.class);
        verify(billingHdrRepository).save(captor.capture());

        SdBillingHdr captured = captor.getValue();
        assertNotNull(captured.getBillingNumber());
        assertTrue(captured.getBillingNumber().startsWith("9"));
        assertEquals("01", captured.getBillingStatus());
        // Verify header info was copied from delivery
        assertEquals(1000L, captured.getSalesOrgId());
        assertEquals("01", captured.getDistributionChannel());
        assertEquals("01", captured.getDivision());
        assertEquals(10000L, captured.getSoldToParty());

        // Verify delivery was looked up
        verify(deliveryHdrRepository).findById(1L);
    }

    @Test
    @DisplayName("过账开票凭证 - 状态从01变为02, 发布事件")
    void testPostBilling_Success() {
        // Given
        SdBillingItm item = SdBillingItm.builder()
                .id(1L)
                .billingHdrId(1L)
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .billedQty(new BigDecimal("10.000"))
                .salesUnit("EA")
                .netPrice(new BigDecimal("100.00"))
                .netValue(new BigDecimal("1000.00"))
                .taxCode("X1")
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .build();

        SdBillingHdr hdr = SdBillingHdr.builder()
                .id(1L)
                .billingNumber("90000001")
                .billingType("F2")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .billingDate(LocalDate.of(2026, 4, 10))
                .billingStatus("01")
                .deliveryId(1L)
                .orderId(1L)
                .netValue(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .tenantId(1L)
                .items(new ArrayList<>(List.of(item)))
                .build();

        when(billingHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(billingHdrRepository.save(any(SdBillingHdr.class))).thenReturn(hdr);

        // When
        billingService.postBilling(1L);

        // Then
        ArgumentCaptor<SdBillingHdr> captor = ArgumentCaptor.forClass(SdBillingHdr.class);
        verify(billingHdrRepository).save(captor.capture());

        SdBillingHdr saved = captor.getValue();
        assertEquals("02", saved.getBillingStatus());

        // Verify event was published
        ArgumentCaptor<BillingPostedEvent> eventCaptor = ArgumentCaptor.forClass(BillingPostedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        BillingPostedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.billingId());
        assertEquals("90000001", event.billingNumber());
        assertEquals(1L, event.tenantId());
        assertEquals(new BigDecimal("1000.00"), event.netValue());
        assertEquals(new BigDecimal("130.00"), event.taxAmount());

        verify(billingHdrRepository).findById(1L);
    }

    @Test
    @DisplayName("取消开票凭证 - 状态从02变为03, 创建冲销凭证")
    void testCancelBilling_Success() {
        // Given
        SdBillingItm item = SdBillingItm.builder()
                .id(1L)
                .billingHdrId(1L)
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .billedQty(new BigDecimal("10.000"))
                .salesUnit("EA")
                .netPrice(new BigDecimal("100.00"))
                .netValue(new BigDecimal("1000.00"))
                .taxCode("X1")
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .deliveryId(1L)
                .deliveryItemId(1L)
                .build();

        SdBillingHdr hdr = SdBillingHdr.builder()
                .id(1L)
                .billingNumber("90000001")
                .billingType("F2")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .billToParty(10002L)
                .payerParty(10003L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .billingDate(LocalDate.of(2026, 4, 10))
                .billingStatus("02")  // 已过账状态
                .deliveryId(1L)
                .orderId(1L)
                .remark("原开票凭证")
                .netValue(new BigDecimal("1000.00"))
                .taxAmount(new BigDecimal("130.00"))
                .grossValue(new BigDecimal("1130.00"))
                .tenantId(1L)
                .items(new ArrayList<>(List.of(item)))
                .build();

        when(billingHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(billingHdrRepository.count()).thenReturn(1L);
        when(billingHdrRepository.save(any(SdBillingHdr.class))).thenAnswer(invocation -> {
            SdBillingHdr toSave = invocation.getArgument(0);
            if (toSave.getId() == null) {
                toSave.setId(2L); // reversal gets a new ID
            }
            return toSave;
        });

        // When
        billingService.cancelBilling(1L);

        // Then
        // save() should be called twice: once for original (status -> 03), once for reversal
        verify(billingHdrRepository, times(2)).save(any(SdBillingHdr.class));

        // Capture all saves to verify
        ArgumentCaptor<SdBillingHdr> captor = ArgumentCaptor.forClass(SdBillingHdr.class);
        verify(billingHdrRepository, times(2)).save(captor.capture());

        List<SdBillingHdr> savedEntities = captor.getAllValues();

        // First save: original billing status updated to "03"
        SdBillingHdr originalSaved = savedEntities.get(0);
        assertEquals("03", originalSaved.getBillingStatus());
        assertEquals(1L, originalSaved.getId());

        // Second save: reversal billing
        SdBillingHdr reversalSaved = savedEntities.get(1);
        assertEquals("S1", reversalSaved.getBillingType());  // S1 = cancellation invoice
        assertEquals("02", reversalSaved.getBillingStatus());
        assertEquals(new BigDecimal("-1000.00"), reversalSaved.getNetValue());
        assertEquals(new BigDecimal("-130.00"), reversalSaved.getTaxAmount());
        assertEquals(new BigDecimal("-1130.00"), reversalSaved.getGrossValue());

        // Verify reversal items have negative values
        assertNotNull(reversalSaved.getItems());
        assertEquals(1, reversalSaved.getItems().size());
        assertEquals(new BigDecimal("-1000.00"), reversalSaved.getItems().get(0).getNetValue());
        assertEquals(new BigDecimal("-130.00"), reversalSaved.getItems().get(0).getTaxAmount());
    }
}
