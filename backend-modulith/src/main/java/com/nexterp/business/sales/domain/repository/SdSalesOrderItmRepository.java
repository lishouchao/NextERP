package com.nexterp.business.sales.domain.repository;

import com.nexterp.business.sales.domain.model.SdSalesOrderItm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 销售订单项仓储接口
 *
 * @author NextERP
 */
@Repository
public interface SdSalesOrderItmRepository extends JpaRepository<SdSalesOrderItm, Long> {

    /**
     * 根据订单头ID查询所有行项目
     *
     * @param orderHdrId 订单头ID
     * @return 行项目列表
     */
    List<SdSalesOrderItm> findByOrderHdrId(Long orderHdrId);

    /**
     * 根据订单头ID查询所有行项目 (按行号升序)
     *
     * @param orderHdrId 订单头ID
     * @return 行项目列表
     */
    List<SdSalesOrderItm> findByOrderHdrIdOrderByItemNumberAsc(Long orderHdrId);
}
