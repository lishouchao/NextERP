package com.nexterp.business.supply;

import com.nexterp.business.supply.application.service.MmMaterialService;
import com.nexterp.business.supply.domain.model.MmMaterial;
import com.nexterp.business.supply.domain.repository.MmMaterialRepository;
import com.nexterp.business.supply.dto.MaterialDTO;
import com.nexterp.shared.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MmMaterialServiceTest {

    @Mock
    private MmMaterialRepository materialRepository;

    @InjectMocks
    private MmMaterialService materialService;

    private MmMaterial testMaterial;

    @BeforeEach
    void setUp() {
        testMaterial = new MmMaterial();
        testMaterial.setId(1L);
        testMaterial.setTenantId(1L);
        testMaterial.setMaterialNumber("MAT-001");
        testMaterial.setMaterialType("FERT");
        testMaterial.setMaterialGroup("01");
        testMaterial.setDescription("测试物料");
        testMaterial.setBaseUom("EA");
    }

    @Test
    void getMaterialById_Exists_ReturnsDTO() {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(testMaterial));

        MaterialDTO result = materialService.getMaterialById(1L);

        assertNotNull(result);
        assertEquals("MAT-001", result.getMaterialNumber());
        assertEquals("FERT", result.getMaterialType());
    }

    @Test
    void getMaterialById_NotExists_ThrowsException() {
        when(materialRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> materialService.getMaterialById(999L));
    }

    @Test
    void deleteMaterial_SoftDelete() {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(testMaterial));

        materialService.deleteMaterial(1L);

        assertTrue(testMaterial.getIsDeleted());
        verify(materialRepository).save(testMaterial);
    }
}
