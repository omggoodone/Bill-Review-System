package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 票据列表项出参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizBillVo {

    private Long id;

    private String billNo;

    private String title;

    /** 类别ID */
    private Long categoryId;

    /** 类别名称，JOIN biz_category 查询 */
    private String categoryName;

    private BigDecimal amount;

    private String description;

    /** 状态: 0-草稿 1-已提交 2-已通过 3-已退回 */
    private String status;

    private String attachment;

    /** 创建者用户名 */
    private String createBy;

    private Date createTime;

    /** 审核人用户名 */
    private String auditBy;

    private Date auditTime;

    /** 审核意见 */
    private String auditComment;

}
