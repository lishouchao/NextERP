package com.nexterp.business.supply;

import com.nexterp.business.supply.application.service.MmInventoryService;
import com.nexterp.business.supply.domain.model.MmStock;
import com.nexterp.business.supply.domain.repository.MmMaterialDocHdrRepository;
import com.nexterp.business.supply.domain.repository.MmStockRepository;
import com.nexterp.business.supply.dto.StockDTO;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MmInventoryServiceTest {

    @Mock
    private MmMaterialDocHdrRepository materialDocHdrRepository;

    @Mock
    private MmStockRepository stockRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MmInventoryService inventoryService;

    private MmStock testStock;

    @BeforeEach
    void setUp() {
        testStock = MmStock.builder()
                .id(1L)
                .tenantId(1L)
                .materialId(1L)
                .materialCode("MAT-001")
                .plantId(100L)
                .plantCode("1000")
                .slocId(10L)
                .slocCode("0001")
                .unrestrictedStock(new BigDecimal("100.000"))
                .qualityStock(BigDecimal.ZERO)
                .blockedStock(BigDecimal.ZERO)
                .unrestrictedValue(new BigDecimal("10000.00"))
                .currency("CNY")
                .build();
    }

    @Test
    void getStock_Exists_ReturnsDTO() {
        when(stockRepository.findByMaterialIdAndPlantIdAndSlocIdAndTenantId(1L, 100L, 10L, 1L))
                .thenReturn(Optional.of(testStock));

        StockDTO result = inventoryService.getStock(1L, 100L, 10L, 1L);

        assertNotNull(result);
        assertEquals("MAT-001", result.getMaterialCode());
        assertEquals(new BigDecimal("100.000"), result.getUnrestrictedStock());
    }

    @Test
    void getStock_NotExists_ThrowsException() {
        when(stockRepository.findByMaterialIdAndPlantIdAndSlocIdAndTenantId(1L, 100L, 10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> inventoryService.getStock(1L, 100L, 10L, 1L));
    }
}
