package com.nexterp.business.sales;

import com.nexterp.business.sales.application.service.SdBillingService;
import com.nexterp.business.sales.domain.model.SdBillingHdr;
import com.nexterp.business.sales.domain.model.SdBillingItm;
import com.nexterp.business.sales.domain.repository.SdBillingHdrRepository;
import com.nexterp.business.sales.domain.repository.SdDeliveryHdrRepository;
import com.nexterp.business.sales.dto.BillingDTO;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SdBillingServiceTest {

    @Mock
    private SdBillingHdrRepository billingHdrRepository;

    @Mock
    private SdDeliveryHdrRepository deliveryHdrRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SdBillingService billingService;

    private SdBillingHdr testBilling;

    @BeforeEach
    void setUp() {
        testBilling = SdBillingHdr.builder()
                .id(1L)
                .tenantId(1L)
                .billingNumber("90000001")
                .billingType("F2")
                .salesOrgId(1000L)
                .distributionChannel("10")
                .division("01")
                .soldToParty(100L)
                .documentDate(LocalDate.now())
                .billingDate(LocalDate.now())
                .netValue(new BigDecimal("100000.00"))
                .taxAmount(new BigDecimal("13000.00"))
                .grossValue(new BigDecimal("113000.00"))
                .billingStatus("01")
                .build();
        testBilling.setItems(new ArrayList<>());
    }

    @Test
    void getBillingById_Exists_ReturnsDTO() {
        when(billingHdrRepository.findById(1L)).thenReturn(Optional.of(testBilling));

        BillingDTO result = billingService.getBillingById(1L);

        assertNotNull(result);
        assertEquals("90000001", result.getBillingNumber());
        assertEquals("F2", result.getBillingType());
        assertEquals(new BigDecimal("100000.00"), result.getNetValue());
    }

    @Test
    void getBillingById_NotExists_ThrowsException() {
        when(billingHdrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> billingService.getBillingById(999L));
    }

    @Test
    void postBilling_Created_CanPost() {
        testBilling.setBillingStatus("01");
        when(billingHdrRepository.findById(1L)).thenReturn(Optional.of(testBilling));

        billingService.postBilling(1L);

        assertEquals("02", testBilling.getBillingStatus());
        verify(billingHdrRepository).save(testBilling);
    }

    @Test
    void postBilling_AlreadyPosted_CannotPost() {
        testBilling.setBillingStatus("02");
        when(billingHdrRepository.findById(1L)).thenReturn(Optional.of(testBilling));

        assertThrows(BusinessException.class, () -> billingService.postBilling(1L));
    }
}
