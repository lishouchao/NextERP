package com.nexterp.business.sales;

import com.nexterp.business.sales.application.service.SdCreditService;
import com.nexterp.business.sales.application.service.SdSalesOrderService;
import com.nexterp.business.sales.domain.model.SdSalesOrderHdr;
import com.nexterp.business.sales.domain.model.SdSalesOrderItm;
import com.nexterp.business.sales.domain.repository.SdSalesOrderHdrRepository;
import com.nexterp.business.sales.domain.repository.SdSalesOrderItmRepository;
import com.nexterp.business.sales.dto.CreateSalesOrderRequest;
import com.nexterp.business.sales.dto.SalesOrderDTO;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private SdSalesOrderHdr testOrder;

    @BeforeEach
    void setUp() {
        testOrder = SdSalesOrderHdr.builder()
                .id(1L)
                .tenantId(1L)
                .orderNumber("60000001")
                .orderType("OR")
                .salesOrgId(1000L)
                .distributionChannel("10")
                .division("01")
                .soldToParty(100L)
                .documentDate(LocalDate.now())
                .orderStatus("01")
                .deliveryStatus("A")
                .billingStatus("A")
                .netValue(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .grossValue(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void getOrderById_Exists_ReturnsDTO() {
        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        SalesOrderDTO result = salesOrderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals("60000001", result.getOrderNumber());
        assertEquals("OR", result.getOrderType());
    }

    @Test
    void getOrderById_NotExists_ThrowsException() {
        when(orderHdrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> salesOrderService.getOrderById(999L));
    }

    @Test
    void deleteOrder_Draft_CanDelete() {
        testOrder.setOrderStatus("01");
        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        salesOrderService.deleteOrder(1L);

        assertTrue(testOrder.getIsDeleted());
        verify(orderHdrRepository).save(testOrder);
    }

    @Test
    void deleteOrder_Approved_CannotDelete() {
        testOrder.setOrderStatus("02");
        when(orderHdrRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(BusinessException.class, () -> salesOrderService.deleteOrder(1L));
    }
}
