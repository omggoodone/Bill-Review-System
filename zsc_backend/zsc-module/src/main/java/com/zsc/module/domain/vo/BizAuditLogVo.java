package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审核记录出参，嵌套在 BizBillDetailVo 内
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizAuditLogVo {

    private Long id;

    /** 操作: 1-通过 2-退回 */
    private String action;

    /** 审核意见 */
    private String comment;

    /** 审核人用户名 */
    private String auditBy;

    /** 审核时间 */
    private Date auditTime;

}
