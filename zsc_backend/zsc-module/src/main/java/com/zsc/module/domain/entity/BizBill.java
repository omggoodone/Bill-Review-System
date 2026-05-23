package com.zsc.module.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 票据主表实体
 * 状态流转: 0-草稿 → 1-已提交 → 2-已通过 / 3-已退回
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("biz_bill")
public class BizBill {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 票据编号，提交时自动生成 BILL-YYYYMMDD-NNNN */
    private String billNo;

    /** 票据标题，最长 200 字符 */
    private String title;

    /** 票据类别ID，关联 biz_category */
    private Long categoryId;

    /** 金额，精确到分 */
    private BigDecimal amount;

    /** 票据描述 */
    private String description;

    /** 状态: 0-草稿 1-已提交 2-已通过 3-已退回 */
    private String status;

    /** 附件路径（单文件，保留兼容） */
    private String attachment;

    /** 创建者用户名 */
    private String createBy;

    /** 创建时间 */
    private Date createTime;

    /** 更新者用户名 */
    private String updateBy;

    /** 更新时间 */
    private Date updateTime;

    /** 最近审核人用户名 */
    private String auditBy;

    /** 最近审核时间 */
    private Date auditTime;

    /** 最近审核意见 */
    private String auditComment;

    /** 备注 */
    private String remark;

}
