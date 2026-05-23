package com.zsc.module.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 审核记录表实体
 * 一张票据可被多次审核（退回后重新提交），完整追溯审核历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("biz_audit_log")
public class BizAuditLog {

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联票据ID */
    private Long billId;

    /** 操作: 1-通过 2-退回 */
    private String action;

    /** 审核意见 */
    private String comment;

    /** 审核人用户名 */
    private String auditBy;

    /** 审核时间 */
    private Date auditTime;

}
