package com.zsc.module.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 票据审核请求体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BizBillReviewDto {

    @NotNull(message = "票据ID不能为空")
    private Long billId;

    /** 审核结果: 1-通过 2-退回 */
    @NotBlank(message = "审核结果不能为空")
    private String action;

    /** 审核意见 */
    private String comment;

}
