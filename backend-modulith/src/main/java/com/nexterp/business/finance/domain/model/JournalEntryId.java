package com.nexterp.business.finance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 凭证复合主键
 * 用于分区表 fi_journal_entry_hdr 和 fi_journal_entry_itm
 *
 * @author NextERP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntryId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会计年度 (分区键)
     */
    private Integer fiscalYear;

    /**
     * 凭证ID
     */
    private Long id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JournalEntryId that = (JournalEntryId) o;
        return fiscalYear != null && fiscalYear.equals(that.fiscalYear)
                && id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31 * (fiscalYear != null ? fiscalYear.hashCode() : 0)
                + (id != null ? id.hashCode() : 0);
    }
}
