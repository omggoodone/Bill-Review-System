package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 票据详情出参，含附件列表和完整审核历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizBillDetailVo {

    private Long id;

    private String billNo;

    private String title;

    private Long categoryId;

    /** 类别名称，JOIN biz_category 查询 */
    private String categoryName;

    private BigDecimal amount;

    private String description;

    /** 状态: 0-草稿 1-已提交 2-已通过 3-已退回 */
    private String status;

    /** 创建者用户名 */
    private String createBy;

    private Date createTime;

    private Date updateTime;

    /** 最近审核人 */
    private String auditBy;

    private Date auditTime;

    private String auditComment;

    /** 附件列表 */
    private List<BizBillFileVo> files;

    /** 完整审核历史 */
    private List<BizAuditLogVo> auditLogs;

}
