package com.zsc.module.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 票据新增/修改请求体
 * status="0" 保存草稿，status="1" 直接提交
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BizBillDto {

    /** 票据ID，修改时传入 */
    private Long id;

    @NotBlank(message = "票据标题不能为空")
    @Size(max = 255, message = "标题不能超过255个字符")
    private String title;

    /** 票据类别ID */
    private Long categoryId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @DecimalMax(value = "99999999.99", message = "金额不能超过99999999.99")
    private BigDecimal amount;

    /** 票据描述 */
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    /** 状态: 0-草稿 1-提交 */
    @NotBlank(message = "状态不能为空")
    private String status;

    /** 附件路径，逗号分隔 */
    private String attachment;

}
