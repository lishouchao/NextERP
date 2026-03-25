package com.nexterp.business.hrm.domain.model;

import com.nexterp.shared.data.entity.TimeValidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * InfoType 0016 - 合同 (Contract)
 * 对标 SAP IT0016
 *
 * 存储员工的劳动合同信息
 *
 * @author NextERP
 */
@Data
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hr_it0016_contract", indexes = {
    @Index(name = "idx_it0016_employee", columnList = "tenant_id, employee_id, valid_from"),
    @Index(name = "idx_it0016_type", columnList = "tenant_id, employee_id, contract_type")
})
public class HrIt0016Contract extends TimeValidEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工内码
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", nullable = false, length = 8)
    private String employeeNo;

    /**
     * 合同编号
     */
    @Column(name = "contract_no", length = 20)
    private String contractNo;

    /**
     * 合同类型 (1-固定期限 2-无固定期限 3-以完成一定工作为期限 4-劳务协议 5-实习协议)
     */
    @Column(name = "contract_type", nullable = false, length = 1)
    private String contractType;

    /**
     * 合同开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 合同结束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 试用期开始日期
     */
    @Column(name = "probation_start_date")
    private LocalDate probationStartDate;

    /**
     * 试用期结束日期
     */
    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    /**
     * 试用期月数
     */
    @Column(name = "probation_months")
    private Integer probationMonths;

    /**
     * 签约次数
     */
    @Column(name = "sign_times")
    @Builder.Default
    private Integer signTimes = 1;

    /**
     * 签订日期
     */
    @Column(name = "sign_date")
    private LocalDate signDate;

    /**
     * 合同状态 (1-有效 2-到期 3-解除 4-终止)
     */
    @Column(name = "contract_status", nullable = false, length = 1)
    @Builder.Default
    private String contractStatus = "1";

    /**
     * 工作地点
     */
    @Column(name = "work_location", length = 200)
    private String workLocation;

    /**
     * 工作岗位
     */
    @Column(name = "work_position", length = 100)
    private String workPosition;

    /**
     * 工作内容描述
     */
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    /**
     * 工时制度 (1-标准工时 2-综合工时 3-不定时工时)
     */
    @Column(name = "work_hours_type", length = 1)
    @Builder.Default
    private String workHoursType = "1";

    /**
     * 每周工作小时数
     */
    @Column(name = "weekly_hours")
    @Builder.Default
    private Integer weeklyHours = 40;

    /**
     * 合同附件 (JSON数组)
     */
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 获取合同类型名称
     */
    public String getContractTypeName() {
        return switch (contractType) {
            case "1" -> "固定期限";
            case "2" -> "无固定期限";
            case "3" -> "以完成一定工作为期限";
            case "4" -> "劳务协议";
            case "5" -> "实习协议";
            default -> "未知";
        };
    }

    /**
     * 获取合同状态名称
     */
    public String getContractStatusName() {
        return switch (contractStatus) {
            case "1" -> "有效";
            case "2" -> "到期";
            case "3" -> "解除";
            case "4" -> "终止";
            default -> "未知";
        };
    }

    /**
     * 是否无固定期限
     */
    public boolean isIndefinite() {
        return "2".equals(contractType);
    }

    /**
     * 是否即将到期 (30天内)
     */
    public boolean isExpiringSoon() {
        if (endDate == null) {
            return false;
        }
        return !endDate.isAfter(LocalDate.now().plusDays(30));
    }
}
