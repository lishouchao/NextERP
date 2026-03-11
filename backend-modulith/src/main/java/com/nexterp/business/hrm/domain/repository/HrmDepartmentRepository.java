package com.nexterp.business.hrm.domain.repository;

import com.nexterp.business.hrm.domain.model.HrmDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门 Repository
 *
 * @author NextERP
 */
@Repository
public interface HrmDepartmentRepository extends JpaRepository<HrmDepartment, Long> {

    /**
     * 根据父部门ID查询子部门
     */
    List<HrmDepartment> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 根据部门编码查询
     */
    HrmDepartment findByDeptCode(String deptCode);

    /**
     * 查询所有启用的部门
     */
    List<HrmDepartment> findByStatusOrderBySortAsc(Integer status);

    /**
     * 根据负责人ID查询部门
     */
    List<HrmDepartment> findByLeaderId(Long leaderId);
}
