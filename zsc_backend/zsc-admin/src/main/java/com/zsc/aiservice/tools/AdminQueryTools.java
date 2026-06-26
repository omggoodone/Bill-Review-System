package com.zsc.aiservice.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zsc.common.core.domain.entity.SysRole;
import com.zsc.common.core.domain.entity.SysUser;
import com.zsc.module.domain.entity.BizAuditLog;
import com.zsc.module.domain.entity.BizBill;
import com.zsc.module.domain.entity.BizCategory;
import com.zsc.module.domain.vo.ReviewerWorkloadVo;
import com.zsc.module.mapper.BizAuditLogMapper;
import com.zsc.module.service.BizBillService;
import com.zsc.module.service.BizCategoryService;
import com.zsc.system.mapper.SysRoleMapper;
import com.zsc.system.mapper.SysUserMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员查询工具集
 * 暴露给 AI 的 10 个 @Tool 方法，全部只读，复用已有 Service/Mapper
 */
@Component
public class AdminQueryTools {

    @Autowired
    private BizBillService billService;

    @Autowired
    private BizCategoryService categoryService;

    @Autowired
    private BizAuditLogMapper auditLogMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== Tool 1: 系统全景统计 ====================

    @Tool("获取系统全景统计：用户总数、各角色人数、票据总量及各状态数量。用于管理员了解系统整体运行状况")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("总用户数", userMapper.countUser());
        stats.put("超级管理员数", userMapper.countUserByRoleKey("admin"));
        stats.put("系统管理员数", userMapper.countUserByRoleKey("admin_user"));
        stats.put("审核员数", userMapper.countUserByRoleKey("reviewer"));
        stats.put("普通用户数", userMapper.countUserByRoleKey("user"));
        stats.put("票据总量", billService.count());
        stats.put("待审核数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "1")));
        stats.put("已通过数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "2")));
        stats.put("已退回数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "3")));
        stats.put("草稿数", billService.count(
                new LambdaQueryWrapper<BizBill>().eq(BizBill::getStatus, "0")));
        return stats;
    }

    // ==================== Tool 2: 审核员工作量排行 ====================

    @Tool("获取审核员工作量排行。返回已格式化的 Markdown 表格文本。")
    public String getReviewerWorkload() {
        List<BizBill> bills = billService.list(
                new LambdaQueryWrapper<BizBill>()
                        .isNotNull(BizBill::getAuditBy)
                        .in(BizBill::getStatus, "2", "3"));

        Map<String, ReviewerWorkloadVo> map = new LinkedHashMap<>();
        for (BizBill b : bills) {
            String name = b.getAuditBy();
            ReviewerWorkloadVo vo = map.computeIfAbsent(name,
                    k -> new ReviewerWorkloadVo(k, 0, 0, 0));
            vo.setTotalCount(vo.getTotalCount() + 1);
            if ("2".equals(b.getStatus())) {
                vo.setApprovedCount(vo.getApprovedCount() + 1);
            } else {
                vo.setRejectedCount(vo.getRejectedCount() + 1);
            }
        }

        List<ReviewerWorkloadVo> sorted = map.values().stream()
                .sorted(Comparator.comparingLong(ReviewerWorkloadVo::getTotalCount).reversed())
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            return "暂无审核记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共 **").append(sorted.size()).append("** 位审核员参与审核。\n\n");
        sb.append("| 审核员 | 审核总量 | 通过数 | 退回数 | 通过率 |\n");
        sb.append("|--------|----------|--------|--------|--------|\n");
        for (ReviewerWorkloadVo w : sorted) {
            long total = w.getApprovedCount() + w.getRejectedCount();
            String rate = total > 0
                    ? String.format("%.1f%%", 100.0 * w.getApprovedCount() / total) : "0%";
            sb.append("| ").append(w.getReviewerName())
                    .append(" | ").append(w.getTotalCount())
                    .append(" | ").append(w.getApprovedCount())
                    .append(" | ").append(w.getRejectedCount())
                    .append(" | ").append(rate)
                    .append(" |\n");
        }

        return sb.toString();
    }
    // ==================== Markdown 表格格式化 ====================

    /**
     * 将结构化数据转为 Markdown 管道表格字符串。
     * LLM 拿到后直接用，无需自己排版。
     */
    private String toMarkdownTable(List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        // 表头
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        // 分隔行
        sb.append("| ").append(headers.stream().map(h -> "---").collect(Collectors.joining(" | "))).append(" |\n");
        // 数据行
        for (List<String> row : rows) {
            sb.append("| ").append(String.join(" | ", row)).append(" |\n");
        }
        return sb.toString();
    }
}
