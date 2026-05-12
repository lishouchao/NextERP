package com.nexterp.business.production.domain.repository;

import com.nexterp.business.production.domain.model.ProBomDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BOM明细仓储接口
 *
 * @author NextERP
 */
@Repository
public interface ProBomDetailRepository extends JpaRepository<ProBomDetail, Long> {

    /**
     * 根据BOM ID按行号排序查询明细
     *
     * @param bomId BOM ID
     * @return BOM明细列表
     */
    List<ProBomDetail> findByBomIdOrderByLineNo(Long bomId);

    /**
     * 根据子件物料ID查询明细
     *
     * @param componentId 子件物料ID
     * @return BOM明细列表
     */
    List<ProBomDetail> findByComponentId(Long componentId);

    /**
     * 根据BOM ID删除所有明细
     *
     * @param bomId BOM ID
     */
    void deleteByBomId(Long bomId);
}
