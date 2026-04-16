package com.nexterp.business.sales.application.service;

import com.nexterp.business.sales.domain.model.SdDeliveryHdr;
import com.nexterp.business.sales.domain.model.SdDeliveryItm;
import com.nexterp.business.sales.domain.repository.SdDeliveryHdrRepository;
import com.nexterp.business.sales.dto.CreateDeliveryRequest;
import com.nexterp.business.sales.dto.DeliveryDTO;
import com.nexterp.business.sales.event.DeliveryCreatedEvent;
import com.nexterp.business.sales.event.GoodsIssuePostedEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 交货单服务测试
 *
 * @author NextERP
 */
@ExtendWith(MockitoExtension.class)
class SdDeliveryServiceTest {

    @Mock
    private SdDeliveryHdrRepository deliveryHdrRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SdDeliveryService deliveryService;

    @Test
    @DisplayName("创建交货单 - 成功")
    void testCreateDelivery_Success() {
        // Given
        CreateDeliveryRequest.CreateDeliveryItemRequest itemRequest = CreateDeliveryRequest.CreateDeliveryItemRequest.builder()
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .deliveryQty(new BigDecimal("10.000"))
                .salesUnit("EA")
                .plantId(1000L)
                .slocId(1001L)
                .orderItemId(1L)
                .build();

        CreateDeliveryRequest request = CreateDeliveryRequest.builder()
                .tenantId(1L)
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .plannedGiDate(LocalDate.of(2026, 4, 15))
                .shippingPoint("0001")
                .orderId(1L)
                .remark("测试交货单")
                .items(List.of(itemRequest))
                .build();

        // Mock: count() for delivery number generation
        when(deliveryHdrRepository.count()).thenReturn(0L);

        // Mock: save() returns the entity with ID
        SdDeliveryHdr savedHdr = SdDeliveryHdr.builder()
                .id(1L)
                .deliveryNumber("80000001")
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .plannedGiDate(LocalDate.of(2026, 4, 15))
                .shippingPoint("0001")
                .orderId(1L)
                .deliveryStatus("01")
                .pickingStatus("A")
                .giStatus("A")
                .tenantId(1L)
                .items(new ArrayList<>())
                .build();

        when(deliveryHdrRepository.save(any(SdDeliveryHdr.class))).thenReturn(savedHdr);

        // When
        Long deliveryId = deliveryService.createDelivery(request);

        // Then
        assertNotNull(deliveryId);
        assertEquals(1L, deliveryId);

        // Verify saved entity
        ArgumentCaptor<SdDeliveryHdr> captor = ArgumentCaptor.forClass(SdDeliveryHdr.class);
        verify(deliveryHdrRepository).save(captor.capture());

        SdDeliveryHdr captured = captor.getValue();
        assertNotNull(captured.getDeliveryNumber());
        assertTrue(captured.getDeliveryNumber().startsWith("8"));
        assertEquals("01", captured.getDeliveryStatus());
        assertEquals("A", captured.getPickingStatus());
        assertEquals("A", captured.getGiStatus());

        // Verify event was published
        verify(eventPublisher).publishEvent(any(DeliveryCreatedEvent.class));
    }

