package com.zsc.module.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务类别DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BizCategoryDto {

    /**
     * 类别ID
     */
    private Long categoryId;

    /**
     * 类别名称
     */
    @NotBlank(message = "类别名称不能为空")
    @Size(max = 100, message = "类别名称不能超过100个字符")
    private String categoryName;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

    /**
     * 状态（0正常 1停用）
     */
    @NotBlank(message = "状态不能为空")
    private String status;

}
