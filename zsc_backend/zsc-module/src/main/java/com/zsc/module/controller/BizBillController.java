package com.zsc.module.controller;

import com.zsc.common.annotation.Log;
import com.zsc.common.enums.BusinessType;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.common.response.ResultVo;
import com.zsc.module.domain.dto.BizBillDto;
import com.zsc.module.domain.dto.BizBillReviewDto;
import com.zsc.module.domain.dto.query.BizBillQueryDto;
import com.zsc.module.domain.vo.BizBillDetailVo;
import com.zsc.module.domain.vo.BizBillVo;
import com.zsc.module.domain.vo.TrendItemVo;
import com.zsc.module.service.BizBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 票据控制器
 * 普通用户: 新增/编辑/提交/删除/查询自己的票据
 * 管理员: 审核票据 + 查看全部
 */
@Slf4j
@Tag(name = "票据管理")
@Validated
@RestController
@RequestMapping("/api/bill")
public class BizBillController {

    @Autowired
    private BizBillService bizBillService;

    // ==================== 查询 ====================

    /**
     * 分页条件查询票据列表
     */
    @Operation(summary = "查询票据列表")
    @PreAuthorize("@ss.hasPermi('biz:bill:list')")
    @PostMapping("/query")
    public ResultVo<PageResult<BizBillVo>> query(@RequestBody BizBillQueryDto queryDto) {
        PageResult pageResult = bizBillService.queryBills(queryDto);
        log.info(pageResult.getList().toString());
        return ResultVo.ok(pageResult);
    }

    /**
     * 查看票据详情（含附件列表和审核历史）
     */
    @Operation(summary = "获取票据详情")
    @PreAuthorize("@ss.hasPermi('biz:bill:query')")
    @GetMapping("/{id}")
    public ResultVo<BizBillDetailVo> get(@PathVariable @Min(value = 1, message = "票据ID不能小于1") Long id) {
        return ResultVo.ok(bizBillService.getBillDetail(id));
    }

    // ==================== 新增/编辑/提交/删除 ====================

    /**
     * 新增票据
     * status="0" 保存草稿，status="1" 直接提交
     */
    @Operation(summary = "新增票据")
    @PreAuthorize("@ss.hasPermi('biz:bill:add')")
    @Log(title = "票据管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultVo add(@Valid @RequestBody BizBillDto dto) {
        bizBillService.addBill(dto);
        return ResultVo.ok(dto.getStatus().equals("0") ? "草稿已保存" : "提交成功");
    }

    /**
     * 修改票据（仅限草稿或已退回状态）
     */
    @Operation(summary = "修改票据")
    @PreAuthorize("@ss.hasPermi('biz:bill:add')")
    @Log(title = "票据管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultVo update(@Valid @RequestBody BizBillDto dto) {
        bizBillService.updateBill(dto);
        return ResultVo.ok("修改成功");
    }

    /**
     * 提交草稿进入审核流程
     */
    @Operation(summary = "提交审核")
    @PreAuthorize("@ss.hasPermi('biz:bill:add')")
    @Log(title = "票据管理", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{id}")
    public ResultVo submit(@PathVariable @Min(value = 1, message = "票据ID不能小于1") Long id) {
        bizBillService.submitBill(id);
        return ResultVo.ok("提交成功");
    }

    /**
     * 删除草稿票据及其附件
     */
    @Operation(summary = "删除票据")
    @PreAuthorize("@ss.hasPermi('biz:bill:remove')")
    @Log(title = "票据管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResultVo delete(@PathVariable @Min(value = 1, message = "票据ID不能小于1") Long id) {
        bizBillService.deleteBill(id);
        return ResultVo.ok("删除成功");
    }

    // ==================== 审核 ====================

    /**
     * 审核票据（通过或退回）
     */
    @Operation(summary = "审核票据")
    @PreAuthorize("@ss.hasPermi('biz:bill:review')")
    @Log(title = "票据管理", businessType = BusinessType.UPDATE)
    @PostMapping("/review")
    public ResultVo review(@Valid @RequestBody BizBillReviewDto dto) {
        bizBillService.reviewBill(dto);
        return ResultVo.ok("审核成功");
    }

    @Operation(summary = "本月提交趋势")
    @PreAuthorize("@ss.hasPermi('biz:bill:list')")
    @GetMapping("/trend")
    public ResultVo<List<TrendItemVo>> trend() {
        return ResultVo.ok(bizBillService.getMonthlyTrend());
    }

    @Operation(summary = "各类别金额汇总")
    @PreAuthorize("@ss.hasPermi('biz:bill:list')")
    @GetMapping("/category-summary")
    public ResultVo<List<TrendItemVo>> categorySummary() {
        return ResultVo.ok(bizBillService.getCategoryAmountSummary());
    }

}