    @Test
    @DisplayName("拣配交货单 - 完全拣配成功")
    void testPickDelivery_Success() {
        // Given
        SdDeliveryItm item = SdDeliveryItm.builder()
                .id(1L)
                .deliveryHdrId(1L)
                .itemNumber(10)
                .materialId(100L)
                .materialCode("MAT001")
                .description("测试物料")
                .deliveryQty(new BigDecimal("10.000"))
                .pickedQty(BigDecimal.ZERO)
                .salesUnit("EA")
                .plantId(1000L)
                .slocId(1001L)
                .build();

        SdDeliveryHdr hdr = SdDeliveryHdr.builder()
                .id(1L)
                .deliveryNumber("80000001")
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .deliveryStatus("01")
                .pickingStatus("A")
                .giStatus("A")
                .items(new ArrayList<>(List.of(item)))
                .build();

        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(deliveryHdrRepository.save(any(SdDeliveryHdr.class))).thenReturn(hdr);

        Map<String, Object> pickItem = new HashMap<>();
        pickItem.put("itemNumber", 10);
        pickItem.put("pickedQty", "10.000");
        List<Map<String, Object>> pickItems = List.of(pickItem);

        // When
        deliveryService.pickDelivery(1L, pickItems);

        // Then
        ArgumentCaptor<SdDeliveryHdr> captor = ArgumentCaptor.forClass(SdDeliveryHdr.class);
        verify(deliveryHdrRepository).save(captor.capture());

        SdDeliveryHdr saved = captor.getValue();
        // Fully picked -> pickingStatus should be "C" and deliveryStatus should be "03"
        assertEquals("C", saved.getPickingStatus());
        assertEquals("03", saved.getDeliveryStatus());

        // Verify item pickedQty was updated
        assertEquals(new BigDecimal("10.000"), saved.getItems().get(0).getPickedQty());
    }

    @Test
    @DisplayName("发货过账 - 成功, 状态03变为04")
    void testPostGoodsIssue_Success() {
        // Given
        SdDeliveryHdr hdr = SdDeliveryHdr.builder()
                .id(1L)
                .deliveryNumber("80000001")
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .deliveryStatus("03")
                .pickingStatus("C")
                .giStatus("A")
                .tenantId(1L)
                .items(new ArrayList<>())
                .build();

        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(deliveryHdrRepository.save(any(SdDeliveryHdr.class))).thenReturn(hdr);

        LocalDate actualGiDate = LocalDate.of(2026, 4, 12);

        // When
        deliveryService.postGoodsIssue(1L, actualGiDate);

        // Then
        ArgumentCaptor<SdDeliveryHdr> captor = ArgumentCaptor.forClass(SdDeliveryHdr.class);
        verify(deliveryHdrRepository).save(captor.capture());

        SdDeliveryHdr saved = captor.getValue();
        assertEquals("04", saved.getDeliveryStatus());
        assertEquals("B", saved.getGiStatus());
        assertEquals(actualGiDate, saved.getActualGiDate());

        // Verify event was published
        ArgumentCaptor<GoodsIssuePostedEvent> eventCaptor = ArgumentCaptor.forClass(GoodsIssuePostedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        GoodsIssuePostedEvent event = eventCaptor.getValue();
        assertEquals(1L, event.deliveryId());
        assertEquals("80000001", event.deliveryNumber());
        assertEquals(1L, event.tenantId());
        assertEquals(actualGiDate, event.actualGiDate());
    }

    @Test
    @DisplayName("取消交货单 - 成功, 仅未处理状态(01)")
    void testCancelDelivery_Success() {
        // Given
        SdDeliveryHdr hdr = SdDeliveryHdr.builder()
                .id(1L)
                .deliveryNumber("80000001")
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("01")
                .division("01")
                .soldToParty(10000L)
                .shipToParty(10001L)
                .documentDate(LocalDate.of(2026, 4, 10))
                .deliveryStatus("01")
                .pickingStatus("A")
                .giStatus("A")
                .items(new ArrayList<>())
                .build();

        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(hdr));
        when(deliveryHdrRepository.save(any(SdDeliveryHdr.class))).thenReturn(hdr);

        // When
        deliveryService.cancelDelivery(1L);

        // Then
        ArgumentCaptor<SdDeliveryHdr> captor = ArgumentCaptor.forClass(SdDeliveryHdr.class);
        verify(deliveryHdrRepository).save(captor.capture());

        SdDeliveryHdr saved = captor.getValue();
        assertTrue(saved.getIsDeleted());

        verify(deliveryHdrRepository).findById(1L);
        verify(deliveryHdrRepository).save(any(SdDeliveryHdr.class));
    }
}
