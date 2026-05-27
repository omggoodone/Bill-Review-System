package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户已通过票据金额汇总
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAmountVo {
    /** 用户名 */
    private String userName;
    /** 已通过金额合计 */
    private long totalAmount;
}
