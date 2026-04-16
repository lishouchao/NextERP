package com.nexterp.business.supply.domain.repository;

import com.nexterp.business.supply.domain.model.MmStorageBin;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 仓位仓储接口
 *
 * @author NextERP
 */
@Repository
public interface MmStorageBinRepository extends TenantAwareRepository<MmStorageBin> {

    /**
     * 根据仓库/存储类型/仓位查询
     *
     * @param warehouseNumber 仓库号
     * @param storageType     存储类型
     * @param storageBin      仓位
     * @return 仓位
     */
    Optional<MmStorageBin> findByWarehouseNumberAndStorageTypeAndStorageBin(String warehouseNumber, String storageType, String storageBin);

    /**
     * 根据仓库号分页查询仓位
     *
     * @param warehouseNumber 仓库号
     * @param pageable        分页
     * @return 仓位分页
     */
    Page<MmStorageBin> findByWarehouseNumber(String warehouseNumber, Pageable pageable);
}
