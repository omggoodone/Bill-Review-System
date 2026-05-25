package com.zsc.module.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势图数据项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendItemVo {
    /** 标签（如"第1周"） */
    private String label;
    /** 数量 */
    private long count;
}
