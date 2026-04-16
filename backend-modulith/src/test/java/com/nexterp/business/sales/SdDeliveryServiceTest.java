package com.nexterp.business.sales;

import com.nexterp.business.sales.application.service.SdDeliveryService;
import com.nexterp.business.sales.domain.model.SdDeliveryHdr;
import com.nexterp.business.sales.domain.model.SdDeliveryItm;
import com.nexterp.business.sales.domain.repository.SdDeliveryHdrRepository;
import com.nexterp.business.sales.dto.DeliveryDTO;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SdDeliveryServiceTest {

    @Mock
    private SdDeliveryHdrRepository deliveryHdrRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SdDeliveryService deliveryService;

    private SdDeliveryHdr testDelivery;

    @BeforeEach
    void setUp() {
        testDelivery = SdDeliveryHdr.builder()
                .id(1L)
                .tenantId(1L)
                .deliveryNumber("80000001")
                .deliveryType("LF")
                .salesOrgId(1000L)
                .distributionChannel("10")
                .division("01")
                .soldToParty(100L)
                .shipToParty(100L)
                .documentDate(LocalDate.now())
                .deliveryStatus("01")
                .pickingStatus("A")
                .giStatus("A")
                .build();
        testDelivery.setItems(new ArrayList<>());
    }

    @Test
    void getDeliveryById_Exists_ReturnsDTO() {
        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(testDelivery));

        DeliveryDTO result = deliveryService.getDeliveryById(1L);

        assertNotNull(result);
        assertEquals("80000001", result.getDeliveryNumber());
        assertEquals("LF", result.getDeliveryType());
    }

    @Test
    void getDeliveryById_NotExists_ThrowsException() {
        when(deliveryHdrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> deliveryService.getDeliveryById(999L));
    }

    @Test
    void cancelDelivery_NotProcessed_CanCancel() {
        testDelivery.setDeliveryStatus("01");
        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(testDelivery));

        deliveryService.cancelDelivery(1L);

        assertTrue(testDelivery.getIsDeleted());
        verify(deliveryHdrRepository).save(testDelivery);
    }

    @Test
    void cancelDelivery_AlreadyProcessed_CannotCancel() {
        testDelivery.setDeliveryStatus("03");
        when(deliveryHdrRepository.findById(1L)).thenReturn(Optional.of(testDelivery));

        assertThrows(BusinessException.class, () -> deliveryService.cancelDelivery(1L));
    }
}
