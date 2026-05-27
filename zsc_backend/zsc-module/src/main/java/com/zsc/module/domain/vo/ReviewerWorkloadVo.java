package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核员工作量统计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerWorkloadVo {
    /** 审核员用户名 */
    private String reviewerName;
    /** 审核总数 */
    private long totalCount;
    /** 通过数 */
    private long approvedCount;
    /** 退回数 */
    private long rejectedCount;
}
