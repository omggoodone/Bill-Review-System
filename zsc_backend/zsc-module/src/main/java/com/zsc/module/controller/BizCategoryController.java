package com.zsc.module.controller;

import com.zsc.common.annotation.Log;
import com.zsc.common.enums.BusinessType;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.common.response.ResultVo;
import com.zsc.module.domain.dto.BizCategoryDto;
import com.zsc.module.domain.dto.query.BizCategoryQueryDto;
import com.zsc.module.domain.entity.BizCategory;
import com.zsc.module.service.BizCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 业务类别控制器
 */
@Tag(name = "业务类别管理")
@Validated
@RestController
@RequestMapping("/api/category")
public class BizCategoryController {

    @Autowired
    private BizCategoryService bizCategoryService;

    /**
     * 获取指定ID的业务类别信息
     */
    @Operation(summary = "获取业务类别详情")
    @PreAuthorize("@ss.hasPermi('biz:category:query')")
    @GetMapping("/{id}")
    public ResultVo<BizCategory> get(@PathVariable long id) {
        BizCategory category = bizCategoryService.getById(id);
        return ResultVo.ok(category);
    }

    /**
     * 添加新的业务类别
     */
    @Operation(summary = "新增业务类别")
    @PreAuthorize("@ss.hasPermi('biz:category:add')")
    @Log(title = "业务类别管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultVo add(@Valid @RequestBody BizCategoryDto categoryDto) {
        bizCategoryService.addCategory(categoryDto);
        return ResultVo.ok();
    }

    /**
     * 更新业务类别信息
     */
    @Operation(summary = "修改业务类别")
    @PreAuthorize("@ss.hasPermi('biz:category:edit')")
    @Log(title = "业务类别管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultVo update(@Valid @RequestBody BizCategoryDto categoryDto) {
        bizCategoryService.updateCategory(categoryDto);
        return ResultVo.ok();
    }

    /**
     * 删除指定ID的业务类别
     */
    @Operation(summary = "删除业务类别")
    @PreAuthorize("@ss.hasPermi('biz:category:remove')")
    @Log(title = "业务类别管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResultVo delete(@PathVariable @Min(value = 1, message = "类别ID不能小于1") long id) {
        bizCategoryService.deleteCategory(id);
        return ResultVo.ok("删除成功");
    }

    /**
     * 分页查询业务类别列表
     */
    @Operation(summary = "查询业务类别列表")
    @PreAuthorize("@ss.hasPermi('biz:category:query')")
    @PostMapping("/query")
    public ResultVo<PageResult> query(@RequestBody BizCategoryQueryDto queryDto) {
        return ResultVo.ok(bizCategoryService.queryCategories(queryDto));
    }
}
