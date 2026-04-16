package com.nexterp.business.supply;

import com.nexterp.business.supply.application.service.MmPurchaseOrderService;
import com.nexterp.business.supply.domain.model.MmPurchaseOrderHdr;
import com.nexterp.business.supply.domain.repository.MmPurchaseOrderHdrRepository;
import com.nexterp.business.supply.dto.PurchaseOrderDTO;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MmPurchaseOrderServiceTest {

    @Mock
    private MmPurchaseOrderHdrRepository poHdrRepository;

    @InjectMocks
    private MmPurchaseOrderService purchaseOrderService;

    private MmPurchaseOrderHdr testPO;

    @BeforeEach
    void setUp() {
        testPO = MmPurchaseOrderHdr.builder()
                .id(1L)
                .tenantId(1L)
                .poNumber("45000001")
                .poType("NB")
                .vendorId(100L)
                .vendorCode("V001")
                .purchasingOrg("1000")
                .purchasingGroup("001")
                .companyCode("1000")
                .currency("CNY")
                .documentDate(LocalDate.now())
                .status("0")
                .releaseStatus("0")
                .build();
    }

    @Test
    void getPurchaseOrderById_Exists_ReturnsDTO() {
        when(poHdrRepository.findById(1L)).thenReturn(Optional.of(testPO));

        PurchaseOrderDTO result = purchaseOrderService.getPurchaseOrderById(1L);

        assertNotNull(result);
        assertEquals("45000001", result.getPoNumber());
        assertEquals("NB", result.getPoType());
    }

    @Test
    void getPurchaseOrderById_NotExists_ThrowsException() {
        when(poHdrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> purchaseOrderService.getPurchaseOrderById(999L));
    }

    @Test
    void submitPurchaseOrder_ChangesStatus() {
        testPO.setStatus("0");
        when(poHdrRepository.findById(1L)).thenReturn(Optional.of(testPO));

        purchaseOrderService.submitPurchaseOrder(1L);

        assertEquals("1", testPO.getStatus());
        verify(poHdrRepository).save(testPO);
    }
}
